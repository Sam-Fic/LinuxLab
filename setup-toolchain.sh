#!/usr/bin/env bash
# 一键准备安卓构建工具链：JDK17 / Android SDK 34 / NDK / Gradle / 原生二进制
#
# 用法：bash ./setup-toolchain.sh
# 可选环境变量：
#   TOOLS_ROOT=/opt     工具链安装位置（默认 /opt；CI 或无 root 时可指向任意可写目录）
#   GRADLE_USER_HOME    Gradle 缓存目录（默认 $TOOLS_ROOT/gradle-home）
#
# 说明：脚本幂等，已存在的组件会跳过。仓库不保存任何预编译二进制，
#       rootfs / proot / 静态 busybox 由本脚本从各自官方地址下载。
set -e

PROJECT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS_ROOT="${TOOLS_ROOT:-/opt}"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$TOOLS_ROOT/gradle-home}"
export GRADLE_USER_HOME

if [ ! -w "$TOOLS_ROOT" ] && command -v sudo >/dev/null 2>&1; then
  sudo -n mkdir -p "$TOOLS_ROOT" 2>/dev/null || true
  sudo -n chown -R "$(id -u):$(id -g)" "$TOOLS_ROOT" 2>/dev/null || true
fi
mkdir -p "$TOOLS_ROOT/jdk" "$TOOLS_ROOT/android-sdk" "$TOOLS_ROOT/gradle-home"

# 1. swap（内存小于 4GB 时补一块，构建 Kotlin/NDK 需要）
TOTAL_MEM_MB=$(awk '/MemTotal/ {print int($2/1024)}' /proc/meminfo 2>/dev/null || echo 8192)
if [ "${TOTAL_MEM_MB:-8192}" -lt 4096 ] && ! swapon --show 2>/dev/null | grep -q swapfile; then
  if command -v sudo >/dev/null 2>&1; then
    sudo -n dd if=/dev/zero of=/swapfile bs=1M count=2560 status=none 2>/dev/null || true
    sudo -n chmod 600 /swapfile 2>/dev/null || true
    sudo -n mkswap /swapfile >/dev/null 2>/dev/null || true
    sudo -n swapon /swapfile 2>/dev/null || true
  fi
fi
echo "[1/5] swap OK"

# 2. JDK 17
if [ ! -x "$TOOLS_ROOT/jdk"/bin/java ]; then
  curl -sL -o "$TOOLS_ROOT/jdk".tar.gz "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13%2B11/OpenJDK17U-jdk_x64_linux_hotspot_17.0.13_11.tar.gz"
  tar xzf "$TOOLS_ROOT/jdk".tar.gz -C "$TOOLS_ROOT/jdk" --strip-components=1
  rm -f "$TOOLS_ROOT/jdk".tar.gz
fi
export JAVA_HOME="$TOOLS_ROOT/jdk"
echo "[2/5] JDK OK"

# 3. Android cmdline-tools + SDK 组件（含 NDK / CMake）
if [ ! -x "$TOOLS_ROOT/android-sdk"/cmdline-tools/latest/bin/sdkmanager ]; then
  curl -sL -o "$TOOLS_ROOT/cmdtools.zip" "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
  mkdir -p "$TOOLS_ROOT/android-sdk"/cmdline-tools/latest
  unzip -q -o "$TOOLS_ROOT/cmdtools.zip" -d "$TOOLS_ROOT/cmdtmp"
  cp -r "$TOOLS_ROOT/cmdtmp"/cmdline-tools/* "$TOOLS_ROOT/android-sdk"/cmdline-tools/latest/
  rm -rf "$TOOLS_ROOT/cmdtools.zip" "$TOOLS_ROOT/cmdtmp"
fi
export ANDROID_HOME="$TOOLS_ROOT/android-sdk"
export ANDROID_SDK_ROOT="$TOOLS_ROOT/android-sdk"
yes | "$TOOLS_ROOT/android-sdk"/cmdline-tools/latest/bin/sdkmanager --sdk_root="$TOOLS_ROOT/android-sdk" --licenses >/dev/null 2>&1 || true
# 说明：compileSdk=37（Compose 1.12 要求 API 37），build-tools 随之用 37；
#       platform-34 仍保留，便于对照与降级排查
"$TOOLS_ROOT/android-sdk"/cmdline-tools/latest/bin/sdkmanager --sdk_root="$TOOLS_ROOT/android-sdk" \
  "platform-tools" "platforms;android-34" "platforms;android-37.0" \
  "build-tools;34.0.0" "build-tools;37.0.0" \
  "ndk;27.0.12077973" "cmake;3.22.1" > /tmp/sdk-install.log 2>&1
echo "[3/5] SDK/NDK OK -> $(ls "$TOOLS_ROOT/android-sdk")"

# 4. 原生二进制：Alpine Linux rootfs + 静态 proot（arm64）
mkdir -p "$PROJECT/app/src/main/assets" "$PROJECT/app/src/main/jniLibs/arm64-v8a"
if [ ! -f "$PROJECT/app/src/main/assets/alpine-aarch64.tar" ]; then
  curl -sL -o /tmp/alpine.tar.gz \
    "https://dl-cdn.alpinelinux.org/alpine/v3.20/releases/aarch64/alpine-minirootfs-3.20.3-aarch64.tar.gz"
  gunzip -c /tmp/alpine.tar.gz > "$PROJECT/app/src/main/assets/alpine-aarch64.tar"
  rm -f /tmp/alpine.tar.gz
fi
if [ ! -f "$PROJECT/app/src/main/assets/busybox-static-aarch64" ]; then
  # 静态 busybox：不依赖动态解释器，作为 /bin/sh 的兜底
  curl -sL -o /tmp/bb.apk \
    "https://dl-cdn.alpinelinux.org/alpine/v3.20/main/aarch64/busybox-static-1.36.1-r31.apk"
  python3 - <<'PY'
import zlib, io, tarfile, os
data=open('/tmp/bb.apk','rb').read(); pos=0
while pos < len(data):
    d=zlib.decompressobj(31)
    try: out=d.decompress(data[pos:])
    except Exception: break
    if not out: break
    try: tf=tarfile.open(fileobj=io.BytesIO(out))
    except Exception: break
    done=False
    for m in tf.getmembers():
        if m.name.endswith('busybox.static'):
            open(os.environ.get('PROJECT','.')+'/app/src/main/assets/busybox-static-aarch64','wb').write(tf.extractfile(m).read())
            done=True
    if done: break
    if not d.unused_data: break
    pos += len(data[pos:]) - len(d.unused_data)
os.chmod(os.environ.get('PROJECT','.')+'/app/src/main/assets/busybox-static-aarch64', 0o644)
PY
  rm -f /tmp/bb.apk
fi
if [ ! -f "$PROJECT/app/src/main/jniLibs/arm64-v8a/libproot.so" ]; then
  curl -sL -o "$PROJECT/app/src/main/jniLibs/arm64-v8a/libproot.so" \
    "https://github.com/proot-me/proot/releases/download/v5.3.0/proot-v5.3.0-aarch64-static"
  chmod 755 "$PROJECT/app/src/main/jniLibs/arm64-v8a/libproot.so"
fi
echo "[4/5] 原生二进制 OK"

# 5. Gradle 8.9（命令行备用；项目本身带 Wrapper）
if [ ! -x "$TOOLS_ROOT/gradle"/gradle-8.9/bin/gradle ]; then
  mkdir -p "$TOOLS_ROOT/gradle"
  curl -sL -o "$TOOLS_ROOT/gradle".zip "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
  unzip -q -o "$TOOLS_ROOT/gradle".zip -d "$TOOLS_ROOT/gradle"
  rm -f "$TOOLS_ROOT/gradle".zip
fi
chmod +x "$PROJECT/gradlew"
echo "[5/5] Gradle OK"

echo "=== 工具链就绪 ==="
echo "构建命令："
echo "  export JAVA_HOME="$TOOLS_ROOT/jdk" ANDROID_HOME="$TOOLS_ROOT/android-sdk" GRADLE_USER_HOME="$TOOLS_ROOT/gradle"-home"
echo "  cd $PROJECT && ./gradlew assembleRelease"

#!/usr/bin/env bash
# build-apk.sh - 从本仓库的 apktool 解包目录重建并签名 APK
#
# 用法:
#   bash scripts/build-apk.sh [input_dir] [output_apk]
#   默认 input_dir = 仓库根, output_apk = dist/linux-starter-release.apk
#
# 环境变量:
#   APKTOOL_JAR          apktool jar 路径（默认当前目录下 tools/apktool.jar，或 PATH 中的 apktool）
#   ANDROID_BUILD_TOOLS  含 aapt2/zipalign/apksigner 的 build-tools 目录（默认自动探测）
#   KEYSTORE / KEYSTORE_PASS / KEYSTORE_ALIAS / KEY_PASS
#                        APK 签名参数；未提供时自动生成临时 debug keystore
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INPUT_DIR="${1:-$ROOT}"
OUTPUT_APK="${2:-$ROOT/dist/linux-starter-release.apk}"

echo "==> 输入目录: $INPUT_DIR"
echo "==> 输出 APK: $OUTPUT_APK"

# ---------- 1. 定位 apktool ----------
APKTOOL_JAR="${APKTOOL_JAR:-}"
if [[ -z "$APKTOOL_JAR" ]]; then
  if [[ -f "$ROOT/tools/apktool.jar" ]]; then
    APKTOOL_JAR="$ROOT/tools/apktool.jar"
  elif command -v apktool >/dev/null 2>&1; then
    APKTOOL_JAR="apktool"
  else
    echo "错误: 未找到 apktool，请设置 APKTOOL_JAR 或在 tools/ 放置 apktool.jar" >&2
    exit 1
  fi
fi
echo "==> apktool: $APKTOOL_JAR"
java -jar "$APKTOOL_JAR" --version >/dev/null 2>&1 || { echo "错误: apktool 无法运行（需要 Java 8+）" >&2; exit 1; }

# ---------- 2. 定位 build-tools（aapt2/zipalign/apksigner） ----------
BT="${ANDROID_BUILD_TOOLS:-}"
if [[ -z "$BT" ]]; then
  for cand in "$ANDROID_HOME/build-tools"/* "$ANDROID_SDK_ROOT/build-tools"/* "$ROOT/tools/build-tools"/* "$ROOT/third_party/build-tools"/*; do
    if [[ -x "$cand/zipalign" && -x "$cand/apksigner" ]]; then BT="$cand"; break; fi
  done
fi
if [[ -z "$BT" ]]; then
  echo "错误: 未找到 Android build-tools（需要 aapt2/zipalign/apksigner），请设置 ANDROID_BUILD_TOOLS" >&2
  exit 1
fi
echo "==> build-tools: $BT"
export PATH="$BT:$PATH"

# ---------- 3. 生成 apktool.yml（本仓库只含 apktool.json，需要标准 yml 才能用官方 apktool 构建） ----------
if [[ ! -f "$INPUT_DIR/apktool.yml" && -f "$INPUT_DIR/apktool.json" ]]; then
  echo "==> 从 apktool.json 生成 apktool.yml"
  python3 - "$INPUT_DIR" <<'PY'
import json, sys, os
src = sys.argv[1]
d = json.load(open(os.path.join(src, 'apktool.json'), encoding='utf-8'))
lines = ['!!brut.androlib.meta.MetaInfo']
lines.append('apkFileName: %s' % d.get('apkFileName', 'app-src.apk'))
lines.append('compressionType: %s' % str(d.get('compressionType', False)).lower())
dnc = d.get('doNotCompress') or []
lines.append('doNotCompress:')
for x in dnc:
    lines.append('- %s' % x)
lines.append('isFrameworkApk: %s' % str(d.get('isFrameworkApk', False)).lower())
pkg = d.get('PackageInfo', {})
lines.append('packageInfo:')
lines.append("  forcedPackageId: '%s'" % pkg.get('forcedPackageId', '127'))
lines.append('  renameManifestPackage: %s' % pkg.get('renameManifestPackage'))
sd = d.get('sdkInfo') or {}
if sd:
    lines.append('sdkInfo:')
    for k in ('minSdkVersion', 'targetSdkVersion'):
        if sd.get(k) is not None:
            lines.append("  %s: '%s'" % (k, sd[k]))
lines.append('sharedLibrary: %s' % str(d.get('sharedLibrary', False)).lower())
lines.append('sparseResources: %s' % str(d.get('sparseResources', False)).lower())
uf = d.get('unknownFiles') or {}
lines.append('unknownFiles:')
for k, v in uf.items():
    lines.append("  %s: '%s'" % (k, v))
fr = d.get('UsesFramework', {})
lines.append('usesFramework:')
lines.append('  ids:')
for i in fr.get('ids', []):
    lines.append('  - %d' % i)
lines.append('  tag: %s' % fr.get('tag'))
lines.append('version: %s' % str(d.get('version', '2.4.0')).split('-')[0])
vi = d.get('VersionInfo', {})
lines.append('versionInfo:')
lines.append("  versionCode: '%s'" % vi.get('versionCode', '1'))
lines.append('  versionName: %s' % vi.get('versionName', '1.0'))
open(os.path.join(src, 'apktool.yml'), 'w', encoding='utf-8').write('\n'.join(lines) + '\n')
print('apktool.yml 已生成')
PY
elif [[ ! -f "$INPUT_DIR/apktool.yml" ]]; then
  echo "错误: 缺少 apktool.yml（且无 apktool.json 可转换）" >&2
  exit 1
fi

# ---------- 4. apktool 重建 ----------
TMPDIR_APK="$(mktemp -d)"
trap 'rm -rf "$TMPDIR_APK"' EXIT
UNSIGNED_APK="$TMPDIR_APK/unsigned.apk"
echo "==> apktool b 重建 APK..."
java -jar "$APKTOOL_JAR" b "$INPUT_DIR" -o "$UNSIGNED_APK"

# ---------- 5. 签名（zipalign + apksigner） ----------
ALIGNED_APK="$TMPDIR_APK/aligned.apk"
zipalign -f 4 "$UNSIGNED_APK" "$ALIGNED_APK"

KS="${KEYSTORE:-}"
if [[ -z "$KS" ]]; then
  echo "==> 未提供签名密钥，生成临时 debug keystore"
  KS="$TMPDIR_APK/debug.keystore"
  keytool -genkeypair -v -keystore "$KS" -alias androiddebugkey -keyalg RSA -keysize 2048 \
    -validity 10950 -storepass android -keypass android \
    -dname "CN=Android Debug,O=Android,C=US" >/dev/null 2>&1
  KEYSTORE_PASS="${KEYSTORE_PASS:-android}"
  KEYSTORE_ALIAS="${KEYSTORE_ALIAS:-androiddebugkey}"
  KEY_PASS="${KEY_PASS:-android}"
fi

mkdir -p "$(dirname "$OUTPUT_APK")"
apksigner sign --ks "$KS" \
  --ks-pass "pass:${KEYSTORE_PASS:-android}" \
  --key-pass "pass:${KEY_PASS:-android}" \
  --ks-key-alias "${KEYSTORE_ALIAS:-androiddebugkey}" \
  --out "$OUTPUT_APK" "$ALIGNED_APK"

echo "==> 验证签名..."
apksigner verify --verbose "$OUTPUT_APK" | head -8
echo "==> 完成: $OUTPUT_APK"
ls -la "$OUTPUT_APK"
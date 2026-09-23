# Linux Starter

面向初学者的 Linux 学习 App，Material 3 + 莫奈（Monet）动态取色，内置 **221 条命令速查** 与一个 **真实 Linux 终端**（Alpine Linux + proot + 原生 PTY）。

- **包名**：`com.linuxlab.starter`
- **版本**：1.2.1（versionCode 10）
- **minSdk 26（Android 8.0） / targetSdk 34 / compileSdk 34**
- **ABI**：仅 `arm64-v8a`（rootfs 与 proot 均为 aarch64）
- **APK 体积**：约 7.5 MB（内置了完整的 Alpine Linux 用户态）

---

## 一、功能

| 模块 | 说明 |
| --- | --- |
| **真实终端** | 内置 Alpine Linux 用户态，真 PTY、真进程、真 ELF 二进制；`apk` 装包、`python3`、`top`、`vim` 都能跑 |
| 全局搜索 | 命令名、中文用途、英文说明、参数、示例代码全部参与匹配；按「名字命中 > 说明 > 示例」打分排序 |
| 分类速查 | 12 大分类，首页网格直达；搜索页可按分类二次筛选 |
| 命令详情 | 一句话中英说明 + 详细解释 + 语法 + 常用选项表 + 可复制示例 + 小贴士 + 相关命令跳转 |
| 内置沙盒终端（回退） | 若 proot 在本机被限制，可一键切到纯 Kotlin 实现的模拟终端（虚拟文件系统 + 20 条命令 + 7 个闯关任务） |
| 今日推荐 / 一键复制 / 外观设置 | 每日命令、示例复制、主题与莫奈取色开关 |

## 二、真实终端是怎么做到的（Termux 同款原理）

Termux 的本质是在 Android 上跑一个 **真实 Linux 用户态**。本项目用同样的三件套实现：

```
┌─────────────────────────────────────────────────────────────┐
│ Compose UI（TerminalView：VT100/ANSI 渲染 + 输入 + 扩展键） │
├─────────────────────────────────────────────────────────────┤
│ TerminalBuffer：转义序列解析（光标/颜色/滚动区域/备用屏）   │
├─────────────────────────────────────────────────────────────┤
│ PtyNative（JNI） ──► pty.c：forkpty + execve + TIOCSWINSZ   │
├─────────────────────────────────────────────────────────────┤
│ proot（静态 ELF，随 APK 打包）──► Alpine Linux rootfs        │
└─────────────────────────────────────────────────────────────┘
```

| 组成 | 文件 | 说明 |
| --- | --- | --- |
| 原生 PTY | `app/src/main/cpp/pty.c` | `forkpty()` 起子进程、`execve()` 执行 proot；非阻塞读写、`TIOCSWINSZ` 改变窗口大小、`waitpid` 回收、会话结束发 `SIGHUP` |
| JNI 封装 | `terminal/pty/PtyNative.kt` | `createPty / writePty / readPty / resizePty / waitPty / closePty` |
| 终端模拟器 | `terminal/emulator/TerminalBuffer.kt` | 支持 CUP/ED/EL/IL/DL/ICH/DCH/SU/SD/DECSTBM/DECSC/备用屏幕(?1049)、SGR 16 色/256 色/24 位真彩、自动换行、Tab 停位、3000 行回滚、东亚宽字符占 2 格 |
| 配色 | `terminal/emulator/ColorResolver.kt` | xterm 256 色表 + 真彩；加粗自动提亮；默认前景/背景跟随界面主题 |
| 会话管理 | `terminal/TerminalSession.kt` | 引导 → 起进程 → 读循环（UTF-8 流式解码）→ 写输入 → 尺寸同步 → 重启/诊断 |
| 引导安装 | `terminal/Bootstrap.kt` | 首次启动解压内置 rootfs（约 8.7MB / 518 个条目，处理目录、软链接、权限位），写入 `resolv.conf`、`.profile`，拼装 proot 命令 |
| 内置资产 | `assets/alpine-aarch64.tar`、`jniLibs/arm64-v8a/libproot.so` | Alpine minirootfs 3.20.3（aarch64）+ proot 5.3.0 静态可执行文件 |

**启动链路**：首页 → 终端页 → 解压 rootfs（仅首次，约 2 秒）→ `proot --rootfs=... --bind=/proc,/dev,/sys,/system,/home,/sdcard --cwd=/root --root-id --kill-on-exit --link2symlink /bin/sh -l` → 通过 PTY 拿到一个真正的 shell 会话。

**关键配置**（`app/build.gradle.kts`）：

```kotlin
ndkVersion = "27.0.12077973"
externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt") } }
defaultConfig { ndk { abiFilters += listOf("arm64-v8a") } }
packaging {
    jniLibs {
        useLegacyPackaging = true                 // proot 必须有磁盘上的可执行路径
        keepDebugSymbols += "**/libproot.so"      // 静态可执行文件不能被 strip
    }
}
```
Manifest 中 `android:extractNativeLibs="true"` 同样是为了让 `libproot.so` 被解压成可 exec 的真实文件。

### 终端里能做什么

```bash
uname -a                       # 真内核/真架构信息（rootfs 内）
ls -la /                       # 真文件系统
apk update && apk add python3  # 真包管理器，装完就能用
python3 -c "print(2**100)"     # 真解释器
top                            # 需要 TTY 的全屏程序（PTY 已支持）
vi /tmp/a.txt                  # 全屏编辑器
ping -c 3 1.1.1.1              # 联网（已申请 INTERNET 权限）
```

交互：软键盘直连 PTY、`↑/↓` 取历史（由 shell 提供）、`^C ^D ^L ^Z`、Tab、方向键、HOME/END/PGUP/PGDN、CTRL/ALT 修饰键、上下拖动查看回滚、字号 10/12/14/17sp 切换、一键重启会话、一键跑诊断命令。

## 三、兼容性说明（重要）

- **架构**：本包只含 arm64 原生二进制；非 arm64 设备会启动失败并提示切换沙盒终端。
- **依赖 ptrace**：proot 通过 ptrace 实现路径翻译，个别深度定制 ROM / 开启强 SELinux 策略的设备可能失败；失败时终端会显示具体错误，并可回退到内置沙盒终端。
- **不需要 root**：proot 是纯用户态方案。
- **首次启动**：解压约 8.7MB 到 App 私有目录，之后秒开；全程离线（APK 内已内置）。
- **联网**：`apk` / `ping` / `python` 需要网络，已申请 `INTERNET` 权限。

## 四、Material 3 + 莫奈取色

`ui/theme/Theme.kt`：

```kotlin
val colorScheme = when {
    useDynamicColor && Build.VERSION.SDK_INT >= S && darkTheme -> dynamicDarkColorScheme(context)
    useDynamicColor && Build.VERSION.SDK_INT >= S             -> dynamicLightColorScheme(context)
    darkTheme -> DarkColors
    else      -> LightColors
}
```
Android 12+ 从壁纸取色，整站（含终端默认前景/背景之外的所有控件）自动跟随；关闭开关或低版本回退到内置的「终端深绿」配色。设置页可切跟随系统/浅色/深色，偏好存 SharedPreferences。

## 五、命令库（221 条 / 12 分类）

文件与目录 21 · 文本查看与编辑 23 · 搜索与文本处理 14 · 权限与用户 21 · 进程与作业 18 · 系统信息与环境 28 · 软件包管理 12 · 网络与远程 20 · 磁盘与存储 13 · 压缩与归档 11 · 服务与 systemd 15 · Shell 脚本入门 25

## 六、目录结构

```
LinuxStarter/
├── app/src/main/
│   ├── cpp/pty.c + CMakeLists.txt        # 原生 PTY（JNI）
│   ├── assets/alpine-aarch64.tar         # 内置 Alpine Linux 用户态（8.7MB）
│   ├── jniLibs/arm64-v8a/libproot.so     # 静态 proot（1.4MB）
│   └── kotlin/com/linuxlab/starter/
│       ├── MainActivity.kt
│       ├── model/ data/                  # 命令库（221 条）
│       ├── terminal/                     # 真实终端：pty / emulator / Bootstrap / Session
│       │   └── (沙盒回退：VirtualFs.kt、TerminalEngine.kt)
│       └── ui/                           # AppNav、ThemePrefs、theme/、screens/
├── setup-toolchain.sh                    # 一键准备构建环境（下载工具链与原生二进制）
├── CONTRIBUTING.md / NOTICE / RELEASE.md  # 贡献指南、第三方许可声明、发布流程
└── local.properties                      # sdk.dir
```

> 注意：AGP 会把 `assets` 目录里的 `.gz` **自动解压后**再打包（实测 `x.tar.gz` → APK 中的 `x.tar`），
> 所以仓库里内置的是未压缩的 `alpine-aarch64.tar`，由 APK 的 zip 层负责压缩（8.7MB → 3.8MB）。

## 七、在本机编译

1. Android Studio（Ladybug+），安装 **Android SDK Platform 34、Build-Tools 34、NDK 27.0.12077973、CMake 3.22.1**。
2. 修改 `local.properties` 的 `sdk.dir`。
3. 构建：

```bash
./gradlew assembleRelease   # 发布版（R8 压缩，约 7.5MB）
./gradlew assembleDebug     # 调试版（抓 logcat 用，约 66MB）
```
产物：`app/build/outputs/apk/{debug,release}/app-*.apk`

发布版目前使用 Android 默认 debug 签名（`CN=Android Debug`），上架前请配置自己的 `signingConfigs`。

## 八、一键准备工具链

仓库自带 `setup-toolchain.sh`，会按需下载并配置全部依赖（幂等，已存在则跳过）：

- JDK 17（Temurin）→ `$TOOLS_ROOT/jdk`
- Android SDK 34 + Build-Tools 34 + NDK 27 + CMake → `$TOOLS_ROOT/android-sdk`
- Gradle 8.9 → `$TOOLS_ROOT/gradle`，缓存 → `$GRADLE_USER_HOME`
- 原生二进制：Alpine minirootfs、静态 proot、静态 busybox（**不进仓库**，已 `.gitignore`）
- 内存小于 4GB 时自动补 2.5GB swap（Kotlin + NDK 编译必需）

```bash
bash ./setup-toolchain.sh

export TOOLS_ROOT=/opt                     # 默认值，无 root 可换成任意可写目录
export JAVA_HOME=$TOOLS_ROOT/jdk
export ANDROID_HOME=$TOOLS_ROOT/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export GRADLE_USER_HOME=$TOOLS_ROOT/gradle-home

./gradlew assembleRelease
```

> 本 APK 最初是在只有 2GB 内存、无真机/模拟器的沙盒中真实编译通过的；
> 上述脚本就是那套环境的沉淀，在普通的 Linux 开发机 / CI 上同样可用（见 `.github/workflows/build.yml`）。

`gradle.properties` 里做了低内存适配（`-Xmx1150m`、单 worker、Kotlin 编译跑在 Gradle 进程内）；开发机上可按需调大以加快编译。

## ⚠️ 安装须知（非常重要）

Android 10（API 29）起，系统**禁止 targetSdk ≥ 29 的应用 exec() 自己私有目录
（`/data/data/<包名>`）里的文件**，官方理由是可写目录执行代码违反 W^X 策略
[官方文档](https://developer.android.com/about/versions/10/behavior-changes-10)。
Termux 也正因为这条限制把 targetSdk 锁死在 28，并因此不再上架 Play 商店。

本项目同样因此把 **targetSdk 固定为 28**，并做了双层保险：

| 场景 | 表现 | 方案 |
| --- | --- | --- |
| 系统允许 exec 数据目录（targetSdk≤28 的正常设备） | 完整 Alpine 环境，含 `apk` 包管理器 | 直接使用 `/data/data/.../rootfs` |
| 设备/ROM 仍然禁止（少数定制 ROM 对 ≤28 也限制） | 仅 shell 可用，无 `apk` | 自动改用随 APK 安装的 `/data/app/.../lib/arm64/libbusybox.so`（系统解压，只读、允许执行），通过 proot `--bind` 挂到 `/bin/busybox` |

终端启动时会打印自检结果，形如：
```
[自检] 静态 busybox : 可执行 ✓
[自检] 动态 busybox : 可执行 ✓
[自检] /bin/sh → busybox.static
```

### 建议安装步骤

1. **先卸载旧版本**（包名相同，覆盖安装会沿用系统已分配的旧 SELinux 域）；
2. **重启一次手机**；
3. 再安装新的 APK。

不卸载/不重启直接覆盖安装，部分机型会保留“禁止执行数据目录”的限制，导致
`execve("/bin/sh"): Permission denied`。

## 参与贡献 / Contributing

- 贡献指南：[CONTRIBUTING.md](./CONTRIBUTING.md)（如何加命令、加 FAQ、代码风格、PR 要求）
- 问题反馈 / 功能建议：用 `.github/ISSUE_TEMPLATE` 里的模板开 issue
- 发布流程：[RELEASE.md](./RELEASE.md)

## 开源协议 / License

本项目源码采用 **GNU General Public License v3.0（GPL-3.0）**，完整协议见 [LICENSE](./LICENSE)。

第三方组件的许可证与源码获取方式汇总在 [THIRD_PARTY_LICENSES.md](./THIRD_PARTY_LICENSES.md)，
同样的文本也随 APK 内置（`assets/THIRD_PARTY_LICENSES.txt`），可在
「外观与关于 → 开源许可 / Licenses」中查看。

| 部分 | 协议 |
| --- | --- |
| 本项目自身源码（Kotlin / C） | GPL-3.0-or-later |
| 内置 Alpine Linux rootfs 中的 busybox | GPL-2.0-only |
| 内置 Alpine Linux rootfs 中的 apk-tools | GPL-2.0 |
| 静态 BusyBox（兜底 shell） | GPL-2.0-only |
| PRoot | GPL-2.0 |
| musl libc | MIT |
| AndroidX / Compose / Material Icons / Commons Compress | Apache-2.0 |

要点说明：

1. 上表中的 GPL 组件都是**独立的可执行程序**，本项目仅 `fork`/`exec` 调用、未与其链接，
   属于 GPLv2 第 2 条末段的「单纯聚合」，因此本项目自身代码采用 GPL-3.0 不冲突。
2. 仓库**不包含任何预编译二进制**。`alpine-aarch64.tar`、`libproot.so`、`busybox-static-aarch64`
   由 `setup-toolchain.sh` 构建时从官方地址下载，并已在 `.gitignore` 中排除。
3. 若你 fork 或二次分发本应用，请保留 `LICENSE`、`THIRD_PARTY_LICENSES.md`
   以及 APK 内的 `THIRD_PARTY_LICENSES.txt`，并保留各源文件顶部的版权头。

---

## 十一、GitHub Actions 自动打包

本仓库配置了 GitHub Actions 自动打包流水线（`.github/workflows/build-apk.yml`），
基于仓库内已有的 **apktool 解包产物**重新构建并签名 APK，无需 Gradle 源码工程：

- **触发时机**：push 到 `master` 自动构建；打 `v*` / `R*` 标签时构建并自动发布 Release（附 APK 资产）。
- **产物**：`dist/linux-starter-release.apk`（arm64-v8a，约 7.5~7.9 MB），同时上传为 Actions artifact。
- **构建原理**：CI 用 apktool 2.9.3 读取解包目录（自动将 `apktool.json` 转为标准 `apktool.yml`），
  完成资源编译、DEX 复用、zipalign 对齐、apksigner 签名（v2/v3）。
- **本地复现**：`ANDROID_BUILD_TOOLS=<build-tools路径> bash scripts/build-apk.sh`。
- **正式签名**：默认使用临时 debug key；如需正式签名，在仓库 Secrets 中配置
  `KEYSTORE`（base64 编码的 .jks/.keystore）、`KEYSTORE_PASS`、`KEYSTORE_ALIAS`、`KEY_PASS` 即可自动启用。

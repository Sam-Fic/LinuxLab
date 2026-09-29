# 第三方组件许可 / Third-party licenses

本应用（Linux 入门 · `com.linuxlab.starter`）**自身源码采用 GNU General Public License v3.0**，
完整协议见仓库根目录的 [LICENSE](./LICENSE)。

App 内置的 Linux 运行环境由下列第三方自由软件组成，它们保持**各自原有的许可证**。

---

## 1. Alpine Linux mini rootfs（内置于 `assets/alpine-aarch64.tar`）

来源：Alpine Linux v3.20.3 aarch64 minirootfs — <https://alpinelinux.org/downloads/>

| 组件 | 许可证 | 源码获取 |
|---|---|---|
| busybox | **GPL-2.0-only** | <https://busybox.net/downloads/> |
| apk-tools | **GPL-2.0** | <https://gitlab.alpinelinux.org/alpine/apk-tools> |
| musl libc | MIT | <https://musl.libc.org/> |
| alpine-baselayout / openrc 等基础包 | GPL-2.0 / MIT / BSD 等 | <https://pkgs.alpinelinux.org/>（按包名查 License 字段） |

## 2. PRoot（`app/src/main/jniLibs/arm64-v8a/libproot.so`，静态可执行文件）

| 组件 | 许可证 | 源码获取 |
|---|---|---|
| PRoot | **GPL-2.0** | <https://github.com/proot-me/proot> |

## 3. 静态 BusyBox（兜底 shell）

| 文件 | 许可证 | 源码获取 |
|---|---|---|
| `assets/busybox-static-aarch64`、`jniLibs/libbusybox.so` | **GPL-2.0-only** | <https://busybox.net/downloads/>（Alpine 包：`busybox-static`） |

## 4. Java / Kotlin 依赖

| 组件 | 许可证 |
|---|---|
| Apache Commons Compress | Apache-2.0 — <https://commons.apache.org/proper/commons-compress/> |
| AndroidX（Core / Lifecycle / Activity / Navigation / Compose / Material3） | Apache-2.0 — <https://developer.android.com/jetpack/androidx> |
| Material Symbols & Icons | Apache-2.0 — <https://fonts.google.com/icons> |
| Kotlin / kotlinx-coroutines | Apache-2.0 — <https://github.com/JetBrains/kotlin> |

## 5. 液态玻璃效果 —— Backdrop（Apache-2.0）

底栏的液态玻璃效果基于 [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)
（Copyright 2025 Kyant，**Apache License 2.0**）。

| 组成 | 引入方式 | 许可证 | 获取 |
|---|---|---|---|
| `io.github.kyant0:backdrop:2.0.1` | Gradle 依赖（Maven Central） | Apache-2.0 | <https://github.com/Kyant0/AndroidLiquidGlass> |
| `io.github.kyant0:shapes:1.2.1` | Gradle 依赖（Maven Central） | Apache-2.0 | <https://github.com/Kyant0/AndroidLiquidGlass> |
| `LiquidBottomTabs.kt`、`LiquidBottomTab.kt`、`DampedDragAnimation.kt`<br>`InteractiveHighlight.kt`、`DragGestureInspector.kt`、`AwaitFrame.kt` | 源码形式引入 | Apache-2.0 | 上游 `app/src/*/kotlin/com/kyant/backdrop/catalog/**` |

引入的源码位于 `app/src/main/kotlin/com/linuxlab/starter/ui/liquidglass/`，每个文件均**保留上游 Apache-2.0 版权头**，
并在头部「修改说明」中列出改动：包名改为本项目包名、仅保留 Android 平台实现、
配色改为由调用方传入以接入 Material 3 动态取色（Monet）。

### 许可证兼容性

- 本项目整体按 **GPL-3.0** 分发；Apache-2.0 经 FSF 认定为**与 GPLv3 兼容**（单向兼容：
  Apache-2.0 代码可以并入 GPLv3 作品，反之不行）。因此合并后的作品以 GPL-3.0 提供，
  Apache-2.0 部分保留其原有许可声明与免责条款。
- 依据 Apache-2.0 第 4 条，本项目已做到：
  1. 随附协议全文 —— 见 [`licenses/Apache-2.0.txt`](./licenses/Apache-2.0.txt)，
     并在 APK 内置的 `assets/THIRD_PARTY_LICENSES.txt` 中收录；
  2. 修改过的文件在头部显著标注了修改内容；
  3. 保留全部版权、专利、商标与归属声明；
  4. 在 [`NOTICE`](./NOTICE) 与本文件中列出上游归属信息。

### 关于代码来源

上游 release APK 经过 R8 混淆（类名与字符串均已重命名，无法通过反编译还原可读源码），
因此本项目的移植**取自上游公开仓库的源码**，而非反编译产物 —— 这既保证了代码可读性，
也保证了上述许可声明可完整追溯。

## 6. 其他

- Material Design 3 规范与动态取色（Monet）由 Android 系统运行时提供，不属于本项目分发内容。

---

## 说明

1. 上表 GPL 组件均为**独立的可执行程序**，本项目仅通过 `fork`/`exec` 调用它们，未与其链接、未修改其代码，
   属于 GPLv2 第 2 条末段所述的「单纯聚合（mere aggregation）」，因此本项目自身代码可以采用 GPL-3.0。
2. 本仓库**不包含任何预编译二进制**。`alpine-aarch64.tar`、`libproot.so`、`busybox-static-aarch64`
   由 [`setup-toolchain.sh`](./setup-toolchain.sh) 在构建时从上述官方地址下载（已列入 `.gitignore`）。
3. 发布的 APK 内附 `assets/THIRD_PARTY_LICENSES.txt`，并在「外观与关于 → 开源许可」中展示，
   以满足 GPL 关于源码可得性的要求。

# 贡献指南 / Contributing

感谢你愿意为「Linux 入门」出力！这是一个面向 Linux 初学者的离线学习 App，
目标是：**讲人话、能复制、能直接在真实终端里跑起来**。

提交任何内容前请先确认一件事：你贡献的代码与内容将以 **GPL-3.0-or-later** 授权发布（见 [LICENSE](./LICENSE)）。

---

## 一、我可以贡献什么

| 类型 | 说明 | 难度 |
| --- | --- | --- |
| 补充/修正命令词条 | 现在有 221 条、12 个分类，缺了常用命令欢迎补 | ⭐ |
| 补充「常见问题排查」 | 现在有 59 条，欢迎补充真实踩坑与解决办法 | ⭐ |
| 勘误 | 说明写错、示例跑不通、翻译不准确 | ⭐ |
| 终端/兼容性问题 | 真实终端依赖 proot + Alpine rootfs，机型差异很大 | ⭐⭐ |
| UI / 交互改进 | Material 3 + 莫奈取色，改动请一并考虑深浅色 | ⭐⭐ |
| 新增功能模块 | 建议先开 issue 讨论，避免白做 | ⭐⭐⭐ |

## 二、本地跑起来

```bash
bash ./setup-toolchain.sh                 # 准备 JDK17 / Android SDK 34+37 / NDK / 原生二进制（幂等）
export JAVA_HOME=${TOOLS_ROOT:-/opt}/jdk
export ANDROID_HOME=${TOOLS_ROOT:-/opt}/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export GRADLE_USER_HOME=${TOOLS_ROOT:-/opt}/gradle-home
./gradlew assembleRelease                 # 产物：app/build/outputs/apk/release/app-release.apk
```

- `setup-toolchain.sh` 会从官方地址下载 Alpine minirootfs、proot、静态 busybox，
  **这些二进制不会进仓库**（已在 `.gitignore` 排除）。
- 只支持 **arm64-v8a**，需要在真机（arm64 Android 8.0+）上安装验证。
- 内存小于 4GB 时脚本会自动加 swap；构建一次约需 2～5 分钟。

## 三、加一条命令

命令内容全部用 DSL 写在 `app/src/main/kotlin/com/linuxlab/starter/data/Cmds*.kt`：

```kotlin
c("ls", "列出目录内容", "List directory contents", "ls [选项] [路径]") {
    detail("一句话讲清它是什么", "One-line English description")
    p("-l", "长格式显示", "Long format")
    e("ls -l", "以长格式列出当前目录", "List in long format")
    tip("容易踩的坑")
    rel("cd", "pwd")          // 相关命令
    level = Level.ADVANCED    // 可选，默认 BASIC
}
```

写完后把新分组登记到 `data/Repository.kt` 的 `rawGroups` 列表里。约定：

1. **中文说明 + 英文说明都要写**，命令、参数、示例原文保持英文；
2. 示例必须是**能直接复制执行的真命令**，不要写伪代码；
3. 涉及 `rm -rf`、`mkfs`、`dd`、`chmod -R 777 /` 之类的破坏性命令，一定要在 `tip` 里写清风险；
4. 不要只翻译 man page —— 说清「什么时候用」比「参数有哪些」更重要。

## 四、加一条常见问题

编辑 `app/src/main/kotlin/com/linuxlab/starter/data/FaqData.kt`：

```kotlin
Faq(
    id = "disk-full",
    categoryId = "disk",
    title = "磁盘满了：No space left on device",
    symptom = "df -h 显示 100%，写入报 No space left on device",
    cause = "日志、缓存、备份或 core dump 占满分区。",
    steps = listOf(
        FaqStep("确认是哪个分区满", "df -h"),
        FaqStep("找出最大的子目录", "du -sh /* 2>/dev/null | sort -h | tail")
    ),
    danger = Danger.CAREFUL   // NONE / CAREFUL / DANGEROUS
)
```

`DANGEROUS` 只给**会丢数据**的操作（重装 GRUB、rm 恢复、递归 chmod 等）；只影响单个服务用 `CAREFUL`。
新增分类请同步加到同文件的 `FaqCategories` 里。

## 五、代码风格

- Kotlin 官方代码风格，4 空格缩进，不开通配符 import；
- **新文件必须带许可证头**：本项目自写代码用仓库里统一的 GPL-3.0 头；
  若引入第三方源码（如 `ui/liquidglass/` 下的 Apache-2.0 代码），**保留原版权头**并在「修改说明」里
  写清改了什么，同时更新 `NOTICE` 与 `THIRD_PARTY_LICENSES.md`（含 APK 内置的 txt）；
- 公开的函数/类写 KDoc 注释，中文即可；
- UI 一律用 Material 3 组件，**不要写死颜色**，一律走 `MaterialTheme.colorScheme`（莫奈取色需要）；
- 新增源文件请保留顶部 GPL 版权头（复制任意现有文件的头即可，改年份和作者）。

## 六、提交 PR

1. fork → 建分支（`feat/xxx`、`fix/xxx`、`content/xxx`）；
2. 保持一个 PR 一件事，尤其是内容类改动请与代码改动分开；
3. 描述里写清：改了什么、如何验证（最好附真机截图）；
4. 确认 `./gradlew assembleRelease` 能过。

## 七、报告问题

请附：**机型 + Android 版本 + 复现步骤 + 截图/日志**。
真实终端相关的问题，麻烦点一下终端右上角的「诊断」按钮，把输出一起贴出来 —— 那里面包含
`uname -a`、`id`、`/bin/sh` 指向、proot 路径、临时目录等关键信息。

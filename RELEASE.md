# 发布流程 / Release Checklist

## 1. 发版前

- [ ] `./gradlew assembleRelease` 通过，产物位于 `app/build/outputs/apk/release/app-release.apk`
- [ ] 真机（arm64）安装验证：速查 / 排错 / 实战终端三个 tab 都能正常打开
- [ ] 真实终端能出现 `root@localhost:~#` 提示符，`apk update` 可联网执行
- [ ] `LICENSE`、`NOTICE`、`THIRD_PARTY_LICENSES.md` 内容无遗漏
- [ ] APK 内确实打包了 `assets/THIRD_PARTY_LICENSES.txt`：
      `unzip -l app-release.apk | grep THIRD_PARTY`
- [ ] 更新 `app/build.gradle.kts` 的 `versionCode` / `versionName`，以及「外观与关于」里的版本号

## 2. 签名

当前仓库的 release 构建用的是 **debug 签名**（`signingConfig = signingConfigs.getByName("debug")`），
仅供自测。正式发布请改成自己的 keystore：

1. 生成 keystore：`keytool -genkey -v -keystore linux-starter.jks -keyalg RSA -keysize 2048 -validity 10000 -alias linux-starter`
2. 在 `app/build.gradle.kts` 里配置 `signingConfigs { create("release") { ... } }`
3. **不要把 keystore、密码提交进仓库**，用 `local.properties`（已在 `.gitignore`）或 CI Secrets 注入。

## 3. 打 tag

```bash
git tag -a v1.7.1 -m "v1.7.1：引导页增加欢迎页"
git push origin v1.7.1
```

## 4. GitHub Release 说明模板

> 复制以下内容作为 Release 描述（按实际改版本号与变更点）：

````markdown
## Linux 入门 v1.7.1

面向 Linux 初学者的离线学习 App：221 条命令速查 + 59 条真实故障排查 + 内置 Alpine Linux 真实终端。

### 主要功能
- 全局搜索 + 12 个分类速查，中英双语，每条命令都带可一键复制的示例
- 常见问题排查：现象 → 原因 → 处理步骤，危险操作带分级警示
- 实战终端：内置 Alpine Linux + proot + 原生 PTY，真 shell、真 `apk` 装包
- Material 3 + 莫奈动态取色，深浅色跟随系统

### 安装须知
1. **先卸载旧版本**（包名相同，覆盖安装会沿用旧的 SELinux 域）；
2. **重启一次手机**；
3. 安装本 APK。

不卸载/不重启直接覆盖，部分机型会保留「禁止执行 App 数据目录」的限制，
表现为 `execve("/bin/sh"): Permission denied`。
本应用 targetSdk 固定为 28，原因见 README 的「安装须知」一节。

### 内置第三方组件许可

本 APK 内包含以下自由软件（完整清单见 APK 内的 `assets/THIRD_PARTY_LICENSES.txt`，
或仓库中的 `THIRD_PARTY_LICENSES.md`）：

| 组件 | 许可 | 源码 |
| --- | --- | --- |
| BusyBox（含静态版本） | GPL-2.0-only | https://busybox.net/downloads/ |
| apk-tools | GPL-2.0 | https://gitlab.alpinelinux.org/alpine/apk-tools |
| PRoot | GPL-2.0 | https://github.com/proot-me/proot |
| musl libc | MIT | https://musl.libc.org/ |
| Alpine Linux minirootfs（v3.20.3 aarch64） | 多种，见上 | https://alpinelinux.org/downloads/ |
| AndroidX / Compose / Material Icons / Commons Compress | Apache-2.0 | 见 NOTICE |

本应用自身源码采用 **GNU General Public License v3.0**，见仓库 `LICENSE` 文件。
````

## 5. 发布后

- [ ] 确认 Release 里的 APK 能被正常下载安装（换一台没装过的手机验证）
- [ ] 在 Release 页面勾选 "Set as the latest release"

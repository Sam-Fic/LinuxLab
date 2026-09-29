package com.linuxlab.starter.data

import com.linuxlab.starter.model.LinkCategory
import com.linuxlab.starter.model.LinkItem

/** 资源导航分类 */
val LinkCategories: List<LinkCategory> = listOf(
    LinkCategory("lookup", "查询与解疑", "Docs & Q&A", "search"),
    LinkCategory("distro", "发行版", "Distributions", "computer"),
    LinkCategory("ops", "运维与基础设施", "Ops & Infra", "misc"),
    LinkCategory("database", "数据库", "Databases", "storage"),
    LinkCategory("dev", "开发与工具", "Dev & Tools", "code")
)

/** 精选外部资源：文档、问答站、知名开源项目官网 */
val Links: List<LinkItem> = listOf(
    // ---------------- 查询与解疑 ----------------
    LinkItem("Arch Wiki", "公认最好的 Linux 文档，查命令、配置、报错的第一站", "https://wiki.archlinux.org/", "lookup"),
    LinkItem("Debian Wiki", "Debian 官方 Wiki，包管理与系统配置资料很全", "https://wiki.debian.org/", "lookup"),
    LinkItem("Ubuntu 官方文档", "Ubuntu 桌面与服务器的官方帮助文档", "https://help.ubuntu.com/", "lookup"),
    LinkItem("Gentoo Wiki", "另一个高质量 Wiki，硬件适配与内核话题尤其详细", "https://wiki.gentoo.org/", "lookup"),
    LinkItem("Linux man 手册", "man7 维护的在线手册页，查命令的权威出处", "https://man7.org/linux/man-pages/index.html", "lookup"),
    LinkItem("TLDP", "Linux 文档工程，经典 HOWTO 与入门指南合集", "https://tldp.org/", "lookup"),
    LinkItem("explainshell", "粘贴一条命令，逐段解释每个参数是什么意思", "https://explainshell.com/", "lookup"),
    LinkItem("commandlinefu", "各种一行流命令片段，学技巧的好地方", "https://www.commandlinefu.com/", "lookup"),
    LinkItem("Stack Overflow", "全球最大的编程问答社区", "https://stackoverflow.com/", "lookup"),
    LinkItem("Super User", "电脑与软件使用类问答", "https://superuser.com/", "lookup"),
    LinkItem("Server Fault", "面向系统管理员的运维问答", "https://serverfault.com/", "lookup"),
    LinkItem("鸟哥的 Linux 私房菜", "中文经典 Linux 入门书，作者站点的在线版", "https://linux.vbird.org/", "lookup"),
    LinkItem("菜鸟教程 · Linux", "中文速查教程，适合零基础对照练习", "https://www.runoob.com/linux/linux-tutorial.html", "lookup"),

    // ---------------- 发行版 ----------------
    LinkItem("Linux 内核官网", "内核源码与版本发布信息", "https://www.kernel.org/", "distro"),
    LinkItem("GNU 工程", "GNU 工具链与自由软件理念的源头", "https://www.gnu.org/", "distro"),
    LinkItem("Alpine Linux", "本应用内置的就是它，轻巧、适合容器与学习", "https://www.alpinelinux.org/", "distro"),
    LinkItem("Debian", "以稳定著称，服务器与众多发行版的上游", "https://www.debian.org/", "distro"),
    LinkItem("Ubuntu", "最流行的桌面/服务器发行版之一", "https://ubuntu.com/", "distro"),
    LinkItem("Fedora", "Red Hat 社区版，新技术最先落地", "https://getfedora.org/", "distro"),
    LinkItem("Arch Linux", "滚动更新、文档极佳，Arch Wiki 的主人", "https://archlinux.org/", "distro"),
    LinkItem("openSUSE", "德国老牌发行版，YaST 管理工具很有名", "https://www.opensuse.org/", "distro"),
    LinkItem("Rocky Linux", "RHEL 的社区复刻，CentOS 的接棒者", "https://rockylinux.org/", "distro"),
    LinkItem("CentOS", "曾经的服务器常客，现已转向 CentOS Stream", "https://www.centos.org/", "distro"),

    // ---------------- 运维与基础设施 ----------------
    LinkItem("Docker", "容器事实标准，打包与运行环境必备", "https://www.docker.com/", "ops"),
    LinkItem("Kubernetes", "容器编排的事实标准", "https://kubernetes.io/", "ops"),
    LinkItem("Nginx", "高性能 Web 服务器与反向代理", "https://nginx.org/", "ops"),
    LinkItem("Apache HTTP Server", "资历最老的 Web 服务器", "https://httpd.apache.org/", "ops"),
    LinkItem("OpenSSH", "SSH 协议实现，远程登录离不开它", "https://www.openssh.com/", "ops"),
    LinkItem("systemd", "现代发行版的 init 系统与服务管理器", "https://systemd.io/", "ops"),
    LinkItem("Ansible", "无 agent 的自动化运维工具", "https://www.ansible.com/", "ops"),
    LinkItem("Prometheus", "云原生监控系统", "https://prometheus.io/", "ops"),
    LinkItem("Grafana", "指标可视化仪表盘", "https://grafana.com/", "ops"),
    LinkItem("Let's Encrypt", "免费 HTTPS 证书颁发机构", "https://letsencrypt.org/", "ops"),
    LinkItem("OpenSSL", "TLS/SSL 与证书工具库", "https://www.openssl.org/", "ops"),
    LinkItem("curl", "命令行数据传输工具，调试接口常用", "https://curl.se/", "ops"),

    // ---------------- 数据库 ----------------
    LinkItem("PostgreSQL", "功能最完整的开源关系型数据库", "https://www.postgresql.org/", "database"),
    LinkItem("MySQL", "使用最广泛的开源数据库之一", "https://www.mysql.com/", "database"),
    LinkItem("MariaDB", "MySQL 的社区分支", "https://mariadb.org/", "database"),
    LinkItem("Redis", "内存键值数据库，缓存与队列常用", "https://redis.io/", "database"),
    LinkItem("MongoDB", "文档型 NoSQL 数据库", "https://www.mongodb.com/", "database"),
    LinkItem("SQLite", "嵌入式数据库，一个文件就是一个库", "https://www.sqlite.org/", "database"),

    // ---------------- 开发与工具 ----------------
    LinkItem("Python", "语法简洁的通用语言，运维脚本首选", "https://www.python.org/", "dev"),
    LinkItem("Node.js", "服务端 JavaScript 运行时", "https://nodejs.org/", "dev"),
    LinkItem("Go", "云原生时代的主力语言", "https://go.dev/", "dev"),
    LinkItem("Rust", "内存安全的系统级语言", "https://www.rust-lang.org/", "dev"),
    LinkItem("OpenJDK", "Java 开源实现", "https://openjdk.org/", "dev"),
    LinkItem("GCC", "GNU 编译器集合", "https://gcc.gnu.org/", "dev"),
    LinkItem("LLVM", "现代编译器基础设施", "https://llvm.org/", "dev"),
    LinkItem("Git", "分布式版本控制系统", "https://git-scm.com/", "dev"),
    LinkItem("GitHub", "全球最大的代码托管平台", "https://github.com/", "dev"),
    LinkItem("GitLab", "可自建的代码托管与 CI 平台", "https://about.gitlab.com/", "dev"),
    LinkItem("Vim", "终端里的经典编辑器", "https://www.vim.org/", "dev"),
    LinkItem("Neovim", "Vim 的现代分支，配置更友好", "https://neovim.io/", "dev"),
    LinkItem("VS Code", "跨平台图形编辑器，插件生态丰富", "https://code.visualstudio.com/", "dev")
)

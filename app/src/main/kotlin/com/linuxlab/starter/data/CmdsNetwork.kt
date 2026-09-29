/*
 * Linux 入门 —— Linux 命令学习与真实终端 App
 * Copyright (C) 2026 拾星*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.linuxlab.starter.data

import com.linuxlab.starter.model.Level

/** 网络 —— 连得上、传得快、排得掉 */
val NetworkGroup = group("net", "网络与远程连接", "Networking & Remote", "language") {

    c("ping", "测试网络连通性", "Send ICMP echo requests to a host", "ping [选项] 主机") {
        detail("排查网络的第一步：先 ping 网关，再 ping 外网，能定位是哪一段断了。", "The first network check: ping your gateway, then the internet, to see where it breaks.")
        p("-c N", "发送 N 次后停止（Linux 不会自动停）", "Stop after N packets")
        p("-i N", "间隔 N 秒发一个包", "Wait N seconds between packets")
        p("-W N", "等待响应的超时秒数", "Time to wait for a response")
        p("-4 / -6", "使用 IPv4 / IPv6", "Use IPv4 / IPv6")
        e("ping -c 4 baidu.com", "测试到外网的连通性", "Check connectivity to the internet")
        e("ping -c 3 192.168.1.1", "测试到路由器的连通性", "Check your gateway")
        tip("ping 不通不一定断网：很多服务器禁用了 ICMP，可改用 curl 或 nc 测试端口。")
        rel("traceroute", "curl", "ip", "dig")
    }

    c("curl", "发送 HTTP 请求 / 下载文件", "Transfer data from or to a server", "curl [选项] URL") {
        detail("调试接口的瑞士军刀：能看响应头、发 POST、带证书、跟随跳转。", "The API Swiss army knife: inspect headers, POST data, use certificates and follow redirects.")
        p("-I", "只查看响应头", "Fetch the headers only")
        p("-o 文件 / -O", "保存为指定文件 / 按原名保存", "Save to a file / save with the remote name")
        p("-L", "跟随 301/302 跳转", "Follow redirects")
        p("-X", "指定请求方法，如 POST / PUT", "Set the request method")
        p("-H", "自定义请求头", "Add a request header")
        p("-d", "发送请求体数据", "Send data in the request body")
        p("-s", "静默模式，不显示进度条", "Silent mode")
        p("-w", "输出格式化信息，如 %{http_code}", "Print formatted info, e.g. %{http_code}")
        e("curl -I https://example.com", "查看响应头（含状态码）", "Inspect response headers")
        e("curl -sL -o install.sh https://example.com/i.sh", "静默下载脚本", "Download a script quietly")
        e("curl -X POST -H 'Content-Type: application/json' -d '{\"a\":1}' api.test/todo", "发送 JSON POST 请求", "Send a JSON POST request")
        e("curl -s -o /dev/null -w '%{http_code}\\n' https://example.com", "只输出 HTTP 状态码", "Print only the HTTP status code")
        rel("wget", "wget2", "ping", "jq", "ssh")
    }

    c("wget", "下载文件（支持断点续传）", "Download files from the web", "wget [选项] URL") {
        p("-c", "断点续传", "Resume a partially downloaded file")
        p("-O 文件", "另存为指定文件名", "Save under a different name")
        p("-b", "后台下载", "Run in the background")
        p("-r", "递归下载（整站镜像）", "Download recursively")
        p("--limit-rate=", "限速，如 200k", "Limit the download rate")
        e("wget https://example.com/big.iso", "下载文件", "Download a file")
        e("wget -c https://example.com/big.iso", "网络中断后继续下载", "Resume an interrupted download")
        e("wget --limit-rate=200k -c file.zip", "限速下载", "Download with a speed limit")
        rel("curl", "scp", "rsync")
    }

    c("ssh", "远程登录服务器", "Log in to a remote machine securely", "ssh [选项] [用户@]主机") {
        detail("运维最常用的命令：加密远程登录，也能用来做端口转发和隧道。", "The sysadmin's daily tool: encrypted remote login, plus port forwarding and tunnels.")
        p("-p 端口", "指定 SSH 端口（默认 22）", "Set the port (default 22)")
        p("-i 私钥", "使用指定密钥登录", "Use a specific private key")
        p("-L", "本地端口转发（本地:远程）", "Local port forwarding")
        p("-R", "远程端口转发", "Remote port forwarding")
        p("-D", "动态转发（SOCKS 代理）", "Dynamic SOCKS proxy")
        p("-v", "显示详细调试信息", "Verbose output for debugging")
        e("ssh alex@192.168.1.10", "以 alex 身份登录服务器", "Log in as alex")
        e("ssh -p 2222 root@server.example.com", "指定端口登录", "Log in on a custom port")
        e("ssh -L 8080:localhost:80 user@server", "把远程 80 端口映射到本地 8080", "Forward a remote port locally")
        e("ssh -i ~/.ssh/id_ed25519 alex@host", "用密钥登录", "Log in with a key")
        tip("配置 ~/.ssh/config 可以给服务器起别名，之后只需 ssh myserver。")
        rel("scp", "ssh-keygen", "rsync", "sftp")
    }

    c("ssh-keygen", "生成 SSH 密钥对", "Generate SSH authentication keys", "ssh-keygen [选项]") {
        detail("生成密钥后用 ssh-copy-id 把公钥传到服务器，即可免密登录（比密码更安全）。", "Generate a key pair, then copy the public key with ssh-copy-id for safer passwordless login.")
        p("-t 类型", "指定算法，推荐 ed25519", "Key type — ed25519 is recommended")
        p("-C 注释", "添加注释，通常写邮箱", "Add a comment, usually your email")
        p("-f 文件", "指定保存路径", "Set the output file")
        p("-p", "修改已有私钥的口令", "Change the passphrase")
        e("ssh-keygen -t ed25519 -C 'me@example.com'", "生成 ed25519 密钥", "Create an ed25519 key")
        e("ssh-copy-id alex@server", "把公钥复制到服务器实现免密登录", "Copy your public key to a server")
        tip("私钥权限必须是 600，否则 SSH 会拒绝使用：chmod 600 ~/.ssh/id_ed25519")
        rel("ssh", "scp", "chmod")
    }

    c("scp", "在本地与服务器间复制文件", "Copy files over SSH", "scp [选项] 源 目标") {
        p("-r", "递归复制整个目录", "Copy directories recursively")
        p("-P 端口", "指定 SSH 端口", "Set the SSH port")
        p("-i 私钥", "使用指定密钥", "Use a specific key")
        p("-C", "传输时压缩", "Enable compression")
        e("scp app.zip alex@server:/tmp/", "把本地文件上传到服务器", "Upload a file to a server")
        e("scp -r alex@server:/var/log/nginx ./logs", "从服务器下载整个日志目录", "Download a log directory")
        tip("大量文件或需要断点续传时，请用 rsync 代替 scp。")
        rel("rsync", "ssh", "cp", "sftp")
    }

    c("rsync", "高效增量同步文件", "Fast, incremental file transfer", "rsync [选项] 源 目标", level = Level.ADVANCED) {
        detail("只传输变化的部分，支持断点续传、保留权限、删除同步，是备份和部署的首选。", "Transfers only what changed; preserves permissions and can mirror deletions — the tool of choice for backups and deploys.")
        p("-a", "归档模式：保留权限、时间、软链接等", "Archive mode: preserve attributes")
        p("-v", "显示详细过程", "Verbose output")
        p("-z", "传输时压缩", "Compress during transfer")
        p("-P", "显示进度并支持断点续传", "Show progress and allow resume")
        p("--delete", "删除目标端多余文件（严格镜像，慎用）", "Delete extraneous files at the destination")
        p("--exclude=", "排除匹配的文件", "Exclude matching files")
        p("-n", "演练：只显示会做什么", "Dry run")
        e("rsync -avP ~/docs/ /mnt/backup/docs/", "把文档增量备份到移动硬盘", "Back up documents incrementally")
        e("rsync -avz --exclude 'node_modules/' ./ user@server:/srv/app/", "部署项目并排除依赖目录", "Deploy while excluding dependencies")
        e("rsync -av --delete src/ dst/", "让 dst 与 src 完全一致（先加 -n 演练）", "Mirror src into dst (dry-run first!)")
        tip("源路径结尾带 / 表示同步目录内的内容，不带 / 会连目录本身一起复制。")
        rel("scp", "cp", "tar", "cpio")
    }

    c("ip", "查看和配置网络接口（推荐）", "Show and manipulate routing, devices and tunnels", "ip [对象] [操作]", level = Level.ADVANCED) {
        detail("ip 命令取代了老的 ifconfig / route / arp，是现代 Linux 的网络配置工具。", "ip replaces the legacy ifconfig, route and arp tools on modern Linux.")
        p("addr / a", "查看 IP 地址", "Show addresses")
        p("link", "查看链路层（网卡）状态", "Show link-layer devices")
        p("route / r", "查看路由表", "Show the routing table")
        p("neigh", "查看 ARP / 邻居表", "Show neighbour (ARP) entries")
        e("ip a", "查看所有网卡和 IP 地址", "Show all interfaces and addresses")
        e("ip route", "查看路由表与默认网关", "Show routes and the default gateway")
        e("sudo ip link set eth0 up", "启用网卡", "Bring an interface up")
        tip("记不住就记 ip a（地址）、ip r（路由）两条，够日常用了。")
        rel("ifconfig", "ss", "ping", "netstat")
    }

    c("ifconfig", "查看网卡信息（传统命令）", "Configure a network interface (legacy)", "ifconfig [接口] [选项]", level = Level.ADVANCED) {
        detail("已被 ip 命令取代，很多最小化系统默认不安装（net-tools 包）。", "Superseded by ip; not installed by default on many minimal systems (package net-tools).")
        p("-a", "显示所有接口（含未启用的）", "Show all interfaces, even down ones")
        p("up / down", "启用 / 停用网卡", "Enable / disable an interface")
        e("ifconfig", "查看已启用网卡的 IP", "Show active interfaces")
        e("sudo apt install net-tools", "安装 ifconfig 等传统工具", "Install the net-tools package")
        rel("ip", "ss", "ping")
    }

    c("ss", "查看端口与连接（推荐）", "Show socket statistics", "ss [选项]", level = Level.ADVANCED) {
        detail("比 netstat 更快更全，用来看「哪个端口被谁占用了」。", "Faster and richer than netstat — the way to ask 'who is using this port?'.")
        p("-t / -u", "只看 TCP / UDP", "Show only TCP / UDP sockets")
        p("-l", "只看监听中的端口", "Show listening sockets only")
        p("-n", "不解析服务名和主机名（更快）", "Do not resolve names")
        p("-p", "显示占用端口的进程", "Show the owning process")
        p("-s", "显示统计摘要", "Show summary statistics")
        e("ss -tlnp", "查看当前监听的 TCP 端口和进程", "List listening TCP ports with processes")
        e("ss -tunp | grep :80", "查看谁占用了 80 端口", "Find who is using port 80")
        e("ss -s", "查看连接数统计", "Show socket statistics summary")
        tip("服务起不来提示端口被占用时，ss -tlnp | grep 端口号 一查便知。")
        rel("netstat", "ip", "lsof", "ps")
    }

    c("netstat", "查看网络连接（传统命令）", "Print network connections and routing tables", "netstat [选项]", level = Level.ADVANCED) {
        p("-tulnp", "查看监听端口与进程", "Show listening ports with processes")
        p("-r", "查看路由表", "Show the routing table")
        p("-i", "查看网卡流量统计", "Show interface statistics")
        e("netstat -tulnp", "查看所有监听端口", "List all listening ports")
        e("sudo apt install net-tools", "安装 netstat", "Install netstat")
        rel("ss", "ip", "lsof")
    }

    c("lsof", "查看进程打开了哪些文件 / 端口", "List open files and the processes that opened them", "lsof [选项]", level = Level.ADVANCED) {
        detail("Linux 里「一切皆文件」，因此 lsof 也能查网络连接、被删除但仍占用的文件。", "Everything is a file on Linux, so lsof also reveals network sockets and deleted-but-held files.")
        p("-i", "查看网络连接", "List network files")
        p("-i :端口", "查看占用某端口的进程", "Find the process using a port")
        p("-u 用户", "查看某用户打开的文件", "Show files opened by a user")
        p("-p PID", "查看某进程打开的文件", "Show files opened by a PID")
        e("sudo lsof -i :8080", "查看 8080 端口被谁占用", "Find what is on port 8080")
        e("sudo lsof -u alex", "查看 alex 打开的所有文件", "Show files opened by alex")
        e("sudo lsof | grep deleted", "找出已删除但仍占空间的文件", "Find deleted files still held open")
        tip("磁盘满了但 du 找不到大文件？多半是已删除却仍被进程占用的日志。")
        rel("ss", "ps", "df", "fuser")
    }

    c("dig", "查询 DNS 解析", "Query DNS name servers", "dig [选项] [域名] [记录类型]", level = Level.ADVANCED) {
        detail("排查域名解析问题的首选，比 nslookup 输出更详细。", "The go-to DNS debugging tool; more detailed than nslookup.")
        p("+short", "只输出解析结果", "Print the answer briefly")
        p("@服务器", "指定 DNS 服务器，如 @8.8.8.8", "Query a specific name server")
        p("+trace", "从根服务器开始追踪解析过程", "Trace the delegation path")
        p("MX / NS / TXT", "查询指定记录类型", "Query a specific record type")
        e("dig example.com +short", "查看域名解析到的 IP", "Show the resolved IP")
        e("dig @8.8.8.8 example.com", "用 Google DNS 解析", "Resolve using Google DNS")
        e("dig example.com MX +short", "查看邮件交换记录", "Show the MX records")
        rel("nslookup", "host", "ping", "curl")
    }

    c("nslookup", "查询域名解析（传统）", "Query DNS interactively", "nslookup [域名] [DNS服务器]") {
        e("nslookup example.com", "查看域名 IP", "Look up a domain")
        e("nslookup example.com 8.8.8.8", "用指定 DNS 查询", "Query a specific DNS server")
        rel("dig", "host", "ping")
    }

    c("host", "简单的 DNS 查询工具", "A simple DNS lookup utility", "host [选项] 域名") {
        p("-a", "查询所有记录", "Show all records")
        e("host example.com", "查看域名 A 记录", "Show the A record")
        e("host -t MX example.com", "查看邮件记录", "Show MX records")
        rel("dig", "nslookup")
    }

    c("traceroute", "追踪数据包的路径", "Trace the route packets take to a host", "traceroute [选项] 主机", level = Level.ADVANCED) {
        detail("显示从本机到目标之间经过了哪些路由节点，用于定位网络卡在哪一跳。", "Shows every hop between you and the target — perfect for finding where the network stalls.")
        p("-n", "不解析主机名（更快）", "Do not resolve hostnames")
        p("-m N", "最多追踪 N 跳", "Set the maximum number of hops")
        e("traceroute example.com", "查看访问目标的完整路径", "Trace the path to a host")
        e("traceroute -n 8.8.8.8", "快速追踪到 Google DNS", "Quickly trace to Google DNS")
        rel("ping", "mtr", "ip", "dig")
    }

    c("nc", "网络调试的「瑞士军刀」", "Netcat — read and write across network connections", "nc [选项] 主机 端口", level = Level.ADVANCED) {
        detail("可以测试端口通不通、临时传文件、甚至搭一个简易聊天服务。", "Test whether a port is open, transfer files ad hoc, or even run a quick chat server.")
        p("-z", "只探测端口是否开放，不发送数据", "Zero-I/O port scanning mode")
        p("-v", "显示详细信息", "Verbose output")
        p("-l", "监听模式（作为服务端）", "Listen mode (act as a server)")
        p("-u", "使用 UDP", "Use UDP instead of TCP")
        e("nc -zv example.com 443", "测试 443 端口是否可达", "Check whether port 443 is open")
        e("nc -l 9999 > received.txt", "在本机 9999 端口接收文件", "Receive a file on port 9999")
        e("echo 'hello' | nc localhost 9999", "向监听端口发送数据", "Send data to a listening port")
        tip("想测试 TCP 连通性又没装 nc 时，可用 bash 的 /dev/tcp：echo > /dev/tcp/host/80")
        rel("telnet", "curl", "ss", "ping")
    }

    c("telnet", "测试 TCP 端口连通性", "Connect to a host over the Telnet protocol", "telnet 主机 端口", level = Level.ADVANCED) {
        detail("明文协议不安全，如今主要用于快速判断端口是否可达。", "Insecure and deprecated for login, but handy as a quick port reachability test.")
        e("telnet example.com 80", "测试 80 端口是否连通", "Test connectivity to port 80")
        e("telnet smtp.example.com 25", "手工测试邮件服务", "Probe an SMTP server by hand")
        rel("nc", "ssh", "ping")
    }

    c("sftp", "交互式安全传输文件", "Secure file transfer over SSH", "sftp [用户@]主机") {
        p("get / put", "下载 / 上传文件", "Download / upload a file")
        p("ls / lls", "查看远程 / 本地目录", "List remote / local directory")
        p("-P 端口", "指定端口", "Set the port")
        e("sftp alex@server", "连接服务器进入交互式传输", "Open an interactive SFTP session")
        e("sftp -P 2222 alex@server:/var/log/syslog ./", "直接下载单个文件", "Download one file directly")
        rel("scp", "rsync", "ssh")
    }

    c("netcat 端口转发示例", "用命令快速搭建调试通道", "Quick one-liners for debugging channels", "nc -l 端口 | ...", level = Level.ADVANCED) {
        detail(
            "把常见的网络调试场景整理在这里：临时传文件、测端口、模拟服务。生产环境请用正式工具。",
            "A few handy one-liners for debugging: ad-hoc file transfer, port checks and fake services. Use proper tools in production."
        )
        e("nc -l 1234 | tar xzvf -", "接收方：在 1234 端口接收并解压", "Receiver: unpack what arrives on port 1234")
        e("tar czvf - dir/ | nc host 1234", "发送方：打包并发送整个目录", "Sender: pack and send a directory")
        e("python3 -m http.server 8000", "临时起一个静态文件服务器", "Serve the current directory over HTTP")
        tip("临时分享目录最简单的办法：python3 -m http.server 8000，然后用浏览器访问。")
        rel("nc", "scp", "rsync", "python3")
    }
}

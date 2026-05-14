# FrpMC

<div align="center">

**Mefrp FRP 管理插件 —— 为 Minecraft Paper 1.21+ 服务器打造**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-brightgreen)
![Paper](https://img.shields.io/badge/Paper-1.21.4-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

</div>

---

## 📖 概述 | Overview

**FrpMC** 是一个 Minecraft Paper 服务器插件，作为 [Mefrp](https://mefrp.com) FRP 服务的客户端，允许服务器管理员直接在游戏内管理 FRP 隧道。

> **作者：** [FZAoao](https://github.com/FZAoao)
>
> **开源许可：** MIT License — 详见下方

---

## ✨ 功能特性 | Features

- 🔐 **登录/注销** — 连接 Mefrp API，支持验证码人机验证
- 🚇 **隧道管理** — 在游戏内创建、删除、启动、停止 FRP 隧道
- 📊 **状态监控** — 实时查看连接状态、用户信息、服务器统计
- 🔄 **自动重连** — 断线后自动尝试重新连接
- 📝 **多语言消息** — 可配置的消息文件，支持自定义颜色
- 🎮 **完整的 Tab 补全** — 所有命令均支持 Tab 补全

---

## 📋 命令 | Commands

| 命令 | 描述 | 权限 |
|------|------|------|
| `/mefrp help` | 显示帮助信息 | `frpmc.command` |
| `/mefrp login <用户名> <密码>` | 登录 Mefrp API | `frpmc.command` |
| `/mefrp logout` | 注销登录 | `frpmc.command` |
| `/mefrp status` | 查看连接状态 | `frpmc.command` |
| `/mefrp tunnel list` | 列出所有隧道 | `frpmc.command` |
| `/mefrp tunnel create <name> <node> <localPort> <remotePort> <type>` | 创建隧道 | `frpmc.command` |
| `/mefrp tunnel delete <id>` | 删除隧道 | `frpmc.command` |
| `/mefrp tunnel start <id>` | 启动隧道 | `frpmc.command` |
| `/mefrp tunnel stop <id>` | 停止隧道 | `frpmc.command` |
| `/mefrp nodes` | 列出可用节点 | `frpmc.command` |
| `/mefrp reload` | 重载配置文件 | `frpmc.admin` |

**别名：** `/frp`, `/mefrpc`

---

## 🔧 安装 | Installation

1. 从 [GitHub Releases](https://github.com/FZAoao/FrpMC/releases) 下载最新版本的 `FrpMC-*.jar`
2. 将 JAR 文件放入服务器的 `plugins/` 目录
3. 重启服务器或使用 `/reload`
4. 编辑 `plugins/FrpMC/config.yml` 配置你的 API 设置
5. 使用 `/mefrp login <用户名> <密码>` 登录 Mefrp API

---

## ⚙️ 配置 | Configuration

### config.yml

```yaml
# API Settings
api:
  base-url: https://api.mefrp.com/api
  user-agent: FrpMC/1.0.0 MinecraftPlugin
  timeout: 10000

# Auto-reconnect settings
auto-reconnect:
  enabled: true
  interval: 30
  max-retries: 5

# Status monitoring
status:
  update-interval: 60
  show-in-chat: true

# Captcha settings (人机验证设置)
captcha:
  client-code: FrpMC
  enabled: true
```

### messages.yml

所有消息文本均可通过 `messages.yml` 自定义，支持 Minecraft 颜色代码（`&` 格式）。

---

## 🔒 权限 | Permissions

| 权限 | 默认 | 描述 |
|------|------|------|
| `frpmc.*` | OP | 所有权限 |
| `frpmc.command` | 所有人 | 基础命令权限 |
| `frpmc.admin` | OP | 管理权限（重载等） |

---

## 🗺️ 路线图 | Roadmap

- [ ] 支持 `/mefrp logout` 命令（代码已实现，当前通过 `/mefrp login` 的子命令未正确路由）
- [ ] 增加隧道编辑功能
- [ ] 更详细的日志记录
- [ ] 多节点支持界面优化
- [ ] 国际化消息完善

---

## 🧑‍💻 开发 | Development

### 构建要求

- Java 21+
- Maven 3.8+

### 构建命令

```bash
mvn clean package
```

构建产物位于 `target/FrpMC-<version>.jar`

---

## 📄 开源许可 | License

```
MIT License

Copyright (c) 2025 FZAoao

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🌐 链接 | Links

- **Mefrp 官网：** https://mefrp.com
- **GitHub 仓库：** https://github.com/FZAoao/FrpMC
- **作者 GitHub：** https://github.com/FZAoao

---

<div align="center">
  <sub>Built with ❤️ by <a href="https://github.com/FZAoao">FZAoao</a> for the Mefrp community</sub>
</div>

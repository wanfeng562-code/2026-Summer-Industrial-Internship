# AI 智能客服工单处理系统

本仓库是课程项目的可继续开发版本，包含 Vue 3 前端、Spring Boot 后端、MySQL 初始化数据，以及团队协作和验收文档。

## 1. 目录说明

| 目录/文件 | 用途 |
| --- | --- |
| `ticket-vue/` | 正式前端，Vue 3 + TypeScript + Element Plus + Pinia |
| `ticketsystem/` | 正式后端，Spring Boot 3 + MyBatis-Plus + Sa-Token + Spring AI Alibaba |
| `springAIDemo/` | 课程中的 Spring AI 学习示例，不作为正式后端启动 |
| `docs/` | 接口说明、成员工作记录和端到端验收清单 |
| `系统开发进度与协作约定.md` | 接口、权限、状态机与当前完成进度 |
| `三人协作任务分工与开发计划.md` | A、B、C 三人的责任边界和模块分工 |

## 2. 环境要求

- JDK 17 或更高版本。
- MySQL 8.x。
- Node.js `22.18.0+` 或 `24.12.0+`。
- npm。后端已带 Maven Wrapper，不要求另装 Maven。
- 使用 AI 功能时，需要可用的阿里云百炼 DashScope API Key。

默认端口：

| 服务 | 地址 |
| --- | --- |
| 后端 | `http://localhost:8080` |
| 前端 | `http://localhost:5173` |
| 前端 API 代理 | `/api` 转发至 `http://localhost:8080` |

## 3. 首次初始化数据库

打开 MySQL 客户端，按顺序执行：

1. `ticketsystem/src/main/resources/static/ticket_system.sql`
2. `ticketsystem/src/main/resources/static/data.sql`
3. `ticketsystem/src/main/resources/static/migrate_full_requirements.sql`
4. 可选：`ticketsystem/src/main/resources/static/seed_rich_demo_data.sql`，在现有数据上追加一批丰富的联调数据。

在仓库根目录打开 MySQL 客户端：

```powershell
mysql --default-character-set=utf8mb4 -u root -p
```

进入 MySQL 提示符后执行：

```sql
source ticketsystem/src/main/resources/static/ticket_system.sql;
source ticketsystem/src/main/resources/static/data.sql;
source ticketsystem/src/main/resources/static/migrate_full_requirements.sql;
source ticketsystem/src/main/resources/static/seed_rich_demo_data.sql;
```

Windows 下应在启动客户端时使用 `--default-character-set=utf8mb4`；只执行 `SET NAMES utf8mb4` 不能改变客户端读取脚本文件时使用的本地代码页。全量需求迁移脚本本身保持 ASCII，并用 UTF-8 十六进制写入中文种子，避免 `source` 再次产生乱码。

如果使用早期课程数据库，不要重新导入全量数据，应先备份，再按实际版本执行：

1. `migrate_legacy_passwords.sql`：把旧演示明文密码迁移为 BCrypt。
2. `migrate_member_c_workflow.sql`：补充工作流、操作日志、FAQ、策略和 SLA 结构。
3. `migrate_full_requirements.sql`：补充账号运维、坐席组、动态分类、归档、满意度、AI 会话和 FAQ 语义配置。该脚本兼容 MySQL 8.0，并可重复执行。
4. `seed_rich_demo_data.sql`：可选增量脚本，不删除或覆盖现有订单、工单；使用独立业务编号，可重复执行。

扩充脚本一次会补充：

- 16 个账号（12 个普通用户、4 个分组客服）；
- 4 个坐席组、2 个动态分类；
- 30 个订单、24 个不同状态的工单；
- 78 条工单消息、87 条操作日志、24 条 AI 处理日志；
- 7 条满意度、10 条 FAQ、4 条策略和 6 个 AI 历史会话。

脚本已经在临时 MySQL 8.0.42 中连续执行两次验证，第二次不会重复插入。它要求先完成 `migrate_full_requirements.sql`；任一语句失败时当前批次会回滚。

## 4. 启动后端

后端运行前必须在本机设置环境变量：

- `DB_PASSWORD`：本地 MySQL 密码。
- `DASHSCOPE_API_KEY`：本地 DashScope Key。
- `DB_URL`：可选，默认连接本机 `ticket_system`。
- `DB_USERNAME`：可选，默认 `root`。
- `DASHSCOPE_MODEL`：可选，默认 `qwen3.7-max`。
- `DASHSCOPE_TEMPERATURE`：可选，默认 `0.7`。

在 IDEA 中打开 `ticketsystem/`，进入 Run/Debug Configuration，在 Environment variables 中配置：

```text
DB_PASSWORD=你的MySQL密码;DASHSCOPE_API_KEY=你的DashScopeKey
```

然后运行 `TicketsystemApplication`。也可以在 PowerShell 中启动：

```powershell
cd .\ticketsystem
$env:DB_PASSWORD = "你的MySQL密码"
$env:DASHSCOPE_API_KEY = "你的DashScopeKey"
.\mvnw.cmd spring-boot:run
```

看到应用监听 `8080` 后，后端启动完成。真实密码和 Key 只能保存在本机环境变量或 IDEA 私有运行配置中，不要写进源码、文档或 Git。

## 5. AI 模型配置

系统当前通过 Spring AI Alibaba Agent Framework `1.1.2.0` 和 DashScope Starter
`1.1.2.2` 调用阿里云百炼，默认配置为：

```properties
spring.ai.dashscope.api-key=${DASHSCOPE_API_KEY}
spring.ai.dashscope.chat.options.model=${DASHSCOPE_MODEL:qwen3.7-max}
spring.ai.dashscope.chat.options.temperature=${DASHSCOPE_TEMPERATURE:0.7}
```

| 配置 | 当前值 | 作用 |
| --- | --- | --- |
| 提供方 | 阿里云百炼 DashScope | 提供模型推理服务 |
| 默认模型 | `qwen3.7-max` | 普通客服对话、流式对话、工单分类与回复、只读工具调用 |
| 温度 | `0.7` | 在稳定回答和自然表达之间取平衡 |
| 系统提示词 | `AiConfig.java` | 限定中文客服身份、安全边界和回复风格 |
| 工具 | `TicketAiTools.java` | 只读查询本人全部订单/工单，或按 `ORD...`、`TK...`、数字工单 ID 精确查询；也可查询策略和 FAQ |

`qwen3.7-max` 是模型别名，平台可能在未来将别名指向更新版本。需要固定答辩结果时，可在 IDEA 运行配置中增加快照模型，例如：

```text
DASHSCOPE_MODEL=qwen3.7-max-2026-06-08
```

需要换模型时优先修改本机 `DASHSCOPE_MODEL`，不要修改或提交真实 Key。所选模型必须在当前百炼账号、业务空间和地域中可用，并支持 Function Calling；模型调用可能消耗免费额度或产生费用。

模型实际用于以下接口和流程：

- `POST /ai/chat`：普通 AI 客服对话。
- `POST /ai/chat/stream`：SSE 流式对话。
- 创建工单时的自动分类和首次回复。
- `AI_PROCESSING` 阶段用户补充消息后的 AI 回复。
- 经后端权限校验的只读 Tool Calling。

## 6. 启动前端

另开一个终端：

```powershell
cd .\ticket-vue
npm ci
npm run dev
```

浏览器打开 `http://localhost:5173`。开发环境下浏览器只请求 `/api`，Vite 会自动代理到后端，无需手工修改 Axios 地址。

生产构建检查：

```powershell
npm run build
```

## 7. 演示账号

所有演示账号的密码均为 `123456`，数据库仅保存 BCrypt 哈希。

| 角色 | 账号 | 主要操作 |
| --- | --- | --- |
| 管理员 | `admin` | 查看全量统计和用户、管理售后策略/FAQ、分配或关闭工单 |
| 客服 | `agent_zhang`、`agent_li` | 查看待处理工单、接单、回复、解决和关闭 |
| 普通用户 | `user_wang`、`user_liu`、`user_chen` | 查看自己的订单/工单、创建工单、补充消息、使用 AI 客服 |
| 扩充客服 | `demo_2026_agent_refund`、`demo_2026_agent_logistics`、`demo_2026_agent_quality`、`demo_2026_agent_general` | 验证四个坐席组的数据范围、组长报表和接单 |
| 扩充用户 | `demo_2026_chenxi`、`demo_2026_linan`、`demo_2026_zhouyu` 等 12 个账号 | 验证丰富订单、工单状态、满意度和 AI 会话 |

扩充账号密码同样为 `123456`。扩充数据统一使用 `demo_2026_*`、`DEMO2026*`、`DEMO-TK-*` 和 `DEMOCHAT2026*` 前缀，便于查询和与真实数据区分。

## 8. 推荐演示流程

1. 使用 `user_wang` 登录，在“订单管理”选择自己的订单并创建工单。
2. 在工单详情查看 AI 回复；需要人工时点击“转人工客服”。
3. 退出后使用 `agent_zhang` 登录，在“工单管理”接单、回复并标记解决。
4. 将已解决工单关闭，再用 `user_wang` 查看最终状态和完整消息。
5. 使用 `admin` 登录，查看工作台真实统计、“用户管理”和“售后策略”页面。
6. 在“AI 客服”验证普通/流式回复、停止生成和创建工单入口。

AI 客服支持“列出我的全部订单和工单”、按订单号查询订单，以及按工单号或数字 ID 查询工单。模型不会直接连接数据库或执行 SQL；后端只读工具使用当前登录用户身份过滤数据。离开 AI 页面再返回时会自动恢复最近会话，模型回答时会携带本会话最近 12 条消息。

工单主流程为：

```text
AI_PROCESSING -> MANUAL_REVIEW -> RESOLVED -> CLOSED
```

用户可在 `RESOLVED` 状态补充问题，工单会回到 `MANUAL_REVIEW`。已关闭工单不能继续发送消息。

## 9. 构建、测试与验收

后端自动化测试：

```powershell
cd .\ticketsystem
.\mvnw.cmd test
```

前端类型检查和生产构建：

```powershell
cd .\ticket-vue
npm run build
```

真实三角色和 AI 验收按 `docs/成员C端到端验收清单.md` 执行。自动化测试不会连接开发数据库，也不会真实调用 DashScope。

当前最新离线验证结果：后端 `.\mvnw.cmd clean test` 共 71 项全部通过，前端 `npm run build` 通过。已知缺陷、逻辑漏洞、修复说明和仍需真实环境验证的边界见 `docs/缺陷修复与逻辑漏洞审计记录.md`。

后端运行后可执行核心 API 冒烟流程：

```powershell
.\scripts\e2e-core.ps1
.\scripts\e2e-core.ps1 -TestAi
```

## 10. 常见问题

- **前端提示网络异常**：确认后端已监听 `8080`，前端是通过 `npm run dev` 启动且地址为 `5173`。
- **后端启动时报数据库错误**：确认 MySQL 已启动、数据库脚本已导入、`DB_PASSWORD`/`DB_URL` 正确。
- **后端提示缺少环境变量**：在当前启动方式对应的位置设置变量；IDEA 与 PowerShell 的环境变量不会自动互通。
- **AI 返回服务不可用**：检查 `DASHSCOPE_API_KEY`、百炼模型权限和本机网络；不要把 Key 发到聊天或提交到仓库。
- **AI 页面一直显示“正在思考”**：先确认 IDEA 已重启并加载最新后端；当前版本为 SSE 增加了后端 60 秒、前端 70 秒超时和可见错误提示。控制台中周期性的 SLA `SELECT` 日志与 AI 请求无关。
- **模型不存在或无权限**：确认 `DASHSCOPE_MODEL` 在当前百炼账号、业务空间和 Key 所属地域可用；未配置时使用 `qwen3.7-max`。
- **npm 发出 engine 警告**：升级到 Node.js `22.18.0+` 或 `24.12.0+` 后删除本地依赖并重新执行 `npm ci`。
- **401/403**：401 表示未登录或 Token 过期；403 表示当前角色或数据归属不允许该操作。

## 11. 相关文档

- `系统开发进度与协作约定.md`
- `三人协作任务分工与开发计划.md`
- `docs/成员A认证订单接口说明.md`
- `docs/成员B前端交付与联调说明.md`
- `docs/成员C工单策略AI接口说明.md`
- `docs/成员C端到端验收清单.md`
- `docs/缺陷修复与逻辑漏洞审计记录.md`
- `docs/丰富演示数据说明.md`

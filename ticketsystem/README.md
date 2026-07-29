# 工单管理系统后端

本目录以老师 2026-07-28 的后端快照为基线。当前已落地成员 A 的认证、权限和订单数据范围，以及成员 C 的工单状态机、操作日志、策略/SLA、FAQ、受控 AI 上下文、普通对话与 SSE 接口。

## 1. 运行环境

- JDK 17 或更高版本（项目按 Java 17 编译）
- MySQL 8.x
- 端口 `8080`
- Maven Wrapper（无需单独安装 Maven）

## 2. 初始化数据库

全新环境按顺序执行：

1. `src/main/resources/static/ticket_system.sql`：创建 `ticket_system` 库和表。
2. `src/main/resources/static/data.sql`：导入演示账号、订单、工单和策略。
3. `src/main/resources/static/migrate_full_requirements.sql`：补充账号运维、坐席组、动态分类、归档、满意度、AI 会话和语义检索配置。
4. 可选执行 `src/main/resources/static/seed_rich_demo_data.sql`：只在现有库上追加丰富演示数据。

Windows MySQL 命令行应使用 `mysql --default-character-set=utf8mb4 -u root -p` 启动。只运行 `SET NAMES utf8mb4` 不能修正客户端读取 UTF-8 脚本时的本地代码页问题。

已有老师早期数据库且账号密码仍为明文 `123456` 时，只执行
`src/main/resources/static/migrate_legacy_passwords.sql`。在已有 7-28/A 数据库上继续开发时，再执行
`src/main/resources/static/migrate_member_c_workflow.sql`；随后执行 `migrate_full_requirements.sql`。两者均保留现有业务数据，后者采用 MySQL 8.0 兼容的字段存在性检查并可重复执行。

如需让列表、工作台和报表更充实，在迁移完成后执行：

```sql
source ticketsystem/src/main/resources/static/seed_rich_demo_data.sql;
```

该脚本新增 16 个账号、30 个订单、24 个工单，并配套消息、操作日志、AI 处理日志、满意度、FAQ、策略、坐席组、动态分类和 AI 历史会话。脚本不会 `DROP`、`TRUNCATE` 或删除业务表，使用独立前缀和存在性检查，重复执行不会重复写入；SQL 异常会回滚当前批次。已在临时 MySQL 8.0.42 中完成两次连续导入验证。

演示账号密码均为 `123456`：

| 角色 | 账号 |
| --- | --- |
| 管理员 | `admin` |
| 客服 | `agent_zhang`、`agent_li` |
| 用户 | `user_wang`、`user_liu`、`user_chen` |
| 扩充客服 | `demo_2026_agent_refund`、`demo_2026_agent_logistics`、`demo_2026_agent_quality`、`demo_2026_agent_general` |
| 扩充用户 | `demo_2026_chenxi`、`demo_2026_linan`、`demo_2026_zhouyu` 等 12 个账号 |

数据库中保存的是 BCrypt 哈希，不保存明文密码。

## 3. 配置环境变量

必须配置：

- `DB_PASSWORD`：本地 MySQL 密码。
- `DASHSCOPE_API_KEY`：阿里云百炼 DashScope Key。AI Bean 会在应用启动时读取；真实 Key 不得写入配置文件或提交 Git。

可选配置：

- `DB_URL`：默认
  `jdbc:mysql://localhost:3306/ticket_system?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai`
- `DB_USERNAME`：默认 `root`
- `DASHSCOPE_MODEL`：默认 `qwen3.7-max`
- `DASHSCOPE_TEMPERATURE`：默认 `0.7`

在 IDEA 的 Run/Debug Configuration → Environment variables 中填写，例如：

```text
DB_PASSWORD=你的本地数据库密码;DASHSCOPE_API_KEY=你的DashScopeKey
```

PowerShell 临时启动示例：

```powershell
$env:DB_PASSWORD = "你的本地数据库密码"
$env:DASHSCOPE_API_KEY = "你的DashScopeKey"
.\mvnw.cmd spring-boot:run
```

不要创建或提交包含真实密钥的 `.env` 文件。

## 4. 模型与 Spring AI 配置

当前使用：

- Spring AI Alibaba Agent Framework：`1.1.2.0`
- DashScope Starter：`1.1.2.2`
- 默认模型：`qwen3.7-max`
- 默认温度：`0.7`

`application.properties` 使用环境变量覆盖模型参数：

```properties
spring.ai.dashscope.api-key=${DASHSCOPE_API_KEY}
spring.ai.dashscope.chat.options.model=${DASHSCOPE_MODEL:qwen3.7-max}
spring.ai.dashscope.chat.options.temperature=${DASHSCOPE_TEMPERATURE:0.7}
```

IDEA 中仅设置 `DB_PASSWORD`、`DASHSCOPE_API_KEY` 即可使用默认模型。若要临时换成固定快照，可在运行配置中增加：

```text
DASHSCOPE_MODEL=qwen3.7-max-2026-06-08
```

`AiConfig.java` 定义客服系统提示词并注册 `TicketAiTools`。工具支持查询当前登录人有权访问的全部订单/工单、按订单号查询订单，以及按工单号或数字 ID 查询工单。请求线程捕获的可信用户 ID 通过 Spring AI `ToolContext` 传入工具，避免流式线程切换后丢失登录身份。工具只提供受后端权限约束的只读查询；模型输出不能直接退款、修改订单、改变工单状态或写数据库。

模型用于普通对话、SSE 流式回复、工单分类、工单 AI 回复和只读 Function Calling。更换模型前应确认该模型在当前百炼账号、业务空间和 Key 所属地域可用，并支持 Function Calling。模型调用可能消耗额度或产生费用。

SSE 接口使用 Spring MVC 原生 `ServerSentEvent` 输出 `message`、`done`、`error`
事件，统一按 UTF-8 编码，并设置 60 秒模型响应超时；前端等待上限为
70 秒。Sa-Token 只在首次请求派发时鉴权，SSE 结束后的异步收尾不会重复访问已释放的请求上下文。修改后端依赖、控制器或环境变量后必须重启 IDEA 中的 Spring Boot 进程，浏览器中的旧请求不会自动切换到新后端代码。

## 5. 构建与测试

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

当前自动化测试不连接 MySQL，也不调用 DashScope，可重复运行且不会修改开发数据。真实模型验收需要本地环境变量和可用网络。

## 6. 前后端联调

后端地址为 `http://localhost:8080`。浏览器端统一访问 Vite 代理前缀 `/api`，由前端转发至后端。除登录和注册外，请求头必须携带：

```text
Authorization: Bearer <token>
```

统一响应：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

接口说明：

- `docs/成员A认证订单接口说明.md`
- `docs/成员C工单策略AI接口说明.md`

可导入的认证/订单请求集合见 `docs/postman/成员A认证订单接口.postman_collection.json`。

扩充数据的账号、数量、推荐用例和核对 SQL 见 `docs/丰富演示数据说明.md`。

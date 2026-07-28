# 工单系统后端启动说明（smart-ticket-system/backend）

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x
- 可选：环境变量 `aliQwen-api`（阿里百炼 API Key，AI 功能需要）

## 数据库初始化

1. 执行 `src/main/resources/static/ticket_system.sql` 建库建表
2. 执行 `src/main/resources/static/data.sql` 导入演示数据
3. 按需通过环境变量覆盖数据库连接（默认 `root/root` @ `localhost:3306/ticket_system`）：

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/ticket_system?...` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `root` |
| `aliQwen-api` | 百炼 API Key | 无（AI 功能需要） |

演示账号（密码均为 `123456`）：

| 用户名 | 角色 | 说明 |
| --- | --- | --- |
| admin | ADMIN | 管理员 |
| agent_zhang | AGENT | 客服 |
| user_wang | USER | 普通用户 |

启动后 `PasswordMigrationRunner` 会自动把明文密码迁移为 BCrypt。

## 启动

```bash
./mvnw spring-boot:run
```

Windows：

```bash
mvnw.cmd spring-boot:run
```

服务默认端口：`8080`

## 认证约定

- 登录：`POST /user/login`，JSON：`{"username":"user_wang","password":"123456"}`
- 请求头：`Authorization: Bearer <token>`
- 统一响应：`{ "code": 200, "msg": "...", "data": ... }`
- 错误码：400 参数错误，401 未登录，403 无权限，404 不存在，500 服务器错误

更多接口见 `docs/API.md`，Postman 集合见 `docs/TicketSystem.postman_collection.json`。

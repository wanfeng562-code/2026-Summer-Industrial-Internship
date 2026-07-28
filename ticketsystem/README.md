# 工单管理系统后端

本目录以老师 2026-07-28 的后端快照为基线。当前已落地成员 A 负责的统一响应、账号认证、角色权限、订单数据范围和创建工单前的订单归属校验。

## 1. 运行环境

- JDK 17 或更高版本（项目按 Java 17 编译）
- MySQL 8.x
- 端口 `8080`
- Maven Wrapper（无需单独安装 Maven）

## 2. 初始化数据库

全新环境按顺序执行：

1. `src/main/resources/static/ticket_system.sql`：创建 `ticket_system` 库和表。
2. `src/main/resources/static/data.sql`：导入演示账号、订单、工单和策略。

已有老师早期数据库且账号密码仍为明文 `123456` 时，只执行
`src/main/resources/static/migrate_legacy_passwords.sql`。该脚本仅迁移指定演示账号，不覆盖已经修改的密码。

演示账号密码均为 `123456`：

| 角色 | 账号 |
| --- | --- |
| 管理员 | `admin` |
| 客服 | `agent_zhang`、`agent_li` |
| 用户 | `user_wang`、`user_liu`、`user_chen` |

数据库中保存的是 BCrypt 哈希，不保存明文密码。

## 3. 配置环境变量

必须配置：

- `DB_PASSWORD`：本地 MySQL 密码。
- `DASHSCOPE_API_KEY`：阿里云百炼 DashScope Key。AI Bean 会在应用启动时读取；真实 Key 不得写入配置文件或提交 Git。

可选配置：

- `DB_URL`：默认
  `jdbc:mysql://localhost:3306/ticket_system?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai`
- `DB_USERNAME`：默认 `root`

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

## 4. 构建与测试

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

当前成员 A 的单元测试不连接 MySQL，也不调用 DashScope，可重复运行且不会修改开发数据。

## 5. 前后端联调

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

接口说明见仓库根目录 `docs/成员A认证订单接口说明.md`，可导入的请求集合见
`docs/postman/成员A认证订单接口.postman_collection.json`。

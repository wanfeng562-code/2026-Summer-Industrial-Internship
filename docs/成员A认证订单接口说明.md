# 成员 A：认证、账号、订单与归属接口说明

> 基线：2026-07-28
>
> 后端根地址：`http://localhost:8080`
>
> 前端开发环境：统一请求 `/api`，由 Vite 代理到后端
>
> 鉴权请求头：`Authorization: Bearer <token>`

## 1. 通用约定

所有响应使用：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

HTTP 状态与响应 `code` 一致：参数错误 `400`、未登录/Token 失效 `401`、权限或数据归属不足 `403`、资源不存在 `404`、重复或状态冲突 `409`、服务端异常 `500`。

字段校验错误示例：

```json
{
  "code": 400,
  "msg": "参数校验失败",
  "data": {
    "fieldErrors": {
      "username": "用户名不能为空"
    }
  }
}
```

角色权限摘要：

| 角色 | 账号/订单能力 |
| --- | --- |
| `USER` | 查询本人订单和工单、为本人订单创建工单、补充本人未关闭工单消息 |
| `AGENT` | 不访问用户订单；查询可接或已接工单，并处理分配给自己的工单 |
| `ADMIN` | 查询全部订单和用户，拥有管理权限 |

## 2. 认证与账号

### 2.1 登录

`POST /user/login`，无需 Token。

```json
{
  "username": "user_wang",
  "password": "123456"
}
```

成功响应的 `data`：

```json
{
  "id": 4,
  "userId": 4,
  "username": "user_wang",
  "nickname": "王小明",
  "token": "token-value",
  "role": "USER",
  "roles": ["USER"],
  "permissions": ["order:query", "ticket:query", "ticket:add", "ticket:message"]
}
```

`id`、`role` 是正式字段；`userId`、`roles` 暂时保留以兼容老师 7-28 前端。用户名或密码错误返回 `401`。

### 2.2 注册

`POST /user/register`，无需 Token。注册角色固定为 `USER`，客户端不能指定角色。

```json
{
  "username": "new_user",
  "password": "123456",
  "nickname": "新用户",
  "email": "new_user@example.com",
  "phone": "13900000000"
}
```

用户名重复返回 `409`。密码为 6–32 个字符，写库前使用 BCrypt。

### 2.3 退出

正式接口：`POST /user/logout`。为兼容 7-28 前端，当前也接受 `GET /user/logout`。需要 Token。

### 2.4 查询个人资料

`GET /user/profile`，需要 Token。

返回字段包括 `id`、`username`、`nickname`、`email`、`phone`、`avatar`、`role`、`reputationScore`、`createTime`、`updateTime`，不返回密码。

### 2.5 修改个人资料

`PUT /user/profile`，需要 Token。

```json
{
  "nickname": "新的昵称",
  "email": "new@example.com",
  "phone": "13900000000",
  "avatar": "https://example.com/avatar.png"
}
```

用户名、角色和信誉分不能通过该接口修改。

### 2.6 用户分页

`GET /users?current=1&size=10&role=USER`，仅 `ADMIN`（权限 `user:manage`）。

- `current` 从 1 开始。
- `size` 为 1–100。
- `role` 可选：`USER`、`AGENT`、`ADMIN`。

## 3. 订单

### 3.1 订单分页

正式接口：`GET /orders?current=1&size=10`。

- `USER` 只返回自己的订单。
- `ADMIN` 返回全部订单。
- `AGENT` 返回 `403`。
- 兼容旧前端的 `GET /orders/{page}/{pageSize}` 暂时保留。

分页 `data` 固定包含：

```json
{
  "current": 1,
  "size": 10,
  "total": 2,
  "records": []
}
```

### 3.2 订单详情

`GET /orders/{id}`。

普通用户访问他人订单返回 `403`；订单不存在或已逻辑删除返回 `404`。管理员可查看任意有效订单，客服不可查看订单详情。

## 4. 与工单模块的接口落点

### 4.1 创建工单

`POST /tickets`，权限 `ticket:add`，仅普通用户可调用。

```json
{
  "orderId": 1,
  "title": "商品存在质量问题",
  "description": "收到商品后发现屏幕有明显划痕，希望处理。",
  "category": "DAMAGE",
  "priority": "HIGH"
}
```

- 正式字段为 `orderId`；后端暂时兼容旧字段 `ordersId`。
- 后端从 Token 获取当前用户，不接受客户端提交 `userId`、`agentId` 或 `status`。
- 订单不存在返回 `404`；订单属于其他用户返回 `403`。
- 初始状态为 `AI_PROCESSING`；分类缺省为 `OTHER`，优先级缺省为 `MEDIUM`。

### 4.2 补充工单消息

`POST /tickets/{ticketId}/messages`，请求体只传消息内容：

```json
{
  "content": "这里补充一张商品破损照片。"
}
```

工单 ID 只从路径读取。用户只能补充自己的未关闭工单；客服只能回复分配给自己的工单；管理员可按权限访问。`CLOSED` 工单返回 `409`。

## 5. 联调异常用例

成员 B、C 联调时至少验证：

1. 不带 Token 查询订单，返回 HTTP/业务码 `401`。
2. 普通用户访问他人订单，返回 `403`。
3. 普通用户以他人 `orderId` 创建工单，返回 `403`。
4. 客服查询订单，返回 `403`。
5. 管理员查询全部订单和用户分页成功。
6. 注册重复用户名返回 `409`。
7. 空字段或非法枚举返回 `400` 且字段错误位于 `data.fieldErrors`。

可直接导入 `docs/postman/成员A认证订单接口.postman_collection.json`。登录请求会自动保存集合变量 `token`。

# 接口文档（成员 A 交付）

统一响应：

```json
{ "code": 200, "msg": "成功", "data": {} }
```

认证：除登录/注册外，请求头携带 `Authorization: Bearer <token>`。

## 认证与用户

### POST /user/login

```json
{ "username": "user_wang", "password": "123456" }
```

成功 `data`：`userId`、`username`、`nickname`、`token`、`roles`、`permissions`。

### POST /user/register

```json
{ "username": "new_user", "password": "123456", "nickname": "新用户", "email": "", "phone": "" }
```

### GET /user/logout

退出当前登录。

### GET /user/profile

查询当前用户资料。

### PUT /user/profile

```json
{ "nickname": "新昵称", "email": "a@b.com", "phone": "13900000000", "password": "可选新密码" }
```

## 订单

### GET /orders/{page}/{pageSize}

用户仅返回自己的订单；客服/管理员返回全量。`data` 为 MyBatis-Plus `Page<Orders>`（含 `records/total/current/size`）。

### GET /orders/detail/{id}

订单详情。普通用户访问他人订单返回 403。

## 工单（与 C 协作部分）

### GET /tickets?current=&size=

用户仅返回自己的工单；客服/管理员返回全量。

### GET /tickets/{id}

工单详情（含消息列表）。普通用户访问他人工单返回 403。

### POST /tickets

```json
{
  "ordersId": 1,
  "title": "物流异常",
  "description": "包裹两天未更新",
  "category": "LOGISTICS"
}
```

约束：`ordersId` 必须存在且属于当前用户，否则 403/404。

### POST /tickets/{id}/messages

```json
{ "ticketId": 1, "content": "补充说明" }
```

### PUT /tickets/{id}

```json
{ "status": "RESOLVED", "category": null, "priority": null, "agentId": 2 }
```

## 异常示例

| 场景 | HTTP | code | msg |
| --- | --- | --- | --- |
| 未登录 | 401 | 401 | 未登录或登录已失效 |
| 无权限 | 403 | 403 | 权限不足，禁止访问 |
| 参数校验失败 | 400 | 400 | 参数校验异常 |
| 业务错误 | 400 | 400 | 具体业务提示 |

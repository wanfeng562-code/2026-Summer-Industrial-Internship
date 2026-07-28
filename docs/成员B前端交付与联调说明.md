# 成员 B 前端交付与联调说明

## 1. 启动与构建

```powershell
cd ticket-vue
npm ci
npm run dev
```

生产验证：

```powershell
npm run build
```

开发服务器通过 `/api` 代理至 `http://localhost:8080`。登录后所有 Axios 请求自动携带：

```text
Authorization: Bearer <token>
```

## 2. 页面路由与角色

| 路径 | 页面 | USER | AGENT | ADMIN |
| --- | --- | --- | --- | --- |
| `/login`、`/register` | 登录/注册 | 公开 | 公开 | 公开 |
| `/home` | 工作台 | 是 | 是 | 是 |
| `/home/tickets` | 工单列表 | 本人工单 | 已接单/待接工单 | 全量 |
| `/home/tickets/:id` | 工单详情 | 本人工单 | 有权处理工单 | 全量 |
| `/home/create` | 创建工单 | 是 | 否 | 否 |
| `/home/orders` | 订单列表 | 本人 | 否 | 全量 |
| `/home/orders/:id` | 订单详情 | 本人 | 否 | 全量 |
| `/home/policies` | 策略与 FAQ | 否 | 否 | 是 |
| `/home/chat` | AI 客服 | 是 | 是 | 是 |

菜单隐藏只是体验控制，真实安全边界仍由后端权限和数据范围校验。

## 3. 登录态和异常规则

- Pinia 持久化 `userId`、`username`、`nickname`、`token`、`roles`、`permissions`。
- 未登录访问业务路由时跳转 `/login?redirect=原路径`。
- 已登录访问登录/注册页时返回工作台。
- 401 或统一响应 `code=401` 时清空登录态并跳转登录页。
- 400、403、404、409、500、503 优先显示后端 `msg`。
- 退出时即使后端请求失败，也会清理本地 Token。

## 4. 前后端正式契约

- 订单分页：`GET /orders?current=1&size=10`
- 订单详情：`GET /orders/{id}`
- 工单分页：`GET /tickets?current=1&size=10`
- 创建工单：`POST /tickets`，字段为 `orderId`、`title`、`description`、可选 `category`
- 工单消息和状态动作使用 C 的专用接口，不调用通用工单更新。
- 分页统一读取 `data.current`、`data.size`、`data.total`、`data.records`。

## 5. 可复用组件

- `ErrorState.vue`：加载失败提示和重试事件。
- `MessageBubble.vue`：USER、AGENT、AI、SYSTEM 消息的统一展示。
- `Aside.vue`：按角色生成侧边菜单。
- `Header.vue`：面包屑、当前角色/用户和退出入口。

## 6. 联调检查

- [x] `npm ci` 成功，0 vulnerabilities。
- [x] `npm run build` 成功。
- [x] C 的详情、策略、AI 页面保留并通过类型检查。
- [ ] 本地后端启动后完成 USER 登录、订单、创建工单。
- [ ] 完成 AGENT 接单、回复、解决、关闭。
- [ ] 完成 ADMIN 全量订单、分配工单、策略/FAQ 管理。
- [ ] 验证 401、403、409、503 页面提示。

实际业务验收结果统一记录到 `docs/成员C端到端验收清单.md`。

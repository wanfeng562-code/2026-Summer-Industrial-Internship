# 工单系统前端

正式的完整启动、演示账号和使用说明见仓库根目录 `README.md`。

前端要求 Node.js `22.18.0+` 或 `24.12.0+`。开发启动：

```powershell
npm ci
npm run dev
```

浏览器访问 `http://localhost:5173`。Vite 会将 `/api` 请求代理到
`http://localhost:8080`，因此需要先启动 `ticketsystem/` 后端。

类型检查和生产构建：

```powershell
npm run build
```

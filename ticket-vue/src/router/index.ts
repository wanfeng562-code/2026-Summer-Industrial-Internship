import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path : "/login",
      name:"login",
      component: ()=> import("../views/login/LoginView.vue")
    },
    {
      path : "/register",
      name:"register",
      component: ()=> import("../views/login/RegisterView.vue")
    },
    {
      path : "/home",
      name:"home",
      component: ()=> import("../views/home/Home.vue"),
      children:[
        {
          path : "",
          name:"index",
          component: ()=> import("../views/home/Index.vue"),   //首页页面
          meta:{"title" : "工作台"}
        },
        {
          path : "tickets",
          name:"tickets",
          component: ()=> import("../views/tickets/TicketView.vue"),   //工单管理
          meta:{"title" : "工单管理"}
        },
        {
          path : "create",
          name:"create",
          component: ()=> import("../views/tickets/CreateTicket.vue"),   //创建工单
          meta:{"title" : "创建工单"}
        },
        {
          path : "tickets/:id",
          name:"detail",
          component: ()=> import("../views/tickets/TicketDetail.vue"),   //工单详情
          meta:{"title" : "工单详情"}
        },
        {
          path : "chat",
          name:"ai-chat",
          component: ()=> import("../views/ai/ChatView.vue"),
          meta:{"title" : "AI客服"}
        },
        {
          path : "policies",
          name:"policies",
          component: ()=> import("../views/policies/PolicyView.vue"),
          meta:{"title" : "售后策略", "roles": ["ADMIN"]}
        },
      ]
    },
    {
      path:"/",
      redirect: "/login"   //页面重定向
    }
  ],
})

export default router

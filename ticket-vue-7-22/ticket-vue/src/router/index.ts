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
          component: ()=> import("../views/home/Index.vue")   //首页页面
        },
        {
          path : "tickets",
          name:"tickets",
          component: ()=> import("../views/tickets/TicketView.vue")   //工单管理
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

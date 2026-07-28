import { createRouter, createWebHistory } from 'vue-router'
import { useUserInfoStore } from '@/stores/userInfo'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/login/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/login/RegisterView.vue'),
      meta: { public: true },
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('../views/home/Home.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'index',
          component: () => import('../views/home/Index.vue'),
          meta: { title: '工作台' },
        },
        {
          path: 'tickets',
          name: 'tickets',
          component: () => import('../views/tickets/TicketView.vue'),
          meta: { title: '工单管理' },
        },
        {
          path: 'create',
          name: 'create',
          component: () => import('../views/tickets/CreateTicket.vue'),
          meta: { title: '创建工单', roles: ['USER', 'ADMIN'] },
        },
        {
          path: 'tickets/:id',
          name: 'detail',
          component: () => import('../views/tickets/TicketDetail.vue'),
          meta: { title: '工单详情' },
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('../views/orders/OrderView.vue'),
          meta: { title: '订单管理' },
        },
        {
          path: 'orders/:id',
          name: 'orderDetail',
          component: () => import('../views/orders/OrderDetail.vue'),
          meta: { title: '订单详情' },
        },
        {
          path: 'policies',
          name: 'policies',
          component: () => import('../views/placeholder/PlaceholderView.vue'),
          meta: { title: '售后策略', roles: ['ADMIN'], placeholder: '售后策略由成员 C 接入' },
        },
        {
          path: 'chat',
          name: 'chat',
          component: () => import('../views/ai/ChatView.vue'),
          meta: { title: 'AI客服' },
        },
      ],
    },
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/home',
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const stores = useUserInfoStore()
  const isPublic = to.meta.public === true
  if (!isPublic && !stores.isLogin) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if ((to.path === '/login' || to.path === '/register') && stores.isLogin) {
    next('/home')
    return
  }
  const roles = to.meta.roles as string[] | undefined
  if (roles && roles.length > 0) {
    const ok = stores.user.roles.some((r) => roles.includes(r))
    if (!ok) {
      next('/home')
      return
    }
  }
  next()
})

export default router

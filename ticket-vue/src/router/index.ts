import { createRouter, createWebHistory } from 'vue-router'
import { useUserInfoStore } from '@/stores/userInfo'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/login/LoginView.vue'),
      meta: { public: true, title: '登录' },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/login/RegisterView.vue'),
      meta: { public: true, title: '注册' },
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
          meta: { title: '创建工单', roles: ['USER'] },
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
          meta: { title: '订单管理', roles: ['USER', 'ADMIN'] },
        },
        {
          path: 'orders/:id',
          name: 'order-detail',
          component: () => import('../views/orders/OrderDetail.vue'),
          meta: { title: '订单详情', roles: ['USER', 'ADMIN'] },
        },
        {
          path: 'policies',
          name: 'policies',
          component: () => import('../views/policies/PolicyView.vue'),
          meta: { title: '售后策略', roles: ['ADMIN'] },
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('../views/users/UserView.vue'),
          meta: { title: '用户管理', roles: ['ADMIN'] },
        },
        {
          path: 'chat',
          name: 'ai-chat',
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

router.beforeEach((to) => {
  const stores = useUserInfoStore()
  const isPublic = to.meta.public === true
  if (!isPublic && !stores.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if ((to.path === '/login' || to.path === '/register') && stores.isLogin) {
    return '/home'
  }
  const roles = to.meta.roles as string[] | undefined
  if (roles?.length && !stores.user.roles.some((role) => roles.includes(role))) {
    return '/home'
  }
  return true
})

export default router

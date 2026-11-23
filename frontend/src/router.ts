import { createRouter, createWebHashHistory } from 'vue-router'

export const router = createRouter({
  history: createWebHashHistory(),
  routes: [{ path: '/:pathMatch(.*)*', name: 'entry', component: { render: () => null } }],
})

const candidates = [
  { name: 'account', path: '/account', label: '我的账号', roles: ['CUSTOMER', 'ADMIN'],
    component: () => import('./AccountView.vue') },
  { name: 'admin-users', path: '/admin/users', label: '用户管理', roles: ['ADMIN'],
    component: () => import('./AdminUsersView.vue') },
]
let removeRoutes: Array<() => void> = []

/** 根据后端返回的角色添加候选路由，并撤销上一个会话的路由。 */
export async function applyUserRoutes(roles: string[]) {
  removeRoutes.forEach(remove => remove())
  const allowed = candidates.filter(route => route.roles.some(role => roles.includes(role)))
  removeRoutes = allowed.map(route => router.addRoute(route))
  const path = router.currentRoute.value.path
  await router.replace(allowed.some(route => route.path === path) ? path : allowed[0]?.path ?? '/')
  return allowed
}

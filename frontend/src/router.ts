import { createRouter, createWebHashHistory, type RouteRecordRaw, type RouteComponent } from 'vue-router'

/** 后端授权后的路由节点。 */
export interface UserRoute {
  routeName: string
  name: string
  path: string
  componentKey: string | null
  nodeType: string
  visible: boolean
  children: UserRoute[]
}

const components: Record<string, () => Promise<{ default: RouteComponent }>> = {
  account: () => import('./AccountView.vue'),
  'admin/users': () => import('./AdminUsersView.vue'),
}
const unavailable = () => import('./UnavailableView.vue')
export const router = createRouter({
  history: createWebHashHistory(),
  routes: [{ path: '/products/:id', name: 'product-detail', component: () => import('./ProductDetailView.vue') }, { path: '/', name: 'storefront', component: () => import('./StorefrontView.vue') }, { path: '/:pathMatch(.*)*', name: 'unavailable', component: unavailable }],
})
let removers: Array<() => void> = []

/** 清除上个用户的动态路由。 */
export function clearUserRoutes() {
  removers.forEach(remove => remove())
  removers = []
}

/** 递归注册后端路由，仅加载本地受信组件映射。 */
export async function applyUserRoutes(nodes: UserRoute[]) {
  clearUserRoutes()
  const records: RouteRecordRaw[] = []
  /** 展开树形配置，目录仅提供分组，页面独立匹配完整路径。 */
  function visit(items: UserRoute[]) {
    for (const item of items) {
      if (item.nodeType === 'PAGE') {
        const component = item.componentKey && Object.prototype.hasOwnProperty.call(components, item.componentKey)
          ? components[item.componentKey] : undefined
        if (component && item.path.startsWith('/') && !item.path.includes(':') && item.routeName !== 'unavailable') {
          records.push({ path: item.path, name: item.routeName, component, meta: { title: item.name } })
        }
      }
      visit(item.children ?? [])
    }
  }
  visit(nodes)
  records.forEach(record => removers.push(router.addRoute(record)))
  await router.isReady()
  const path = router.currentRoute.value.fullPath
  await router.replace(path)
}

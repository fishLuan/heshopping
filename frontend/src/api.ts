/** 当前会话允许返回给前端的用户资料。 */
export interface User {
  id: number
  roles: string[]
  userName: string
  nickName?: string
}

/** 后端统一响应，提示级别和操作结果分别处理。 */
interface Result<T> {
  isSuccess: boolean
  code?: string
  message?: string
  level?: 'SUCCESS' | 'WARNING' | 'ERROR'
  data?: T
}

/** 请求同域接口，携带会话 Cookie 并读取统一响应。 */
export async function request<T>(path: string, options?: RequestInit) {
  return send<T>('/api/user/' + path, options)
}

/** 调用同域后端，并为写请求附上 Spring Security 的 CSRF 令牌。 */
export async function send<T>(path: string, options?: RequestInit) {
  const headers = new Headers(options?.headers)
  const method = (options?.method ?? 'GET').toUpperCase()
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const cookie = document.cookie.split('; ').find(value => value.startsWith('XSRF-TOKEN='))
    if (cookie) headers.set('X-XSRF-TOKEN', decodeURIComponent(cookie.slice('XSRF-TOKEN='.length)))
  }
  const response = await fetch(path, { ...options, headers, credentials: 'same-origin' })
  const result: Result<T> = await response.json().catch(() => {
    throw new Error('服务暂时不可用，请稍后重试')
  })
  return { ...result, status: response.status, ok: response.ok && result.isSuccess }
}

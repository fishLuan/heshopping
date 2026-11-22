/** 当前会话允许返回给前端的用户资料。 */
export interface User {
  id: number
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
  const response = await fetch('/api/user/' + path, { ...options, credentials: 'same-origin' })
  const result: Result<T> = await response.json().catch(() => {
    throw new Error('服务暂时不可用，请稍后重试')
  })
  return { ...result, status: response.status, ok: response.ok && result.isSuccess }
}

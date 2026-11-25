<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { request, type User } from './api'
import { applyUserRoutes, clearUserRoutes, router, type UserRoute } from './router'
import MenuTree from './MenuTree.vue'

const isStorefront = computed(() => router.currentRoute.value.path === '/' || router.currentRoute.value.name === 'product-detail')
const mode = ref<'login' | 'register'>('login')
const user = ref<User | null>(null)
const menu = ref<UserRoute[]>([])
const routeLoading = ref(false)
const routeError = ref('')
const expanded = ref(false)

/** 读取数据库路由，失败时允许重试，不沿用旧授权。 */
async function loadRoutes() {
  routeLoading.value = true
  routeError.value = ''
  menu.value = []
  clearUserRoutes()
  try {
    const result = await request<UserRoute[]>('routes')
    if (!result.ok) throw new Error(result.message || '无法读取页面入口')
    menu.value = result.data ?? []
    await applyUserRoutes(menu.value)
  } catch (error) {
    routeError.value = error instanceof Error ? error.message : '无法读取页面入口'
  } finally {
    routeLoading.value = false
  }
}

/** 接口会话失效时清理用户和路由，返回登录表单。 */
function sessionExpired() {
  if (!user.value) return
  user.value = null
  menu.value = []
  clearUserRoutes()
  notice.value = { level: 'WARNING', text: '登录已失效，请重新登录' }
}

const userName = ref('')
const password = ref('')
const confirmation = ref('')
const loading = ref(true)
const busy = ref(false)
const notice = ref<{ level: 'SUCCESS' | 'WARNING' | 'ERROR'; text: string } | null>(null)

/** 切换表单时清理密码和上一轮提示。 */
function switchMode(next: 'login' | 'register') {
  mode.value = next
  password.value = ''
  confirmation.value = ''
  notice.value = null
}

/** 通过后端会话恢复登录状态。 */
async function restoreSession() {
  try {
    const result = await request<User>('me')
    if (result.ok && result.data) { user.value = result.data; await loadRoutes() }
    else if (result.status !== 401) {
      notice.value = { level: 'ERROR', text: result.message || '无法查询登录状态，请稍后重试' }
    }
  } catch {
    notice.value = { level: 'ERROR', text: '无法连接服务，请稍后重试' }
  } finally {
    loading.value = false
  }
}

/** 提交注册或登录，按接口返回级别显示提示。 */
async function submit() {
  if (busy.value) return
  const name = userName.value.trim()
  if (!name || !password.value.trim()) {
    notice.value = { level: 'WARNING', text: '请输入用户名和密码' }
    return
  }
  const registering = mode.value === 'register'
  if (registering && password.value !== confirmation.value) {
    notice.value = { level: 'WARNING', text: '两次密码输入不一致' }
    return
  }

  busy.value = true
  notice.value = null
  try {
    const result = registering
      ? await request<User>('reg', {
          method: 'POST',
          body: new URLSearchParams({ userName: name, pwd: password.value }),
        })
      : await request<User>('login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ userName: name, password: password.value }),
        })
    if (!result.ok) {
      notice.value = {
        level: result.level === 'WARNING' || result.status === 429 ? 'WARNING' : 'ERROR',
        text: result.message || '操作失败，请重试',
      }
      return
    }

    if (registering) {
      userName.value = name
      switchMode('login')
      notice.value = { level: 'SUCCESS', text: '注册成功，请登录' }
    } else if (result.data) {
      user.value = result.data
      await loadRoutes()
      if (router.currentRoute.value.path === '/login') await router.replace('/account')
      password.value = ''
    } else {
      notice.value = { level: 'ERROR', text: '登录响应缺少用户资料，请重试' }
    }
  } catch {
    notice.value = { level: 'ERROR', text: '无法连接服务，请稍后重试' }
  } finally {
    busy.value = false
  }
}

/** 退出成功后清除页面上的用户状态。 */
async function logout() {
  if (busy.value) return
  busy.value = true
  notice.value = null
  try {
    const result = await request<void>('logout', { method: 'POST' })
    if (!result.ok) {
      notice.value = { level: 'ERROR', text: result.message || '退出失败，请重试' }
      return
    }
    user.value = null
    menu.value = []
    clearUserRoutes()
    await router.replace('/')
    switchMode('login')
    notice.value = { level: 'SUCCESS', text: '已退出登录' }
  } catch {
    notice.value = { level: 'ERROR', text: '无法连接服务，请稍后重试' }
  } finally {
    busy.value = false
  }
}

window.addEventListener('session-expired', sessionExpired)
onMounted(restoreSession)
</script>

<template>
  <RouterView v-if="isStorefront" />
  <div v-else-if="user && !loading" class="workspace">
    <header class="topbar">
      <RouterLink to="/" class="brand"><span class="mark" aria-hidden="true">H</span> Hashopping</RouterLink>
      <div class="user-actions"><span>{{ user.nickName || user.userName }}</span>
        <button :disabled="busy" @click="logout">{{ busy ? '正在退出…' : '退出登录' }}</button>
      </div>
    </header>
    <div class="workspace-body">
      <aside class="sidebar">
        <button class="menu-toggle" :aria-expanded="expanded" @click="expanded = !expanded">导航菜单</button>
        <nav :class="{ expanded }" aria-label="个人中心导航">
          <MenuTree :items="menu" />
          <p v-if="!menu.length && !routeLoading && !routeError" class="muted">暂无可用页面</p>
        </nav>
      </aside>
      <main class="content">
        <p v-if="routeLoading" role="status">正在加载页面入口…</p>
        <section v-else-if="routeError" role="alert">
          <h1>无法加载页面</h1><p>{{ routeError }}</p><button @click="loadRoutes">重新加载</button>
        </section>
        <RouterView v-else :user="user" />
        <p v-if="notice" class="notice" :class="notice.level.toLowerCase()" role="status">{{ notice.text }}</p>
      </main>
    </div>
  </div>
  <main v-else class="layout">
    <section class="account" aria-labelledby="heading">
      <RouterLink to="/" class="brand"><span class="mark" aria-hidden="true">H</span> Hashopping</RouterLink>
      <p v-if="loading" class="muted" role="status">正在检查登录状态…</p>
      <template v-else>
        <h1 id="heading">{{ mode === 'login' ? '登录账号' : '创建账号' }}</h1>
        <form @submit.prevent="submit">
          <label for="username">用户名</label>
          <input id="username" v-model="userName" autocomplete="username" required :disabled="busy">
          <label for="password">密码</label>
          <input id="password" v-model="password" type="password"
            :autocomplete="mode === 'login' ? 'current-password' : 'new-password'" required :disabled="busy">
          <template v-if="mode === 'register'">
            <label for="confirmation">确认密码</label>
            <input id="confirmation" v-model="confirmation" type="password" autocomplete="new-password" required :disabled="busy">
          </template>
          <button class="primary" type="submit" :disabled="busy">{{ busy ? '正在提交…' : mode === 'login' ? '登录' : '注册' }}</button>
        </form>
        <button class="switch" type="button" :disabled="busy" @click="switchMode(mode === 'login' ? 'register' : 'login')">
          {{ mode === 'login' ? '创建账号' : '返回登录' }}
        </button>
      </template>
      <p v-if="notice" class="notice" :class="notice.level.toLowerCase()"
        :role="notice.level === 'ERROR' ? 'alert' : 'status'">{{ notice.text }}</p>
    </section>
  </main>
</template>
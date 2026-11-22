<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { request, type User } from './api'

const mode = ref<'login' | 'register'>('login')
const user = ref<User | null>(null)
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
    if (result.ok && result.data) user.value = result.data
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
    switchMode('login')
    notice.value = { level: 'SUCCESS', text: '已退出登录' }
  } catch {
    notice.value = { level: 'ERROR', text: '无法连接服务，请稍后重试' }
  } finally {
    busy.value = false
  }
}

onMounted(restoreSession)
</script>

<template>
  <main class="layout">
    <section class="account" aria-labelledby="heading">
      <div class="brand"><span class="mark" aria-hidden="true">H</span> Hashopping</div>
      <p v-if="loading" class="muted" role="status">正在检查登录状态…</p>
      <template v-else-if="user">
        <h1 id="heading">你好，{{ user.nickName || user.userName }}</h1>
        <p class="muted">已登录为 {{ user.userName }}</p>
        <button class="primary" :disabled="busy" @click="logout">{{ busy ? '正在退出…' : '退出登录' }}</button>
      </template>
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
          <button class="primary" type="submit" :disabled="busy">
            {{ busy ? '正在提交…' : mode === 'login' ? '登录' : '注册' }}
          </button>
        </form>
        <button class="switch" type="button" :disabled="busy"
          @click="switchMode(mode === 'login' ? 'register' : 'login')">
          {{ mode === 'login' ? '创建账号' : '返回登录' }}
        </button>
      </template>
      <p v-if="notice" class="notice" :class="notice.level.toLowerCase()"
        :role="notice.level === 'ERROR' ? 'alert' : 'status'">{{ notice.text }}</p>
    </section>
  </main>
</template>

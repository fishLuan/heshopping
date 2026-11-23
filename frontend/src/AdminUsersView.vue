<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { send, type User } from './api'

const users = ref<User[]>([])
const loading = ref(true)
const error = ref('')

/** 加载管理员有权查看的用户列表。 */
async function loadUsers() {
  try {
    const result = await send<{ data: User[] }>('/users')
    if (result.ok) users.value = result.data?.data ?? []
    else error.value = result.message || '无法读取用户列表'
  } catch {
    error.value = '无法连接服务'
  } finally {
    loading.value = false
  }
}

onMounted(loadUsers)
</script>

<template>
  <section aria-labelledby="users-heading">
    <h2 id="users-heading">用户列表</h2>
    <p v-if="loading" role="status">正在加载…</p>
    <p v-else-if="error" role="alert">{{ error }}</p>
    <p v-else-if="!users.length">暂无用户</p>
    <ul v-else>
      <li v-for="item in users" :key="item.id">{{ item.nickName || item.userName }} · {{ item.userName }}</li>
    </ul>
  </section>
</template>

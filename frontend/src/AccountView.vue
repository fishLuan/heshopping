<script setup lang="ts">
import type { User } from './api'
defineProps<{ user?: User }>()
</script>

<template>
  <section v-if="user" aria-labelledby="account-heading">
    <h1 id="account-heading">我的账号</h1>
    <dl class="profile">
      <dt>用户名</dt><dd>{{ user.userName }}</dd>
      <dt>昵称</dt><dd>{{ user.nickName || '未设置' }}</dd>
      <dt>角色</dt><dd>{{ user.roles.join('、') || '暂无角色' }}</dd>
      <dt>最近登录</dt><dd>{{ user.lastLoginAt?.replace('T', ' ') || '暂无记录' }}</dd>
    </dl>
  </section>
</template>

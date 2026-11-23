<script setup lang="ts">
import type { User } from './api'

defineProps<{ user?: User }>()
</script>

<template>
  <section v-if="user" aria-label="我的账号">
    <p class="muted">当前角色：{{ user.roles.join('、') }}</p>
  </section>
</template>

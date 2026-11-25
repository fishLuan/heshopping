<script setup lang="ts">
import type { UserRoute } from './router'
defineProps<{ items: UserRoute[] }>()
</script>

<template>
  <ul class="menu-tree">
    <template v-for="item in items" :key="item.routeName">
      <li v-if="item.visible">
        <span v-if="item.nodeType === 'DIRECTORY'" class="menu-group">{{ item.name }}</span>
        <RouterLink v-else :to="item.path">{{ item.name }}</RouterLink>
        <MenuTree v-if="item.children?.length" :items="item.children" />
      </li>
    </template>
  </ul>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { send } from './api'

/** 首页商品卡片数据。 */
interface Product {
  id: string
  name: string
  mainImage: string
  price: number
  purchaseNum: number
  shopName: string
}
/** 后端分页数据。 */
interface Page {
  data: Product[]
  totalCount: number
  totalPage: number
}

const products = ref<Product[]>([])
const keyword = ref('')
const sort = ref('latest')
const total = ref(0)
const busy = ref(false)
const error = ref('')
const finished = ref(false)
const sentinel = ref<HTMLElement | null>(null)
let activeKeyword = ''
let activeSort = 'latest'
let pageNum = 1
let generation = 0
let controller: AbortController | null = null
let observer: IntersectionObserver | null = null

/** 接近列表底部时追加一页；忽略重置前的迟到响应。 */
async function loadMore() {
  if (busy.value || finished.value) return
  const current = generation
  busy.value = true
  error.value = ''
  controller = new AbortController()
  try {
    const query = new URLSearchParams({
      pageNum: String(pageNum), pageSize: '12', keyword: activeKeyword, sort: activeSort,
    })
    const result = await send<Page>('/api/product/page?' + query, { signal: controller.signal })
    if (current !== generation) return
    if (!result.ok || !result.data) throw new Error(result.message || '商品加载失败')
    const known = new Set(products.value.map(item => item.id))
    products.value.push(...result.data.data.filter(item => !known.has(item.id)))
    total.value = result.data.totalCount
    finished.value = pageNum >= result.data.totalPage || result.data.data.length === 0
    pageNum++
  } catch (reason) {
    if (current === generation && !(reason instanceof DOMException && reason.name === 'AbortError')) {
      error.value = reason instanceof Error ? reason.message : '无法连接服务'
    }
  } finally {
    if (current === generation) {
      busy.value = false
      await nextTick()
      if (!finished.value && !error.value && sentinel.value
          && sentinel.value.getBoundingClientRect().top < window.innerHeight + 240) {
        void loadMore()
      }
    }
  }
}

/** 搜索或排序后取消旧请求，从第一页重新查询。 */
function reset() {
  generation++
  controller?.abort()
  activeKeyword = keyword.value.trim()
  activeSort = sort.value
  products.value = []
  total.value = 0
  finished.value = false
  busy.value = false
  error.value = ''
  pageNum = 1
  void loadMore()
}

/** 图片失效时使用本地默认图，并移除监听以防循环。 */
function fallback(event: Event) {
  const image = event.target as HTMLImageElement
  image.onerror = null
  image.src = '/images/default-product.png'
}

/** 初始化底部观察器，预留距离提前加载下一页。 */
onMounted(() => {
  observer = new IntersectionObserver(entries => {
    if (entries.some(entry => entry.isIntersecting) && !error.value) void loadMore()
  }, { rootMargin: '240px' })
  if (sentinel.value) observer.observe(sentinel.value)
  void loadMore()
})

/** 离开商城时停止监听与在途请求。 */
onUnmounted(() => {
  generation++
  controller?.abort()
  observer?.disconnect()
})
</script>

<template>
  <div class="storefront">
    <header class="store-header storefront-header">
      <RouterLink to="/" class="brand storefront-brand" aria-label="Hashopping 首页">
        <img class="storefront-logo" src="/images/hashopping-logo-green.png" alt="Hashopping" width="280" height="56">
      </RouterLink>
      <RouterLink to="/account" class="account-link">个人中心</RouterLink>
    </header>
    <main class="store-main">
      <div class="store-title"><div><p class="eyebrow">日常穿搭 · 轻松出发</p><h1>找到你的下一双</h1></div></div>
      <form class="store-filters" @submit.prevent="reset">
        <label class="search-field"><span class="sr-only">搜索商品名称</span>
          <input v-model="keyword" placeholder="搜索跑鞋、休闲鞋…" type="search">
        </label>
        <button class="search-button" type="submit">搜索</button>
        <label class="sort-field"><span class="sr-only">商品排序</span>
          <select v-model="sort" @change="reset">
            <option value="latest">最新上架</option>
            <option value="priceAsc">价格从低到高</option>
            <option value="priceDesc">价格从高到低</option>
          </select>
        </label>
      </form>
      <p class="store-count" aria-live="polite">共 {{ total }} 件商品</p>
      <div class="product-grid">
        <article v-for="product in products" :key="product.id" class="product-card">
          <RouterLink :to="{ name: 'product-detail', params: { id: product.id } }" class="product-photo"><img :src="product.mainImage" :alt="product.name" loading="lazy" @error="fallback"></RouterLink>
          <div class="product-copy"><p class="product-shop">{{ product.shopName }}</p>
            <h2><RouterLink :to="{ name: 'product-detail', params: { id: product.id } }">{{ product.name }}</RouterLink></h2>
            <div class="product-bottom"><span class="product-price">¥{{ product.price.toFixed(2) }}<small>起</small></span>
              <span class="product-sales">已售 {{ product.purchaseNum }}</span></div>
          </div>
        </article>
      </div>
      <div ref="sentinel" class="load-status" aria-live="polite">
        <p v-if="busy">正在加载商品…</p>
        <template v-else-if="error"><p role="alert">{{ error }}</p><button @click="loadMore">重新加载</button></template>
        <p v-else-if="!products.length">没有找到商品，试试其他关键词</p>
        <p v-else-if="finished">已经到底了 · 共 {{ products.length }} 件商品</p>
        <button v-else @click="loadMore">加载更多</button>
      </div>
    </main>
  </div>
</template>

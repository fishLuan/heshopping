<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { send } from './api'

/** 商品详情展示数据。 */
interface Product {
  id: string
  name: string
  productIntro?: string
  productImgs?: string
  shopName: string
}
/** 可选择的尺码规格。 */
interface Sku {
  id: string
  size: number
  price: number
  stock: number
}
const route = useRoute()
const product = ref<Product | null>(null)
const skus = ref<Sku[]>([])
const selected = ref<Sku | null>(null)
const activeImage = ref('')
const loading = ref(false)
const error = ref('')
let controller: AbortController | null = null
const images = computed(() => product.value?.productImgs?.split(',').map(s => s.trim()).filter(Boolean) ?? [])
const minimumPrice = computed(() => {
  const prices = skus.value.filter(s => s.stock > 0 && s.price > 0).map(s => s.price)
  return prices.length ? Math.min(...prices) : null
})

/** 同时读取商品与规格，切换商品时取消旧请求。 */
async function load() {
  controller?.abort()
  const current = new AbortController()
  controller = current
  loading.value = true
  error.value = ''
  selected.value = null
  product.value = null
  skus.value = []
  try {
    const id = encodeURIComponent(String(route.params.id))
    const [detail, variants] = await Promise.all([
      send<Product>('/api/product/' + id, { signal: current.signal }),
      send<Sku[]>('/api/productdetail/productId?productId=' + id, { signal: current.signal }),
    ])
    if (current.signal.aborted) return
    if (!detail.ok || !detail.data) throw new Error(detail.message || '商品不存在')
    if (!variants.ok) throw new Error(variants.message || '规格加载失败')
    product.value = detail.data
    skus.value = (variants.data ?? []).sort((a, b) => a.size - b.size)
    activeImage.value = images.value[0] || '/images/default-product.png'
  } catch (reason) {
    if (!current.signal.aborted) error.value = reason instanceof Error ? reason.message : '无法连接服务'
  } finally {
    if (!current.signal.aborted) loading.value = false
  }
}

/** 图片加载失败时切换到本地占位图。 */
function fallback(event: Event) {
  const image = event.target as HTMLImageElement
  if (!image.src.endsWith('/images/default-product.png')) image.src = '/images/default-product.png'
}

/** 路由参数变化时重新加载详情。 */
watch(() => route.params.id, load, { immediate: true })
/** 离开页面时取消请求。 */
onUnmounted(() => controller?.abort())
</script>

<template>
  <div class="storefront">
    <header class="store-header">
      <RouterLink to="/" class="brand"><span class="mark">H</span> Hashopping</RouterLink>
      <RouterLink to="/account">个人中心</RouterLink>
    </header>
    <main class="store-main">
      <RouterLink to="/">返回商城</RouterLink>
      <p v-if="loading" role="status">正在加载商品…</p>
      <section v-else-if="error" role="alert"><h1>{{ error }}</h1><button @click="load">重新加载</button></section>
      <section v-else-if="product" class="detail-grid">
        <div>
          <img class="detail-main-image" :src="activeImage" :alt="product.name" @error="fallback">
          <div v-if="images.length > 1" class="detail-thumbnails">
            <button v-for="(image,index) in images" :key="index" :aria-label="'查看图片'+(index+1)"
              :aria-pressed="activeImage === image" @click="activeImage=image">
              <img :src="image" alt="" @error="fallback">
            </button>
          </div>
        </div>
        <div class="detail-info">
          <p class="product-shop">{{ product.shopName }}</p>
          <h1>{{ product.name }}</h1>
          <p class="muted">{{ product.productIntro }}</p>
          <p class="product-price" aria-live="polite">
            <template v-if="selected">¥{{ selected.price.toFixed(2) }}</template>
            <template v-else-if="minimumPrice !== null">¥{{ minimumPrice.toFixed(2) }} 起</template>
            <template v-else>暂时缺货</template>
          </p>
          <fieldset class="sku-field"><legend>选择尺码</legend>
            <button v-for="sku in skus" :key="sku.id" :disabled="sku.stock <= 0 || sku.price <= 0"
              :aria-pressed="selected?.id === sku.id" @click="selected=sku">
              {{ sku.size }}{{ sku.stock <= 0 ? ' · 缺货' : '' }}
            </button>
          </fieldset>
          <p aria-live="polite">{{ selected ? '当前尺码库存：'+selected.stock+' 件' : skus.length ? '请选择尺码查看库存' : '暂无可售规格' }}</p>
        </div>
      </section>
    </main>
  </div>
</template>

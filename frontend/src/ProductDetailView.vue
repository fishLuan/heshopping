<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { send, request, type User } from './api'

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
  style?: string | null
  size: number
  price: number
  stock: number
}
/** 服务端创建订单后的最小展示信息。 */
interface Address {
  id: number
  recipientName: string
  recipientPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: boolean
}
/** 选择收货地址后确认下单。 */
interface OrderCreated {
  orderNumber: string
  totalPrice: number
  quantity: number
}
const checkout = ref<HTMLDialogElement | null>(null)
const addresses = ref<Address[]>([])
const addressId = ref<number | null>(null)
const preparing = ref(false)

/** 购买入口先验证会话并读取地址，尚不创建订单。 */
async function reviewOrder() {
  if (preparing.value || submitting.value || !selected.value) return
  preparing.value = true
  orderError.value = ''
  try {
    const session = await request<User>('me')
    if (session.status === 401) { await goLogin(); return }
    if (!session.ok) throw new Error(session.message || '无法确认登录状态')
    const result = await send<Address[]>('/api/addresses')
    if (!result.ok) throw new Error(result.message || '无法读取收货地址')
    addresses.value = result.data ?? []
    addressId.value = addresses.value.find(item => item.isDefault)?.id ?? addresses.value[0]?.id ?? null
    checkout.value?.showModal()
  } catch(reason) {
    orderError.value = reason instanceof Error ? reason.message : '无法连接服务'
  } finally { preparing.value = false }
}

/** 提交时防止关闭弹窗，避免误认为下单被撤销。 */
function preventCheckoutClose(event: Event) {
  if (submitting.value) event.preventDefault()
}

const cartDialog = ref<HTMLDialogElement|null>(null)
const cartBusy = ref(false)
const cartNotice = ref('')
const cartQuantity = ref(1)
const cartSkuId = ref('')
const cartSku = computed(() => skus.value.find(item=>item.id===cartSkuId.value))

/** 加购前验证登录，保留规格选择并打开独立加购窗口。 */
async function openCart() {
  if(cartBusy.value) return
  cartBusy.value=true
  orderError.value=''
  try {
    const session=await request<User>('me')
    if(session.status===401) { await goLogin(); return }
    if(!session.ok) throw new Error(session.message || '无法确认登录状态')
    cartSkuId.value=selected.value?.id || ''
    cartQuantity.value=quantity.value
    cartNotice.value=''
    cartDialog.value?.showModal()
  } catch(reason) { orderError.value=reason instanceof Error?reason.message:'无法连接服务' }
  finally { cartBusy.value=false }
}

/** 加购只提交SKU和数量，不预占库存；成功后需用户主动返回。 */
async function addToCart() {
  if(cartBusy.value || !cartSku.value) return
  if(!Number.isInteger(cartQuantity.value) || cartQuantity.value<1 || cartQuantity.value>cartSku.value.stock) {
    cartNotice.value='请输入库存范围内的整数数量'
    return
  }
  cartBusy.value=true
  cartNotice.value=''
  try {
    const result=await send<void>('/api/cart',{
      method:'POST',headers:{'Content-Type':'application/json'},
      body:JSON.stringify({skuId:cartSkuId.value,quantity:cartQuantity.value}),
    })
    if(result.status===401) { cartDialog.value?.close(); await goLogin(); return }
    if(!result.ok) throw new Error(result.message || '加入失败')
    cartDialog.value?.close()
    orderError.value=''
    cartAdded.value=true
  } catch(reason) { cartNotice.value=reason instanceof Error?reason.message:'请求未确认，请先查看购物车，避免重复加购' }
  finally { cartBusy.value=false }
}
const cartAdded=ref(false)

/** 加购提交期间保留窗口，避免误认为已撤销请求。 */
function preventCartClose(event:Event) { if(cartBusy.value) event.preventDefault() }

const router = useRouter()
const quantity = ref(1)
const submitting = ref(false)
const order = ref<OrderCreated | null>(null)
const orderError = ref('')
let pending: { skuId: string; quantity: number; requestId: string; userId: number; addressId: number } | null = null
const route = useRoute()

/** 校验登录后创建订单，网络重试沿用同一请求标识。 */
async function createOrder() {
  if (submitting.value || !selected.value || order.value) return
  if (!Number.isInteger(quantity.value) || quantity.value < 1 || quantity.value > selected.value.stock) {
    orderError.value = '请输入库存范围内的整数数量'
    return
  }
  if (!addressId.value) { orderError.value = '请选择收货地址'; return }
  submitting.value = true
  orderError.value = ''
  try {
    const session = await request<User>('me')
    if (session.status === 401) {
      await goLogin()
      return
    }
    if (!session.ok || !session.data) throw new Error(session.message || '无法确认登录状态')
    if (!pending || pending.skuId !== selected.value.id || pending.quantity !== quantity.value
        || pending.userId !== session.data.id || pending.addressId !== addressId.value) {
      pending = { skuId: selected.value.id, quantity: quantity.value, requestId: crypto.randomUUID(), userId: session.data.id, addressId: addressId.value }
    }
    const result = await send<OrderCreated>('/api/order/add', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ skuId: pending.skuId, quantity: pending.quantity, requestId: pending.requestId, addressId: pending.addressId }),
    })
    if (result.status === 401) {
      await goLogin()
      return
    }
    if (!result.ok || !result.data) throw new Error(result.message || '下单失败，请重试')
    checkout.value?.close()
    order.value = result.data
    selected.value.stock = Math.max(0, selected.value.stock - result.data.quantity)
  } catch (reason) {
    orderError.value = reason instanceof Error ? reason.message : '无法连接服务，请重试'
  } finally {
    submitting.value = false
  }
}

/** 登录前将规格与数量放入返回地址，不自动提交订单。 */
async function goLogin() {
  checkout.value?.close()
  const target = router.resolve({
    name: 'product-detail', params: { id: route.params.id },
    query: { sku: selected.value?.id, quantity: String(quantity.value) },
  }).fullPath
  await router.push({ path: '/login', query: { redirect: target } })
}
const product = ref<Product | null>(null)
const skus = ref<Sku[]>([])
const selected = ref<Sku | null>(null)
const activeStyle = ref('')
const styleOf = (sku: Sku) => sku.style?.trim() || ''
const styles = computed(() => [...new Set(skus.value.map(styleOf))])
const hasStyles = computed(() => styles.value.some(Boolean))
const visibleSkus = computed(() => hasStyles.value
  ? skus.value.filter(sku => styleOf(sku) === activeStyle.value)
  : skus.value)
const available = (sku: Sku) => sku.stock > 0 && sku.price > 0

/** 切换款式时优先保留尺码，否则选择该款式首个可售尺码。 */
function selectStyle(style: string) {
  const size = selected.value?.size
  activeStyle.value = style
  selectSku(visibleSkus.value.find(sku => sku.size === size && available(sku))
    ?? visibleSkus.value.find(available) ?? null)
}
function selectSku(sku: Sku | null) {
  selected.value = sku
  quantity.value = sku ? Math.min(Math.max(1, Number.isInteger(quantity.value) ? quantity.value : 1), sku.stock) : 1
  orderError.value = ''
}
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
  activeStyle.value = ''
  order.value = null
  orderError.value = ''
  quantity.value = 1
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
    const restored = skus.value.find(s => s.id === route.query.sku && s.stock > 0 && s.price > 0)
    selected.value = restored ?? skus.value.find(s => s.stock > 0 && s.price > 0) ?? null
    activeStyle.value = styleOf(selected.value ?? skus.value[0] ?? { id: '', size: 0, price: 0, stock: 0 })
    if (restored) {
      const count = Number(route.query.quantity)
      quantity.value = Number.isInteger(count) && count > 0 ? Math.min(count, restored.stock) : 1
    }
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
      <RouterLink to="/" class="brand storefront-brand" aria-label="Hashopping 首页">
        <img class="storefront-logo" src="/images/hashopping-logo-green.png" alt="Hashopping" width="280" height="56">
      </RouterLink>
      <RouterLink to="/account" class="account-link">个人中心</RouterLink>
    </header>
    <main class="store-main">
      <RouterLink to="/" class="detail-back-link">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
          <path d="m10 6-6 6 6 6M4 12h16" />
        </svg>
        <span>返回商城</span>
      </RouterLink>
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
          <fieldset v-if="hasStyles" class="sku-field"><legend>选择款式</legend>
            <button v-for="style in styles" :key="style" :disabled="submitting || preparing || !!order || !skus.some(sku => styleOf(sku) === style && available(sku))"
              :aria-pressed="activeStyle === style" @click="selectStyle(style)">
              {{ style || '默认款式' }}
            </button>
          </fieldset>
          <fieldset class="sku-field"><legend>选择尺码</legend>
            <button v-for="sku in visibleSkus" :key="sku.id" :disabled="submitting || preparing || !!order || sku.stock <= 0 || sku.price <= 0"
              :aria-pressed="selected?.id === sku.id" @click="selectSku(sku)">
              {{ sku.size }}{{ sku.stock <= 0 ? ' · 缺货' : '' }}
            </button>
          </fieldset>
          <p aria-live="polite">{{ selected ? '当前尺码库存：'+selected.stock+' 件' : '暂无可售规格' }}</p>
          <button :disabled="cartBusy || loading || !skus.some(s=>s.stock>0 && s.price>0)" class="cart-add-button" @click="openCart">🛒 加入购物车</button>
          <p v-if="cartAdded" class="notice success" role="status">已加入购物车，<RouterLink to="/cart">查看购物车</RouterLink></p>
          <div v-if="!order" class="purchase-controls">
            <label for="quantity">购买数量</label>
            <input id="quantity" v-model.number="quantity" type="number" min="1" :max="selected?.stock || 1"
              step="1" :disabled="submitting || !selected">
            <p v-if="selected">预计金额：¥{{ (selected.price * quantity).toFixed(2) }}</p>
            <button class="primary" :disabled="preparing || submitting || !selected || selected.stock <= 0" @click="reviewOrder">
              {{ preparing ? '正在准备…' : selected ? '立即购买' : '暂无可售规格' }}
            </button>
          </div>
          <p v-if="orderError" class="notice error" role="alert">{{ orderError }}</p>
          <section v-if="order" class="order-confirmation" role="status">
            <h2>下单成功 · 待付款</h2>
            <p>订单号：{{ order.orderNumber }}</p>
              <RouterLink :to="{ path:'/orders',query:{ number:order.orderNumber } }">查看订单及取消订单</RouterLink>
            <p>购买数量：{{ order.quantity }}</p>
            <p>订单金额：¥{{ Number(order.totalPrice).toFixed(2) }}</p>
          </section>
        </div>
      </section>
      <dialog ref="cartDialog" class="address-dialog" aria-labelledby="cart-title" @cancel="preventCartClose">
        <h2 id="cart-title">加入购物车</h2>
        <p>{{ product?.name }}</p>
        <fieldset class="sku-field"><legend>选择尺码</legend>
          <button v-for="sku in skus" :key="sku.id" :disabled="cartBusy || sku.stock<=0 || sku.price<=0"
            :aria-pressed="cartSkuId===sku.id" @click="cartSkuId=sku.id">{{ sku.size }}{{ sku.stock<=0?' · 缺货':'' }}</button>
        </fieldset>
        <p v-if="cartSku">¥{{ cartSku.price.toFixed(2) }} · 库存 {{ cartSku.stock }}</p>
        <label for="cart-quantity">数量</label>
        <input id="cart-quantity" v-model.number="cartQuantity" type="number" min="1" :max="cartSku?.stock || 1" :disabled="cartBusy">
        <p v-if="cartNotice" class="notice warning" role="alert">{{ cartNotice }}</p>
        <div class="address-actions">
          <button :disabled="cartBusy" @click="cartDialog?.close()">返回</button>
          <button :disabled="cartBusy || !cartSku || cartSku.stock<=0" @click="addToCart">{{ cartBusy?'加入中…':'加入购物车' }}</button>
        </div>
      </dialog>
      <dialog ref="checkout" class="address-dialog" aria-labelledby="checkout-title" @cancel="preventCheckoutClose">
        <h2 id="checkout-title">确认订单</h2>
        <p>{{ product?.name }} · 尺码 {{ selected?.size }}</p>
        <label for="checkout-quantity">购买数量</label>
        <input id="checkout-quantity" v-model.number="quantity" type="number" min="1" :max="selected?.stock" step="1" :disabled="submitting">
        <p>预计金额：¥{{ ((selected?.price ?? 0) * quantity).toFixed(2) }}</p>
        <label for="checkout-address">收货地址</label>
        <select v-if="addresses.length" id="checkout-address" v-model="addressId" :disabled="submitting" class="checkout-address">
          <option v-for="address in addresses" :key="address.id" :value="address.id">
            {{ address.isDefault ? '默认 · ' : '' }}{{ address.recipientName }} {{ address.recipientPhone }} ·
            {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
          </option>
        </select>
        <p v-else>还没有收货地址，请先添加。</p>
        <RouterLink v-if="!submitting" to="/addresses" @click="checkout?.close()">管理收货地址</RouterLink>
        <p v-if="orderError" class="notice error" role="alert">{{ orderError }}</p>
        <div class="address-actions">
          <button :disabled="submitting" @click="checkout?.close()">返回修改</button>
          <button class="primary" :disabled="submitting || !addressId" @click="createOrder">{{ submitting ? '正在下单…' : '确认下单' }}</button>
        </div>
      </dialog>
    </main>
  </div>
</template>

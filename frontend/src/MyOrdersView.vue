<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { send } from './api'

/** 本人订单详情及快照。 */
interface Order {
  recipientName?: string
  recipientPhone?: string
  shippingAddress?: string
  orderNumber: string
  productName: string | null
  skuSnapshot: string | null
  quantity: number
  totalPrice: number
  unitPrice: number | null
  status: string
  createdAt: string | null
  expiresAt: string | null
}
interface Page { data: Order[]; totalPage: number; totalCount: number }
const labels: Record<string,string> = {
  WAIT_BUYER_PAY:'待付款', TRADE_PAID_SUCCESS:'已付款',
  TRADE_CANCELLED:'已取消', TRADE_CLOSED:'已关闭', TRADE_PAID_FAILED:'支付失败',
}
const route = useRoute()
const router = useRouter()
const orders = ref<Order[]>([])
const detail = ref<Order | null>(null)
const status = ref('')
const page = ref(1)
const pages = ref(0)
const loading = ref(false)
const error = ref('')
const cancelling = ref('')
const confirmOrder = ref<Order | null>(null)
const cashier = ref<HTMLDialogElement | null>(null)
const paymentOrder = ref<Order | null>(null)
const paying = ref(false)
const paymentNotice = ref('')
const paymentSucceeded = ref(false)

/** 重新读取订单后展示开发收银台，避免使用过期状态。 */
async function openCashier(order: Order) {
  if (paying.value) return
  paymentNotice.value = ''
  paymentSucceeded.value = false
  try {
    const result = await send<Order>('/api/order/'+encodeURIComponent(order.orderNumber))
    if (!result.ok || !result.data) throw new Error(result.message || '订单加载失败')
    if (result.data.status !== 'WAIT_BUYER_PAY') {
      await refresh()
      throw new Error('当前订单已不能付款，请查看最新状态')
    }
    paymentOrder.value = result.data
    await nextTick()
    cashier.value?.showModal()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '无法连接服务'
  }
}

/** 模拟渠道结果由后端处理，网络异常时允许使用同一订单重试。 */
async function pay(success: boolean) {
  if (paying.value || !paymentOrder.value || paymentSucceeded.value) return
  paying.value = true
  paymentNotice.value = ''
  try {
    const result = await send<Order>('/api/payment/mock/pay', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderNumber: paymentOrder.value.orderNumber, success }),
    })
    if (!alive) return
    if (result.status === 404) throw new Error('模拟支付未启用，请使用 dev 或 test 配置启动后端')
    if (result.status === 401) {
      cashier.value?.close()
      return
    }
    if (result.data?.status === 'TRADE_PAID_SUCCESS' && result.ok) {
      paymentSucceeded.value = true
      paymentOrder.value = result.data
      paymentNotice.value = '模拟支付成功，订单已付款'
    } else {
      paymentNotice.value = result.message || '支付未完成，请查看订单状态后重试'
    }
    await refresh()
  } catch (reason) {
    if (alive) paymentNotice.value = reason instanceof Error ? reason.message : '请求未确认，可重试查询付款结果'
  } finally {
    paying.value = false
  }
}

/** 请求提交期间阻止关闭收银台，避免用户误以为付款已取消。 */
function preventPendingClose(event: Event) {
  if (paying.value) event.preventDefault()
}

const now = ref(Date.now())
let timer: ReturnType<typeof setInterval>
let alive = true

/** 以服务端截止时间显示倒计时，归零后等待服务端确认关闭。 */
function remaining(order: Order) {
  if (!order.expiresAt) return '等待付款'
  const seconds = Math.ceil((new Date(order.expiresAt).getTime()-now.value)/1000)
  if (seconds <= 0) return '已到付款截止时间，正在等待系统确认'
  return '剩余 '+Math.floor(seconds/60)+' 分 '+seconds%60+' 秒'
}

/** 查询当前页及选中的订单，自动刷新时保留页面内容。 */
async function refresh(silent = false) {
  if (loading.value) return
  loading.value = true
  if (!silent) error.value = ''
  try {
    const query = new URLSearchParams({ pageNum:String(page.value) })
    if (status.value) query.set('status',status.value)
    const result = await send<Page>('/api/order/mine?'+query)
    if (!alive) return
    if (!result.ok || !result.data) throw new Error(result.message || '订单加载失败')
    orders.value = result.data.data
    pages.value = result.data.totalPage
    if (typeof route.query.number === 'string') await showDetail(route.query.number)
    error.value = ''
  } catch(reason) {
    if (alive) error.value = reason instanceof Error ? reason.message : '无法连接服务'
  } finally {
    loading.value = false
  }
}

/** 查询所选订单的服务端详情，失败时不显示上笔订单。 */
async function showDetail(number: string) {
  const result = await send<Order>('/api/order/'+encodeURIComponent(number))
  if (!alive) return
  if (!result.ok || !result.data) {
    detail.value = null
    throw new Error(result.message || '无法读取订单详情')
  }
  detail.value = result.data
}

/** 点击订单后将订单号写入地址，刷新页面仍可恢复。 */
async function openOrder(order: Order) {
  await router.replace({ path:'/orders',query:{number:order.orderNumber} })
  try { await showDetail(order.orderNumber) }
  catch(reason) { error.value = reason instanceof Error ? reason.message : '无法读取订单' }
}

/** 筛选与翻页后重新读取列表。 */
function changePage(next: number) {
  page.value = next
  void refresh()
}

/** 确认取消后调用后端，再重新读取订单状态。 */
async function cancel() {
  const order = confirmOrder.value
  if (!order || cancelling.value) return
  cancelling.value = order.orderNumber
  try {
    const result = await send<void>('/api/order/'+encodeURIComponent(order.orderNumber)+'/cancel',{method:'POST'})
    if (!result.ok) throw new Error(result.message || '取消失败')
    confirmOrder.value = null
    await refresh()
  } catch(reason) {
    error.value = reason instanceof Error ? reason.message : '无法连接服务'
  } finally { cancelling.value = '' }
}

/** 页面重新可见时校准订单状态。 */
function onVisible() {
  if (document.visibilityState === 'visible') void refresh(true)
}

/** 定时刷新待付款状态，倒计时本身不改变订单状态。 */
onMounted(() => {
  void refresh()
  let ticks = 0
  timer = setInterval(() => {
    now.value = Date.now()
    if (++ticks % 10 === 0 && document.visibilityState === 'visible') void refresh(true)
  },1000)
  document.addEventListener('visibilitychange',onVisible)
})
/** 离开页面时停止轮询与页面监听。 */
onUnmounted(() => {
  alive = false
  clearInterval(timer)
  document.removeEventListener('visibilitychange',onVisible)
})
</script>

<template>
  <section>
    <h1>我的订单</h1>
    <label>订单状态 <select v-model="status" :disabled="loading" @change="changePage(1)">
      <option value="">全部</option>
      <option v-for="(label,key) in labels" :key="key" :value="key">{{ label }}</option>
    </select></label>
    <p v-if="error" class="notice error" role="alert">{{ error }} <button @click="refresh()">重试</button></p>
    <p v-if="loading" role="status">正在更新订单…</p>
    <p v-if="!loading && !orders.length && !error">暂无订单，<RouterLink to="/">去商城逛逛</RouterLink></p>
    <article v-for="order in orders" :key="order.orderNumber" class="order-item">
      <div><strong>{{ order.productName || '历史商品' }}</strong> <span>{{ labels[order.status] || order.status }}</span></div>
      <p>{{ order.skuSnapshot || '历史规格' }} · 数量 {{ order.quantity }} · ¥{{ Number(order.totalPrice).toFixed(2) }}</p>
      <p class="muted">订单号：{{ order.orderNumber }}</p>
      <p v-if="order.status === 'WAIT_BUYER_PAY'">{{ remaining(order) }}</p>
      <button @click="openOrder(order)">查看详情</button>
      <button v-if="order.status === 'WAIT_BUYER_PAY'" :disabled="!!cancelling || paying" @click="openCashier(order)">去付款</button>
      <button v-if="order.status === 'WAIT_BUYER_PAY'" :disabled="!!cancelling || paying" @click="confirmOrder=order">取消订单</button>
    </article>
    <div v-if="pages > 1" class="order-actions">
      <button :disabled="page <= 1 || loading" @click="changePage(page-1)">上一页</button>
      <span>第 {{ page }} / {{ pages }} 页</span>
      <button :disabled="page >= pages || loading" @click="changePage(page+1)">下一页</button>
    </div>
    <section v-if="detail" class="order-item" aria-labelledby="order-detail">
      <h2 id="order-detail">订单详情</h2>
      <p>订单号：{{ detail.orderNumber }}</p>
      <p>状态：{{ labels[detail.status] || detail.status }}</p>
      <p>{{ detail.productName || '历史商品' }} · {{ detail.skuSnapshot }}</p>
      <p>数量：{{ detail.quantity }} · 成交单价：{{ detail.unitPrice ?? '暂无' }}</p>
      <p>金额：¥{{ Number(detail.totalPrice).toFixed(2) }}</p>
      <p>收货人：{{ detail.recipientName || '未记录' }} {{ detail.recipientPhone }}</p>
      <p>收货地址：{{ detail.shippingAddress || '未记录' }}</p>
      <p>创建时间：{{ detail.createdAt?.replace('T',' ') || '暂无' }}</p>
      <p>付款截止：{{ detail.expiresAt?.replace('T',' ') || '暂无' }}</p>
      <button v-if="detail.status === 'WAIT_BUYER_PAY'" :disabled="paying || !!cancelling" @click="openCashier(detail)">去付款</button>
    </section>
    <dialog ref="cashier" class="cashier-dialog" aria-labelledby="cashier-title" @cancel="preventPendingClose">
      <template v-if="paymentOrder">
        <h2 id="cashier-title">开发模拟收银台</h2>
        <p class="muted">仅模拟支付结果，不产生真实扣款。</p>
        <p>订单号：{{ paymentOrder.orderNumber }}</p>
        <p class="product-price">¥{{ Number(paymentOrder.totalPrice).toFixed(2) }}</p>
        <p v-if="paymentNotice" :class="paymentSucceeded ? 'notice success' : 'notice warning'" role="status">{{ paymentNotice }}</p>
        <div class="cashier-actions">
          <template v-if="!paymentSucceeded">
            <button :disabled="paying" @click="pay(true)">{{ paying ? '正在处理…' : '模拟支付成功' }}</button>
            <button :disabled="paying" @click="pay(false)">模拟支付失败</button>
          </template>
          <button :disabled="paying" @click="cashier?.close()">{{ paymentSucceeded ? '完成' : '返回订单' }}</button>
        </div>
      </template>
    </dialog>
    <div v-if="confirmOrder" class="confirm-overlay">
      <section class="confirm-box" role="dialog" aria-modal="true" aria-labelledby="cancel-title">
        <h2 id="cancel-title">确认取消订单？</h2><p>取消后，预占库存会释放。</p>
        <button :disabled="!!cancelling" @click="confirmOrder=null">暂不取消</button>
        <button :disabled="!!cancelling" @click="cancel">{{ cancelling ? '正在取消…' : '确认取消' }}</button>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { send } from './api'

const shopId = ref<number>()
const name = ref('')
const intro = ref('')
const images = ref([''])
let nextKey = 1
const newSku = () => ({ key: nextKey++, style: '标准款', size: '', price: '', stock: '0' })
const skus = ref([newSku()])
const busy = ref(false)
const error = ref('')
const productId = ref('')
const locked = computed(() => busy.value || !!productId.value)
function imageUrl(value: string) {
  const url = value.trim()
  if (!url || /[,\\\s]/.test(url)) return ''
  if (url.startsWith('/') && !url.startsWith('//')) return url
  try { const parsed = new URL(url); return ['http:', 'https:'].includes(parsed.protocol) ? url : '' } catch { return '' }
}
async function submit() {
  if (locked.value) return
  error.value = ''
  const urls = images.value.map(value => imageUrl(value))
  if (!Number.isSafeInteger(shopId.value) || !shopId.value || shopId.value < 1) { error.value = '请输入有效的店铺 ID'; return }
  if (!name.value.trim()) { error.value = '请填写商品名称'; return }
  if (urls.some(url => !url) || urls.join(',').length > 1024) { error.value = '请填写有效图片地址，地址总长度不能超过 1024 个字符'; return }
  const keys = new Set<string>()
  for (const sku of skus.value) {
    if (!sku.style.trim() || !/^\d+(\.\d{1,2})?$/.test(sku.size) || Number(sku.size) <= 0 || Number(sku.size) > 999999.99
      || !/^\d+(\.\d{1,2})?$/.test(sku.price) || Number(sku.price) <= 0 || Number(sku.price) > 99999999.99
      || !/^\d+$/.test(sku.stock) || Number(sku.stock) > 2147483647) {
      error.value = '请检查规格：款式必填，尺码和价格须为正数且最多两位小数，库存须为非负整数'; return
    }
    const key = sku.style.trim().toLowerCase() + '|' + Number(sku.size)
    if (keys.has(key)) { error.value = '款式和尺码组合不能重复'; return }
    keys.add(key)
  }
  busy.value = true
  try {
    const result = await send<{ productId: string }>('/api/merchant/products', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ shopId: shopId.value, name: name.value.trim(), productIntro: intro.value.trim(), productImgs: urls,
        skus: skus.value.map(sku => ({ style: sku.style.trim(), size: Number(sku.size), price: Number(sku.price), stock: Number(sku.stock) })) }),
    })
    if (!result.ok || !result.data?.productId) throw new Error(result.message || '添加失败，请稍后重试')
    productId.value = result.data.productId
  } catch (reason) { error.value = reason instanceof Error ? reason.message : '无法连接服务' }
  finally { busy.value = false }
}
function reset() {
  productId.value = ''; name.value = ''; intro.value = ''; images.value = ['']; skus.value = [newSku()]; error.value = ''
}
</script>

<template>
  <section class="add-product">
    <h1>添加商品</h1>
    <p class="muted">完善商品信息与规格，让顾客发现你的好物。</p>
    <form @submit.prevent="submit">
      <fieldset :disabled="locked">
        <legend>基本信息</legend>
        <label for="product-shop">店铺 ID</label>
        <input id="product-shop" v-model.number="shopId" type="number" min="1" step="1" required placeholder="请输入本人名下的有效店铺 ID">
        <label for="product-name">商品名称</label>
        <input id="product-name" v-model="name" maxlength="64" required>
        <label for="product-intro">商品介绍</label>
        <textarea id="product-intro" v-model="intro" maxlength="256" rows="3" placeholder="描述商品特点（选填）" />
      </fieldset>
      <fieldset :disabled="locked">
        <legend>商品图片</legend>
        <p class="muted">支持站内路径或 HTTP(S) 图片地址，第一张为主图，最多 9 张。</p>
        <div v-for="(_, index) in images" :key="index" class="image-row">
          <label :for="'product-image-' + index">图片 {{ index + 1 }}</label>
          <input :id="'product-image-' + index" v-model="images[index]" required maxlength="1024" placeholder="https://… 或 /images/…">
          <img v-if="imageUrl(images[index] || '')" :src="imageUrl(images[index] || '')" :alt="'图片 ' + (index + 1) + ' 预览'" referrerpolicy="no-referrer">
          <button type="button" :disabled="images.length === 1" @click="images.splice(index, 1)">移除图片 {{ index + 1 }}</button>
        </div>
        <button type="button" :disabled="images.length >= 9" @click="images.push('')">＋ 添加图片</button>
      </fieldset>
      <fieldset :disabled="locked">
        <legend>规格与库存</legend>
        <p class="muted">每行是一组款式与尺码，最多 200 组；没有款式区别时填写「标准款」。</p>
        <div v-for="(sku, index) in skus" :key="sku.key" class="sku-editor">
          <label>款式<input v-model="sku.style" maxlength="64" required></label>
          <label>尺码<input v-model="sku.size" type="number" min="0.01" max="999999.99" step="0.01" required></label>
          <label>价格（元）<input v-model="sku.price" type="number" min="0.01" max="99999999.99" step="0.01" required></label>
          <label>库存<input v-model="sku.stock" type="number" min="0" max="2147483647" step="1" required></label>
          <button type="button" :disabled="skus.length === 1" :aria-label="'移除规格 ' + (index + 1)" @click="skus.splice(index, 1)">移除</button>
        </div>
        <button type="button" :disabled="skus.length >= 200" @click="skus.push(newSku())">＋ 添加规格</button>
      </fieldset>
      <p v-if="error" class="notice error" role="alert">{{ error }}</p>
      <button v-if="!productId" type="submit" class="primary" :disabled="busy">{{ busy ? '正在添加…' : '添加商品' }}</button>
    </form>
    <div v-if="productId" class="notice success" role="status">
      <p>商品添加成功。</p>
      <RouterLink :to="{ name: 'product-detail', params: { id: productId } }">查看商品</RouterLink>
      <button type="button" @click="reset">继续添加</button>
    </div>
  </section>
</template>

<style scoped>
.add-product { max-width:960px; }
fieldset { min-width:0; margin:16px 0; padding:20px; border:1px solid #dce4d9; border-radius:14px; }
legend { color:#24513a; font-weight:700; padding:0 8px; }
fieldset > label { display:block; margin:12px 0 6px; }
textarea { width:100%; padding:12px; border:1px solid #aebdaf; border-radius:10px; font:inherit; resize:vertical; }
.image-row { display:flex; align-items:center; gap:12px; flex-wrap:wrap; margin:16px 0; }
.image-row input { flex:1; min-width:160px; margin:0; }
.image-row img { width:64px; height:64px; object-fit:contain; border:1px solid #dce4d9; border-radius:8px; }
.sku-editor { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)) auto; gap:12px; align-items:end; padding:16px 0; border-bottom:1px solid #e3e7de; margin-bottom:16px; }
.sku-editor input { display:block; margin:6px 0 0; }
.sku-editor button { margin-bottom:1px; }
.success button { margin-left:16px; }
@media(max-width:800px) { .sku-editor { grid-template-columns:repeat(2,minmax(0,1fr)); } fieldset { padding:14px; } }
</style>

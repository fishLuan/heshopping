<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { send } from './api'

/** 带实时商品信息的购物车条目。 */
interface Item {
  id:number
  skuId:string
  quantity:number
  available:boolean
  unavailableReason:string|null
  product:null|{productId:string;name:string;image:string;size:string;price:number;stock:number;shopName:string}
}
const items=ref<Item[]>([])
const quantities=ref<Record<number,number>>({})
const loading=ref(false)
const busy=ref(false)
const error=ref('')
const notice=ref('')
const deletion=ref<HTMLDialogElement|null>(null)
const selected=ref<Item|null>(null)

/** 加载本人购物车，并初始化可编辑数量。 */
async function load() {
  loading.value=true
  error.value=''
  try {
    const result=await send<Item[]>('/api/cart')
    if(!result.ok) throw new Error(result.message || '购物车加载失败')
    items.value=result.data ?? []
    quantities.value=Object.fromEntries(items.value.map(item=>[item.id,item.quantity]))
  } catch(reason) { error.value=reason instanceof Error?reason.message:'无法连接服务' }
  finally { loading.value=false }
}

/** 保存数量或删除条目后重新查询，不在前端假定保存成功。 */
async function mutate(item:Item,remove=false) {
  if(busy.value) return
  if(!remove && (!Number.isInteger(quantities.value[item.id]) || quantities.value[item.id]<1)) {
    error.value='请输入正整数数量'
    return
  }
  busy.value=true
  error.value=''
  notice.value=''
  try {
    const result=await send<void>('/api/cart/'+item.id,{
      method:remove?'DELETE':'PUT',
      headers:{'Content-Type':'application/json'},
      body:remove?undefined:JSON.stringify({quantity:quantities.value[item.id]}),
    })
    if(!result.ok) throw new Error(result.message || '操作失败')
    deletion.value?.close()
    notice.value=remove?'商品已移出购物车':'数量已更新'
    await load()
  } catch(reason) { error.value=reason instanceof Error?reason.message:'无法连接服务' }
  finally { busy.value=false }
}

/** 显示删除确认，原生对话框负责焦点管理。 */
function askDelete(item:Item) {
  selected.value=item
  deletion.value?.showModal()
}

/** 提交期间阻止意外关闭。 */
function preventClose(event:Event) { if(busy.value) event.preventDefault() }
onMounted(load)
</script>

<template>
  <section>
    <h1>购物车</h1>
    <p class="muted">商品价格和库存以确认下单时为准。</p>
    <p v-if="error" class="notice error" role="alert">{{ error }} <button @click="load">重新加载</button></p>
    <p v-if="notice" class="notice success" role="status">{{ notice }}</p>
    <p v-if="loading" role="status">正在加载…</p>
    <p v-else-if="!items.length && !error">购物车还是空的，<RouterLink to="/">去逛逛</RouterLink></p>
    <article v-for="item in items" :key="item.id" class="cart-row">
      <img :src="item.product?.image?.split(',')[0] || '/images/default-product.png'" :alt="item.product?.name || '失效商品'">
      <div class="cart-info">
        <h2>{{ item.product?.name || '商品已失效' }}</h2>
        <p>{{ item.product?.shopName }} · 尺码 {{ item.product?.size || '暂无' }}</p>
        <p v-if="item.product">¥{{ Number(item.product.price).toFixed(2) }} · 当前库存 {{ item.product.stock }}</p>
        <p v-if="!item.available" class="notice warning">{{ item.unavailableReason }}</p>
        <label :for="'qty-'+item.id">数量</label>
        <input :id="'qty-'+item.id" v-model.number="quantities[item.id]" type="number" min="1" max="9999" :disabled="busy">
        <div class="address-actions">
          <button :disabled="busy || !item.product" @click="mutate(item)">保存数量</button>
          <button :disabled="busy" @click="askDelete(item)">删除</button>
          <RouterLink v-if="item.available && item.product"
            :to="{name:'product-detail',params:{id:item.product.productId},query:{sku:item.skuId,quantity:item.quantity}}">
            去确认购买
          </RouterLink>
        </div>
      </div>
    </article>
    <dialog ref="deletion" class="address-dialog" aria-labelledby="cart-delete-title" @cancel="preventClose">
      <h2 id="cart-delete-title">移出购物车？</h2>
      <p>{{ selected?.product?.name || '失效商品' }}</p>
      <p v-if="error" role="alert">{{ error }}</p>
      <div class="address-actions">
        <button :disabled="busy" @click="deletion?.close()">保留</button>
        <button :disabled="busy" @click="selected && mutate(selected,true)">{{ busy?'处理中…':'确认删除' }}</button>
      </div>
    </dialog>
  </section>
</template>

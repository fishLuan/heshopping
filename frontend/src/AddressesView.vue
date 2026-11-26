<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { send } from './api'

/** 收货地址表单和接口数据。 */
interface Address {
  id?: number
  recipientName: string
  recipientPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: boolean
}
const addresses = ref<Address[]>([])
const loading = ref(false)
const busy = ref(false)
const error = ref('')
const notice = ref('')
const editor = ref<HTMLDialogElement | null>(null)
const deletion = ref<HTMLDialogElement | null>(null)
const selected = ref<Address | null>(null)
const form = ref<Address>(empty())

/** 生成独立的空地址表单。 */
function empty(): Address {
  return { recipientName:'',recipientPhone:'',province:'',city:'',district:'',detailAddress:'',isDefault:false }
}

/** 读取服务端地址列表，默认地址优先显示。 */
async function load() {
  loading.value = true
  try {
    const result = await send<Address[]>('/api/addresses')
    if (!result.ok) throw new Error(result.message || '地址加载失败')
    addresses.value = result.data ?? []
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '无法连接服务'
  } finally { loading.value = false }
}

/** 打开新增或编辑窗口，复制数据避免未保存修改影响列表。 */
function edit(address?: Address) {
  form.value = address ? { ...address } : empty()
  error.value = ''
  notice.value = ''
  editor.value?.showModal()
}

/** 提交完整地址，由后端确定归属并管理默认地址。 */
async function save() {
  if (busy.value) return
  busy.value = true
  error.value = ''
  try {
    const { id, ...body } = form.value
    const result = await send<Address>('/api/addresses'+(id ? '/'+id : ''), {
      method:id ? 'PUT' : 'POST',
      headers:{'Content-Type':'application/json'},
      body:JSON.stringify(body),
    })
    if (!result.ok) throw new Error(result.message || '保存失败')
    editor.value?.close()
    notice.value = '地址已保存'
    await load()
  } catch(reason) {
    error.value = reason instanceof Error ? reason.message : '无法连接服务'
  } finally { busy.value = false }
}

/** 打开删除确认窗口。 */
function askDelete(address: Address) {
  selected.value = address
  error.value = ''
  deletion.value?.showModal()
}

/** 删除本人地址，完成后重新读取默认地址分配结果。 */
async function remove() {
  if (busy.value || !selected.value) return
  busy.value = true
  error.value = ''
  try {
    const result = await send<void>('/api/addresses/'+selected.value.id,{method:'DELETE'})
    if (!result.ok) throw new Error(result.message || '删除失败')
    deletion.value?.close()
    notice.value = '地址已删除'
    await load()
  } catch(reason) {
    error.value = reason instanceof Error ? reason.message : '无法连接服务'
  } finally { busy.value = false }
}

/** 请求后端原子切换默认地址。 */
async function setDefault(address: Address) {
  if (busy.value) return
  busy.value = true
  error.value = ''
  try {
    const result = await send<void>('/api/addresses/'+address.id+'/default',{method:'PUT'})
    if (!result.ok) throw new Error(result.message || '设置失败')
    notice.value = '默认地址已更新'
    await load()
  } catch(reason) {
    error.value = reason instanceof Error ? reason.message : '无法连接服务'
  } finally { busy.value = false }
}

/** 保存或删除提交期间防止意外关闭确认窗口。 */
function preventBusyClose(event: Event) {
  if (busy.value) event.preventDefault()
}
onMounted(load)
</script>

<template>
  <section>
    <div class="address-heading"><h1>收货地址</h1><button :disabled="busy" @click="edit()">新增地址</button></div>
    <p v-if="error && !editor?.open && !deletion?.open" class="notice error" role="alert">{{ error }} <button @click="load">重试</button></p>
    <p v-if="notice" class="notice success" role="status">{{ notice }}</p>
    <p v-if="loading" role="status">正在加载地址…</p>
    <p v-else-if="!addresses.length && !error" class="muted">还没有收货地址，添加一个吧。</p>
    <article v-for="address in addresses" :key="address.id" class="address-item">
      <h2>{{ address.recipientName }} <span v-if="address.isDefault" class="default-address">默认地址</span></h2>
      <p>{{ address.recipientPhone }}</p>
      <p>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}</p>
      <div class="address-actions">
        <button :disabled="busy" @click="edit(address)">编辑</button>
        <button v-if="!address.isDefault" :disabled="busy" @click="setDefault(address)">设为默认</button>
        <button :disabled="busy" @click="askDelete(address)">删除</button>
      </div>
    </article>

    <dialog ref="editor" class="address-dialog" aria-labelledby="address-title" @cancel="preventBusyClose">
      <h2 id="address-title">{{ form.id ? '编辑地址' : '新增地址' }}</h2>
      <form class="address-form" @submit.prevent="save">
        <label for="recipient">收货人</label>
        <input id="recipient" v-model="form.recipientName" maxlength="50" autocomplete="shipping name" required :disabled="busy">
        <label for="phone">联系电话</label>
        <input id="phone" v-model="form.recipientPhone" type="tel" maxlength="32" autocomplete="shipping tel" required :disabled="busy">
        <div class="address-region">
          <label>省<input v-model="form.province" maxlength="50" autocomplete="shipping address-level1" required :disabled="busy"></label>
          <label>市<input v-model="form.city" maxlength="50" autocomplete="shipping address-level2" required :disabled="busy"></label>
          <label>区县<input v-model="form.district" maxlength="50" autocomplete="shipping address-level3" required :disabled="busy"></label>
        </div>
        <label for="street">详细地址</label>
        <textarea id="street" v-model="form.detailAddress" maxlength="255" rows="3" autocomplete="shipping street-address" required :disabled="busy"></textarea>
        <label class="address-check"><input v-model="form.isDefault" type="checkbox" :disabled="busy">设为默认地址</label>
        <p v-if="error" class="notice error" role="alert">{{ error }}</p>
        <div class="address-actions">
          <button type="button" :disabled="busy" @click="editor?.close()">取消</button>
          <button type="submit" :disabled="busy">{{ busy ? '正在保存…' : '保存地址' }}</button>
        </div>
      </form>
    </dialog>
    <dialog ref="deletion" class="address-dialog" aria-labelledby="delete-address-title" @cancel="preventBusyClose">
      <h2 id="delete-address-title">删除这个地址？</h2>
      <p>{{ selected?.recipientName }} · {{ selected?.detailAddress }}</p>
      <p v-if="error" class="notice error" role="alert">{{ error }}</p>
      <div class="address-actions">
        <button :disabled="busy" @click="deletion?.close()">保留</button>
        <button :disabled="busy" @click="remove">{{ busy ? '正在删除…' : '确认删除' }}</button>
      </div>
    </dialog>
  </section>
</template>

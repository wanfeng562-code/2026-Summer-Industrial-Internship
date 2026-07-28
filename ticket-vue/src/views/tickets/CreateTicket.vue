<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
const formRef = ref<FormInstance>()

import { requestOrdersList, requestCreateTicket } from '@/api/ticket'   // @/   src目录下  
import type { Orders } from '@/api/ticket/type'

const loading = ref(false)
const router = useRouter()
const route = useRoute()

const orderData = ref<Array<Orders>>([])

const form = reactive({
  orderId: null as number | null,
  title: '',
  description: '',
  category: ''
})

const rules = {
  orderId: [{ required: true, message: '请选择关联订单', trigger: 'change' }],
  title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }]
}

const categoryOptions = [
  { label: '退款退货', value: 'REFUND' },
  { label: '物流异常', value: 'LOGISTICS' },
  { label: '商品破损', value: 'DAMAGE' },
  { label: '发票问题', value: 'INVOICE' },
  { label: '其他', value: 'OTHER' }
]

onMounted(async () => {
  if (typeof route.query.description === 'string') {
    form.description = route.query.description.slice(0, 2000)
    form.title = route.query.description.slice(0, 100)
  }
  const queryOrderId = route.query.orderId ?? route.query.ordersId
  if (typeof queryOrderId === 'string') {
    const id = Number(queryOrderId)
    if (Number.isFinite(id)) form.orderId = id
  }
  await listOrders()
})

const listOrders = async () =>{
  try{
    const res = await requestOrdersList()
    if(res.code == 200){
      orderData.value = res.data.records
    }

  }finally{

  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || form.orderId === null) return

  loading.value = true
  try {
    const res = await requestCreateTicket({
      orderId: form.orderId,
      title: form.title,
      description: form.description,
      category: form.category,
    })
    if(res.code == 200){
      ElMessage.success('工单创建成功')
      router.push(`/home/tickets/${res.data.id}`)
    }else{
      ElMessage.error('工单创建失败')
    }
    
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="ticket-create">
    <el-card>
      <template #header>
        <span>创建工单</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 700px;">
        <el-form-item label="关联订单" prop="orderId">
          <el-select v-model="form.orderId" placeholder="请选择关联订单" filterable style="width: 100%;">
            <el-option v-for="item in orderData" :key="item.id" :value="item.id" :label="`${item.orderNo} - ${item.productName} (¥${item.totalAmount})`" />
          </el-select>
        </el-form-item>
        <el-form-item label="工单标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入工单标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="问题分类" prop="category">
          <el-select v-model="form.category" placeholder="留空将由AI自动识别" clearable style="width: 100%;">
            <el-option  v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="请详细描述您遇到的问题..." maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交工单</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.ticket-create {
  max-width: 800px;
}
</style>

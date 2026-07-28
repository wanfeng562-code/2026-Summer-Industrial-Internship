<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { requestOrdersList, requestCreateTicket } from '@/api/ticket'
import type { Orders, TicketCreateRequest } from '@/api/ticket/type'

const formRef = ref<FormInstance>()
const loading = ref(false)
const router = useRouter()
const route = useRoute()
const orderData = ref<Orders[]>([])

const form = reactive<{
  ordersId: number | undefined
  title: string
  description: string
  category: string
}>({
  ordersId: undefined,
  title: '',
  description: '',
  category: '',
})

const rules: FormRules = {
  ordersId: [{ required: true, message: '请选择关联订单', trigger: 'change' }],
  title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
}

const categoryOptions = [
  { label: '退款退货', value: 'REFUND' },
  { label: '物流异常', value: 'LOGISTICS' },
  { label: '商品破损', value: 'DAMAGE' },
  { label: '发票问题', value: 'INVOICE' },
  { label: '其他', value: 'OTHER' },
]

const listOrders = async () => {
  const res = await requestOrdersList(1, 50)
  if (res.code === 200) {
    orderData.value = res.data.records
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || form.ordersId == null) return

  loading.value = true
  try {
    const payload: TicketCreateRequest = {
      ordersId: form.ordersId,
      title: form.title,
      description: form.description,
      category: form.category || undefined,
    }
    const res = await requestCreateTicket(payload)
    if (res.code === 200) {
      ElMessage.success('工单创建成功')
      const id = res.data?.id
      if (id != null) {
        router.push(`/home/tickets/${id}`)
      } else {
        router.push('/home/tickets')
      }
    } else {
      ElMessage.error(res.msg || '工单创建失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await listOrders()
  const q = route.query.ordersId
  if (q != null) {
    const id = Number(Array.isArray(q) ? q[0] : q)
    if (Number.isFinite(id)) {
      form.ordersId = id
    }
  }
})
</script>

<template>
  <div class="ticket-create">
    <el-card>
      <template #header>
        <span>创建工单</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 700px">
        <el-form-item label="关联订单" prop="ordersId">
          <el-select v-model="form.ordersId" placeholder="请选择关联订单" filterable style="width: 100%">
            <el-option
              v-for="item in orderData"
              :key="item.id"
              :value="item.id!"
              :label="`${item.orderNo} - ${item.productName} (¥${item.totalAmount})`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工单标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入工单标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="问题分类" prop="category">
          <el-select v-model="form.category" placeholder="留空将由AI自动识别" clearable style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您遇到的问题..."
            maxlength="2000"
            show-word-limit
          />
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

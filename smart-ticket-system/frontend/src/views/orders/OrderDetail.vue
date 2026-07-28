<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { requestOrderDetail } from '@/api/ticket'
import type { OrderVo } from '@/api/ticket/type'
import ErrorState from '@/components/ErrorState.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const order = ref<OrderVo | null>(null)

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!Number.isFinite(id)) {
    errorMsg.value = '无效的订单 ID'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await requestOrderDetail(id)
    if (res.code === 200) {
      order.value = res.data
    } else {
      errorMsg.value = res.msg || '加载订单详情失败'
      ElMessage.error(errorMsg.value)
    }
  } catch {
    errorMsg.value = '网络异常，订单详情加载失败'
  } finally {
    loading.value = false
  }
}

const createTicket = () => {
  if (!order.value?.id) return
  router.push({ path: '/home/create', query: { ordersId: String(order.value.id) } })
}

onMounted(loadDetail)
</script>

<template>
  <div v-loading="loading">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>订单详情</span>
          <div>
            <el-button @click="router.push('/home/orders')">返回列表</el-button>
            <el-button type="primary" :disabled="!order?.id" @click="createTicket">从订单创建工单</el-button>
          </div>
        </div>
      </template>

      <ErrorState v-if="errorMsg" :message="errorMsg" @retry="loadDetail" />

      <el-descriptions v-else-if="order" :column="2" border>
        <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ order.username || order.userId }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ order.productName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ order.quantity }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ order.unitPrice }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">{{ order.orderStatus }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ order.paymentStatus }}</el-descriptions-item>
        <el-descriptions-item label="物流状态">{{ order.logisticsStatus }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ order.logisticsNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ order.orderTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ order.payTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>

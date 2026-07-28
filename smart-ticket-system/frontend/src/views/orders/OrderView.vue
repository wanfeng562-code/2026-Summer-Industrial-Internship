<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { requestOrdersList } from '@/api/ticket'
import type { Page, Orders } from '@/api/ticket/type'
import ErrorState from '@/components/ErrorState.vue'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')

const orderData = reactive<Page<Orders>>({
  records: [],
  total: 0,
  size: 10,
  current: 1,
  pages: 0,
})

const orderStatusName = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: '待付款',
    PAID: '已付款',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return map[status || ''] || status || '-'
}

const paymentStatusName = (status?: string) => {
  const map: Record<string, string> = {
    UNPAID: '未支付',
    PAID: '已支付',
    REFUNDED: '已退款',
  }
  return map[status || ''] || status || '-'
}

const loadOrders = async (page = 1) => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await requestOrdersList(page, orderData.size)
    if (res.code === 200) {
      orderData.current = res.data.current
      orderData.size = res.data.size
      orderData.total = res.data.total
      orderData.records = res.data.records
      orderData.pages = res.data.pages
    } else {
      errorMsg.value = res.msg || '加载订单失败'
      ElMessage.error(errorMsg.value)
    }
  } catch {
    errorMsg.value = '网络异常，订单列表加载失败'
  } finally {
    loading.value = false
  }
}

const toDetail = (id?: number) => {
  if (id == null) return
  router.push(`/home/orders/${id}`)
}

const createTicket = (id?: number) => {
  if (id == null) return
  router.push({ path: '/home/create', query: { ordersId: String(id) } })
}

onMounted(() => loadOrders(1))
</script>

<template>
  <div v-loading="loading">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>我的订单</span>
          <el-button type="primary" @click="loadOrders(orderData.current)">刷新</el-button>
        </div>
      </template>

      <ErrorState v-if="errorMsg" :message="errorMsg" @retry="loadOrders(orderData.current)" />

      <template v-else>
        <el-table :data="orderData.records" style="width: 100%" empty-text="暂无订单数据">
          <el-table-column prop="orderNo" label="订单编号" min-width="140" />
          <el-table-column prop="productName" label="商品" min-width="160" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="totalAmount" label="金额" width="100">
            <template #default="{ row }">¥{{ row.totalAmount }}</template>
          </el-table-column>
          <el-table-column prop="orderStatus" label="订单状态" width="100">
            <template #default="{ row }">{{ orderStatusName(row.orderStatus) }}</template>
          </el-table-column>
          <el-table-column prop="paymentStatus" label="支付状态" width="100">
            <template #default="{ row }">{{ paymentStatusName(row.paymentStatus) }}</template>
          </el-table-column>
          <el-table-column prop="orderTime" label="下单时间" min-width="160" />
          <el-table-column fixed="right" label="操作" width="200">
            <template #default="{ row }">
              <el-button link type="primary" @click="toDetail(row.id)">详情</el-button>
              <el-button link type="success" @click="createTicket(row.id)">创建工单</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pager"
          layout="total, prev, pager, next"
          :current-page="orderData.current"
          :page-size="orderData.size"
          :total="orderData.total"
          @current-change="loadOrders"
        />
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>

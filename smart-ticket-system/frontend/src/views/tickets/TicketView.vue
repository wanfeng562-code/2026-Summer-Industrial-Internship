<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { requestTicketPage } from '@/api/ticket'
import type { Page, TicketVo } from '@/api/ticket/type'
import ErrorState from '@/components/ErrorState.vue'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const keyword = ref('')
const statusFilter = ref('')

const ticketData = reactive<Page<TicketVo>>({
  records: [],
  total: 0,
  size: 10,
  current: 1,
  pages: 0,
})

const filteredRecords = () => {
  let list = ticketData.records
  if (statusFilter.value) {
    list = list.filter((t) => t.status === statusFilter.value)
  }
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    list = list.filter(
      (t) =>
        t.ticketNo?.toLowerCase().includes(kw) ||
        t.title?.toLowerCase().includes(kw) ||
        t.username?.toLowerCase().includes(kw),
    )
  }
  return list
}

const toTicketDetail = (id?: number) => {
  if (id == null) return
  router.push(`/home/tickets/${id}`)
}

const pageTicketPage = async (current: number) => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await requestTicketPage(current, ticketData.size)
    if (res.code === 200) {
      ticketData.current = res.data.current
      ticketData.size = res.data.size
      ticketData.total = res.data.total
      ticketData.records = res.data.records
      ticketData.pages = res.data.pages
    } else {
      errorMsg.value = res.msg || '加载工单失败'
      ElMessage.error(errorMsg.value)
    }
  } catch {
    errorMsg.value = '网络异常，工单列表加载失败'
  } finally {
    loading.value = false
  }
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'info',
    AI_PROCESSING: 'warning',
    MANUAL_REVIEW: '',
    RESOLVED: 'success',
    CLOSED: 'danger',
  }
  return map[status] || 'info'
}

const statusTagName = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    AI_PROCESSING: 'AI预处理中',
    MANUAL_REVIEW: '人工复核',
    RESOLVED: '已解决',
    CLOSED: '已关闭',
  }
  return map[status] || status || '-'
}

const categoryName = (category: string) => {
  const map: Record<string, string> = {
    REFUND: '退款退货',
    LOGISTICS: '物流异常',
    DAMAGE: '商品破损',
    INVOICE: '发票问题',
    OTHER: '其他',
  }
  return map[category] || category || '-'
}

const priorityName = (priority: string) => {
  const map: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急',
  }
  return map[priority] || priority || '-'
}

onMounted(() => pageTicketPage(1))
</script>

<template>
  <div v-loading="loading">
    <el-card>
      <div class="toolbar">
        <el-input v-model="keyword" style="width: 240px" clearable placeholder="搜索编号/标题/用户" />
        <el-select v-model="statusFilter" clearable placeholder="状态筛选" style="width: 160px">
          <el-option label="待处理" value="PENDING" />
          <el-option label="AI预处理中" value="AI_PROCESSING" />
          <el-option label="人工复核" value="MANUAL_REVIEW" />
          <el-option label="已解决" value="RESOLVED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button type="primary" @click="pageTicketPage(ticketData.current)">刷新</el-button>
        <el-button type="success" @click="$router.push('/home/create')">创建工单</el-button>
      </div>
    </el-card>

    <el-card class="ticket-table">
      <ErrorState v-if="errorMsg" :message="errorMsg" @retry="pageTicketPage(ticketData.current)" />

      <template v-else>
        <el-table :data="filteredRecords()" style="width: 100%" empty-text="暂无工单数据">
          <el-table-column fixed prop="ticketNo" label="工单编号" width="140" />
          <el-table-column prop="title" label="工单标题" min-width="180" />
          <el-table-column prop="category" label="分类" width="100">
            <template #default="{ row }">{{ categoryName(row.category) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="130">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status) as any">{{ statusTagName(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="90">
            <template #default="{ row }">{{ priorityName(row.priority) }}</template>
          </el-table-column>
          <el-table-column prop="username" label="用户" width="100" />
          <el-table-column prop="agentName" label="客服" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column fixed="right" label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="toTicketDetail(row.id)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pager"
          layout="total, prev, pager, next"
          :current-page="ticketData.current"
          :page-size="ticketData.size"
          :total="ticketData.total"
          @current-change="pageTicketPage"
        />
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}
.ticket-table {
  margin-top: 16px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>

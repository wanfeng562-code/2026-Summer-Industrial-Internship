<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { TagProps } from 'element-plus'
import { requestTicketPage } from '@/api/ticket'
import type { Page, TicketVo } from '@/api/ticket/type'
import { useUserInfoStore } from '@/stores/userInfo'
import ErrorState from '@/components/ErrorState.vue'

const router = useRouter()
const userStore = useUserInfoStore()
const loading = ref(false)
const errorMsg = ref('')
const keyword = ref('')
const statusFilter = ref('')
const ticketData = reactive<Page<TicketVo>>({
  records: [],
  total: 0,
  size: 10,
  current: 1,
})

const filteredRecords = computed(() => {
  let records = ticketData.records
  if (statusFilter.value) {
    records = records.filter((ticket) => ticket.status === statusFilter.value)
  }
  const value = keyword.value.trim().toLowerCase()
  if (value) {
    records = records.filter((ticket) =>
      ticket.ticketNo.toLowerCase().includes(value)
      || ticket.title.toLowerCase().includes(value)
      || ticket.username?.toLowerCase().includes(value))
  }
  return records
})

const loadTickets = async (current = 1) => {
  loading.value = true
  errorMsg.value = ''
  try {
    const response = await requestTicketPage(current, ticketData.size)
    if (response.code !== 200) {
      errorMsg.value = response.msg || '加载工单失败'
      ElMessage.error(errorMsg.value)
      return
    }
    Object.assign(ticketData, response.data)
  } catch {
    errorMsg.value = '网络异常，工单列表加载失败'
  } finally {
    loading.value = false
  }
}

const statusTagType = (status: string): TagProps['type'] => {
  const map: Record<string, TagProps['type']> = {
    AI_PROCESSING: 'warning',
    MANUAL_REVIEW: 'primary',
    RESOLVED: 'success',
    CLOSED: 'info',
  }
  return map[status] || 'info'
}

const statusName = (status: string) => {
  const map: Record<string, string> = {
    AI_PROCESSING: 'AI处理中',
    MANUAL_REVIEW: '人工复核',
    RESOLVED: '已解决',
    CLOSED: '已关闭',
  }
  return map[status] || status
}

const categoryName = (category: string) => {
  const map: Record<string, string> = {
    REFUND: '退款退货',
    LOGISTICS: '物流异常',
    DAMAGE: '商品破损',
    INVOICE: '发票问题',
    OTHER: '其他',
  }
  return map[category] || category
}

const priorityName = (priority: string) => {
  const map: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急',
  }
  return map[priority] || priority
}

onMounted(() => loadTickets())
</script>

<template>
  <div v-loading="loading">
    <el-card>
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="搜索编号/标题/用户" class="search" />
        <el-select v-model="statusFilter" clearable placeholder="状态筛选" class="status-filter">
          <el-option label="AI处理中" value="AI_PROCESSING" />
          <el-option label="人工复核" value="MANUAL_REVIEW" />
          <el-option label="已解决" value="RESOLVED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button type="primary" @click="loadTickets(ticketData.current)">刷新</el-button>
        <el-button v-if="userStore.isUser" type="success" @click="router.push('/home/create')">
          创建工单
        </el-button>
      </div>
    </el-card>

    <el-card class="ticket-table">
      <ErrorState v-if="errorMsg" :message="errorMsg" @retry="loadTickets(ticketData.current)" />
      <template v-else>
        <el-table :data="filteredRecords" empty-text="暂无工单数据">
          <el-table-column fixed prop="ticketNo" label="工单编号" width="140" />
          <el-table-column prop="title" label="工单标题" min-width="180" />
          <el-table-column label="分类" width="100">
            <template #default="{ row }">{{ categoryName(row.category) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="130">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ statusName(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="优先级" width="90">
            <template #default="{ row }">{{ priorityName(row.priority) }}</template>
          </el-table-column>
          <el-table-column prop="username" label="用户" width="110" />
          <el-table-column prop="agentName" label="客服" width="120">
            <template #default="{ row }">{{ row.agentName || '未分配' }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column fixed="right" label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="router.push(`/home/tickets/${row.id}`)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pager"
          layout="total, prev, pager, next"
          :current-page="ticketData.current"
          :page-size="ticketData.size"
          :total="ticketData.total"
          @current-change="loadTickets"
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

.search {
  width: 240px;
}

.status-filter {
  width: 160px;
}

.ticket-table {
  margin-top: 16px;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>

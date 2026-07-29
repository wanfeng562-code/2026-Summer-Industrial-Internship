<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { downloadServiceReport, requestDashboardStats, requestServiceReport, type DashboardStats, type ServiceReport } from '@/api/stats'
import { requestAgentGroups } from '@/api/admin'
import { useUserInfoStore } from '@/stores/userInfo'

const userStore = useUserInfoStore()
const loading = ref(false)
const errorMsg = ref('')
const stats = ref<DashboardStats>({
  total: 0,
  aiProcessing: 0,
  manualReview: 0,
  resolved: 0,
  rejected: 0,
  closed: 0,
  slaWarning: 0,
  slaEscalated: 0,
  categoryCounts: {},
})
const serviceReport = ref<ServiceReport>()
const reportMonth = ref(new Date().toISOString().slice(0, 7))
const canViewService = ref(false)

const categoryLabels: Record<string, string> = {
  REFUND: '退款退货',
  LOGISTICS: '物流异常',
  DAMAGE: '商品破损',
  INVOICE: '发票问题',
  OTHER: '其他问题',
}

const categories = computed(() =>
  Object.entries(stats.value.categoryCounts).map(([key, count]) => ({
    key,
    name: categoryLabels[key] || key,
    count,
    percentage: stats.value.total === 0 ? 0 : Math.round((count / stats.value.total) * 100),
  })),
)

const loadStats = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const response = await requestDashboardStats()
    if (response.code !== 200) {
      errorMsg.value = response.msg || '统计数据加载失败'
      ElMessage.error(errorMsg.value)
      return
    }
    stats.value = response.data
  } catch {
    errorMsg.value = '网络异常，统计数据加载失败'
  } finally {
    loading.value = false
  }
}

const loadService = async () => {
  canViewService.value = userStore.isAdmin
  if (!canViewService.value && userStore.isAgent) {
    const response = await requestAgentGroups(false)
    canViewService.value = response.data.some((group) => group.leaderId === userStore.getUserId)
  }
  if (!canViewService.value) return
  const [year, month] = reportMonth.value.split('-').map(Number)
  serviceReport.value = (await requestServiceReport(year, month)).data
}

const exportService = async () => {
  const [year, month] = reportMonth.value.split('-').map(Number)
  const blob = await downloadServiceReport(year, month)
  const url = URL.createObjectURL(blob); const link = document.createElement('a')
  link.href = url; link.download = `service-report-${reportMonth.value}.csv`; link.click(); URL.revokeObjectURL(url)
}

onMounted(() => { void Promise.all([loadStats(), loadService()]) })
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <el-alert
      v-if="errorMsg"
      class="notice"
      type="error"
      :closable="false"
      :title="errorMsg"
      show-icon
    >
      <template #default>
        <el-button link type="primary" @click="loadStats">重新加载</el-button>
      </template>
    </el-alert>

    <el-alert
      v-else-if="stats.slaWarning || stats.slaEscalated"
      class="notice"
      type="warning"
      :closable="false"
      :title="`当前范围内有 ${stats.slaWarning} 个 SLA 临期工单、${stats.slaEscalated} 个已升级工单`"
      show-icon
    />

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover">
          <div class="stat-content">
            <div><div class="stat-title">工单总数</div><div class="stat-value">{{ stats.total }}</div></div>
            <el-icon size="48" color="#409eff"><Document /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover">
          <div class="stat-content">
            <div><div class="stat-title">待人工处理</div><div class="stat-value warning">{{ stats.manualReview }}</div></div>
            <el-icon size="48" color="#e6a23c"><Clock /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover">
          <div class="stat-content">
            <div><div class="stat-title">AI 处理中</div><div class="stat-value primary">{{ stats.aiProcessing }}</div></div>
            <el-icon size="48" color="#409eff"><Loading /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover">
          <div class="stat-content">
            <div>
              <div class="stat-title">已解决 / 已关闭</div>
              <div class="stat-value success">{{ stats.resolved }} / {{ stats.closed }}</div>
            </div>
            <el-icon size="48" color="#67c23a"><CircleCheckFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <el-card class="panel">
          <template #header>
            <div class="card-header">
              <span>工单分类统计</span>
              <el-button link type="primary" @click="loadStats">刷新</el-button>
            </div>
          </template>
          <el-empty v-if="stats.total === 0" description="当前范围内暂无工单" />
          <div v-else class="categories">
            <div v-for="item in categories" :key="item.key" class="category-item">
              <div class="category-label">
                <span>{{ item.name }}</span>
                <span>{{ item.count }}（{{ item.percentage }}%）</span>
              </div>
              <el-progress :percentage="item.percentage" :show-text="false" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="panel">
          <template #header>快捷操作</template>
          <div class="quick-actions">
            <el-button
              v-if="userStore.isUser"
              type="primary"
              size="large"
              @click="$router.push('/home/create')"
            >
              <el-icon><Plus /></el-icon>创建工单
            </el-button>
            <el-button type="success" size="large" @click="$router.push('/home/chat')">
              <el-icon><ChatDotRound /></el-icon>AI 客服
            </el-button>
            <el-button type="warning" size="large" @click="$router.push('/home/tickets')">
              <el-icon><List /></el-icon>工单列表
            </el-button>
            <el-button
              v-if="userStore.isUser || userStore.isAdmin"
              size="large"
              @click="$router.push('/home/orders')"
            >
              <el-icon><ShoppingCart /></el-icon>订单管理
            </el-button>
          </div>
          <el-divider />
          <div class="system-info">
            <h4>系统能力</h4>
            <p>AI 客服通过受控只读工具查询业务摘要。</p>
            <p>支持退款退货、物流异常、商品破损和发票问题。</p>
            <p>SLA 临期预警，超时后自动升级优先级。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="canViewService && serviceReport" class="service-panel">
      <template #header><div class="card-header"><span>服务运营与满意度</span><div><el-date-picker v-model="reportMonth" type="month" value-format="YYYY-MM" @change="loadService" /><el-button class="export" @click="exportService">导出月报</el-button></div></div></template>
      <el-row :gutter="16">
        <el-col :span="4"><el-statistic title="接待量" :value="serviceReport.receptionCount" /></el-col>
        <el-col :span="4"><el-statistic title="AI 自动回复率" :value="serviceReport.aiReplyRate" suffix="%" /></el-col>
        <el-col :span="4"><el-statistic title="转人工率" :value="serviceReport.transferToHumanRate" suffix="%" /></el-col>
        <el-col :span="4"><el-statistic title="完结率" :value="serviceReport.completionRate" suffix="%" /></el-col>
        <el-col :span="4"><el-statistic title="平均满意度" :value="serviceReport.averageSatisfaction" suffix=" / 5" /></el-col>
        <el-col :span="4"><el-statistic title="评价数" :value="serviceReport.satisfactionCount" /></el-col>
      </el-row>
    </el-card>
  </div>
</template>

<style scoped>
.notice,
.stat-row {
  margin-bottom: 20px;
}

.stat-row .el-col {
  margin-bottom: 12px;
}

.stat-content,
.card-header,
.category-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-title {
  margin-bottom: 8px;
  color: #909399;
  font-size: 14px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.primary {
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.panel {
  min-height: 310px;
}
.service-panel{margin-top:20px}.export{margin-left:10px}

.categories,
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-label {
  margin-bottom: 6px;
  color: #606266;
  font-size: 14px;
}

.quick-actions {
  flex-flow: row wrap;
  gap: 12px;
}

.system-info {
  color: #606266;
  font-size: 13px;
}

.system-info p {
  margin: 4px 0;
}

@media (max-width: 1199px) {
  .panel {
    min-height: auto;
    margin-bottom: 20px;
  }
}
</style>

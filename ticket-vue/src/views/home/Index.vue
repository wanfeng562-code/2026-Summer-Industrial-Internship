<script setup lang="ts">
import { useUserInfoStore } from '@/stores/userInfo'

const userStore = useUserInfoStore()
</script>

<template>
  <div class="dashboard">
    <el-alert
      class="notice"
      type="warning"
      :closable="false"
      title="工作台统计暂未接入"
      description="统计口径与接口由成员 A/C 提供后再接入，当前占位符不可作为验收数据。"
      show-icon
    />

    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-content">
            <div><div class="stat-title">工单总数</div><div class="stat-value">--</div></div>
            <el-icon size="48" color="#409eff"><Document /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-content">
            <div><div class="stat-title">待处理</div><div class="stat-value warning">--</div></div>
            <el-icon size="48" color="#e6a23c"><Clock /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-content">
            <div><div class="stat-title">处理中</div><div class="stat-value primary">--</div></div>
            <el-icon size="48" color="#409eff"><Loading /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-content">
            <div><div class="stat-title">已解决</div><div class="stat-value success">--</div></div>
            <el-icon size="48" color="#67c23a"><CircleCheckFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>工单分类统计</template>
          <el-empty description="统计接口暂未接入" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
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
              <el-icon><ChatDotRound /></el-icon>AI客服
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
            <h4>系统信息</h4>
            <p>AI 客服通过受控只读工具查询业务摘要。</p>
            <p>支持退款退货、物流异常、商品破损和发票问题。</p>
            <p>SLA 临期预警，超时后自动升级优先级。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.notice,
.stat-row {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-title {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
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

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.system-info {
  color: #666;
  font-size: 13px;
}

.system-info p {
  margin: 4px 0;
}
</style>

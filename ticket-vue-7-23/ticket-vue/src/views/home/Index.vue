<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">工单总数</div>
              <div class="stat-value">{{ 0 }}</div>
            </div>
            <el-icon size="48" color="#409eff"><Document /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">待处理</div>
              <div class="stat-value warning">{{ 0 }}</div>
            </div>
            <el-icon size="48" color="#e6a23c"><Clock /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">处理中</div>
              <div class="stat-value primary">{{ 0 }}</div>
            </div>
            <el-icon size="48" color="#409eff"><Loading /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">已解决</div>
              <div class="stat-value success">{{ 0 }}</div>
            </div>
            <el-icon size="48" color="#67c23a"><CircleCheckFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 工单分类统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>工单分类统计</span>
          </template>
          <div class="category-stats">
            <div  class="category-item">
              <span class="category-name">{{ }}</span>
              <el-progress  />
            </div>
            <el-empty description="暂无数据" />
          </div>
        </el-card>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="$router.push('/home/create')">
              <el-icon><Plus /></el-icon>创建工单
            </el-button>
            <el-button type="success" size="large" @click="$router.push('/home/chat')">
              <el-icon><ChatDotRound /></el-icon>AI客服
            </el-button>
            <el-button type="warning" size="large" @click="$router.push('/home/tickets')">
              <el-icon><List /></el-icon>工单列表
            </el-button>
            <el-button size="large" @click="$router.push('/main/orders')">
              <el-icon><ShoppingCart /></el-icon>订单管理
            </el-button>
          </div>
          <el-divider />
          <div class="system-info">
            <h4>系统信息</h4>
            <p>AI客服基于阿里百炼千问大模型</p>
            <p>支持退款退货、物流异常、商品破损、发票问题等场景</p>
            <p>SLA预警：超时自动升级处理</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'


const categoryNameMap: Record<string, string> = {
  REFUND: '退款退货',
  LOGISTICS: '物流异常',
  DAMAGE: '商品破损',
  INVOICE: '发票问题',
  OTHER: '其他'
}

onMounted(async () => {
  
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}
.stat-row {
  margin-bottom: 20px;
}
.stat-card {
  border-radius: 8px;
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
  color: #333;
}
.stat-value.warning { color: #e6a23c; }
.stat-value.primary { color: #409eff; }
.stat-value.success { color: #67c23a; }
.category-stats {
  padding: 10px 0;
}
.category-item {
  margin-bottom: 16px;
}
.category-name {
  display: block;
  margin-bottom: 4px;
  font-size: 14px;
  color: #666;
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
.system-info h4 {
  margin-bottom: 8px;
  color: #333;
}
.system-info p {
  margin: 4px 0;
}
</style>

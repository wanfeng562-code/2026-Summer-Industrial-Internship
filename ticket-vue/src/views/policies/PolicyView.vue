<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  requestCreateFaq,
  requestCreatePolicy,
  requestDeleteFaq,
  requestDeletePolicy,
  requestFaqPage,
  requestPolicyEnabled,
  requestPolicyPage,
  requestUpdateFaq,
  requestUpdatePolicy,
} from '@/api/policy'
import type {
  AfterSalePolicy,
  Faq,
  FaqRequest,
  PolicyRequest,
  TicketCategory,
} from '@/api/policy'

const categories: Array<{ label: string; value: TicketCategory }> = [
  { label: '退款退货', value: 'REFUND' },
  { label: '物流异常', value: 'LOGISTICS' },
  { label: '商品破损', value: 'DAMAGE' },
  { label: '发票问题', value: 'INVOICE' },
  { label: '其他', value: 'OTHER' },
]
const categoryName = (value: TicketCategory) =>
  categories.find((item) => item.value === value)?.label || value

const loading = ref(false)
const policies = ref<AfterSalePolicy[]>([])
const policyCurrent = ref(1)
const policyTotal = ref(0)
const policyDialog = ref(false)
const editingPolicyId = ref<number | null>(null)
const emptyPolicy = (): PolicyRequest => ({
  policyName: '',
  category: 'OTHER',
  conditionType: 'ALWAYS',
  minAmount: null,
  maxAmount: null,
  minReputation: null,
  action: 'AUTO_REPLY',
  replyTemplate: '',
  priority: 1,
  enabled: 1,
  slaHours: 48,
})
const policyForm = reactive<PolicyRequest>(emptyPolicy())

const loadPolicies = async () => {
  loading.value = true
  try {
    const response = await requestPolicyPage(policyCurrent.value)
    policies.value = response.data.records
    policyTotal.value = response.data.total
  } finally {
    loading.value = false
  }
}

const openPolicy = (policy?: AfterSalePolicy) => {
  editingPolicyId.value = policy?.id ?? null
  Object.assign(policyForm, policy ? {
    policyName: policy.policyName,
    category: policy.category,
    conditionType: policy.conditionType,
    minAmount: policy.minAmount,
    maxAmount: policy.maxAmount,
    minReputation: policy.minReputation,
    action: policy.action,
    replyTemplate: policy.replyTemplate ?? '',
    priority: policy.priority,
    enabled: policy.enabled,
    slaHours: policy.slaHours,
  } : emptyPolicy())
  policyDialog.value = true
}

const savePolicy = async () => {
  if (!policyForm.policyName.trim()) {
    ElMessage.warning('请输入策略名称')
    return
  }
  if (editingPolicyId.value) await requestUpdatePolicy(editingPolicyId.value, policyForm)
  else await requestCreatePolicy(policyForm)
  policyDialog.value = false
  ElMessage.success('策略保存成功')
  await loadPolicies()
}

const changePolicyEnabled = async (policy: AfterSalePolicy) => {
  try {
    await requestPolicyEnabled(policy.id, policy.enabled)
    ElMessage.success('启用状态已更新')
  } catch {
    policy.enabled = policy.enabled ? 0 : 1
  }
}

const deletePolicy = async (policy: AfterSalePolicy) => {
  await ElMessageBox.confirm(`确定删除策略“${policy.policyName}”？`, '确认删除', { type: 'warning' })
  await requestDeletePolicy(policy.id)
  ElMessage.success('策略已删除')
  await loadPolicies()
}

const faqs = ref<Faq[]>([])
const faqCurrent = ref(1)
const faqTotal = ref(0)
const faqKeyword = ref('')
const faqDialog = ref(false)
const editingFaqId = ref<number | null>(null)
const emptyFaq = (): FaqRequest => ({
  category: 'OTHER',
  question: '',
  answer: '',
  keywords: '',
  enabled: 1,
})
const faqForm = reactive<FaqRequest>(emptyFaq())

const loadFaqs = async () => {
  loading.value = true
  try {
    const response = await requestFaqPage(faqCurrent.value, 10, faqKeyword.value)
    faqs.value = response.data.records
    faqTotal.value = response.data.total
  } finally {
    loading.value = false
  }
}

const openFaq = (faq?: Faq) => {
  editingFaqId.value = faq?.id ?? null
  Object.assign(faqForm, faq ? {
    category: faq.category,
    question: faq.question,
    answer: faq.answer,
    keywords: faq.keywords ?? '',
    enabled: faq.enabled,
  } : emptyFaq())
  faqDialog.value = true
}

const saveFaq = async () => {
  if (!faqForm.question.trim() || !faqForm.answer.trim()) {
    ElMessage.warning('问题和答案不能为空')
    return
  }
  if (editingFaqId.value) await requestUpdateFaq(editingFaqId.value, faqForm)
  else await requestCreateFaq(faqForm)
  faqDialog.value = false
  ElMessage.success('FAQ保存成功')
  await loadFaqs()
}

const deleteFaq = async (faq: Faq) => {
  await ElMessageBox.confirm(`确定删除FAQ“${faq.question}”？`, '确认删除', { type: 'warning' })
  await requestDeleteFaq(faq.id)
  ElMessage.success('FAQ已删除')
  await loadFaqs()
}

onMounted(() => {
  void Promise.all([loadPolicies(), loadFaqs()])
})
</script>

<template>
  <el-card v-loading="loading" shadow="never">
    <template #header>
      <div class="header">
        <div>
          <h2>售后策略与知识库</h2>
          <p>策略决定确定性业务规则；FAQ 为 AI 和人工客服提供只读标准答案。</p>
        </div>
      </div>
    </template>

    <el-tabs>
      <el-tab-pane label="售后策略">
        <div class="toolbar">
          <el-button type="primary" @click="openPolicy()">新增策略</el-button>
        </div>
        <el-table :data="policies" empty-text="暂无售后策略">
          <el-table-column prop="policyName" label="策略名称" min-width="170" />
          <el-table-column label="分类" width="110">
            <template #default="{ row }">{{ categoryName(row.category) }}</template>
          </el-table-column>
          <el-table-column prop="conditionType" label="条件" width="170" />
          <el-table-column prop="action" label="动作" width="140" />
          <el-table-column prop="slaHours" label="SLA(小时)" width="110" />
          <el-table-column prop="priority" label="顺序" width="80" />
          <el-table-column label="启用" width="90">
            <template #default="{ row }">
              <el-switch
                v-model="row.enabled"
                :active-value="1"
                :inactive-value="0"
                @change="changePolicyEnabled(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openPolicy(row)">编辑</el-button>
              <el-button link type="danger" @click="deletePolicy(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="policyCurrent"
          layout="prev, pager, next, total"
          :total="policyTotal"
          class="pagination"
          @current-change="loadPolicies"
        />
      </el-tab-pane>

      <el-tab-pane label="FAQ知识库">
        <div class="toolbar">
          <el-input
            v-model="faqKeyword"
            clearable
            placeholder="搜索问题、答案或关键词"
            class="search"
            @keyup.enter="loadFaqs"
            @clear="loadFaqs"
          />
          <el-button @click="loadFaqs">检索</el-button>
          <el-button type="primary" @click="openFaq()">新增FAQ</el-button>
        </div>
        <el-table :data="faqs" empty-text="暂无FAQ">
          <el-table-column label="分类" width="110">
            <template #default="{ row }">{{ categoryName(row.category) }}</template>
          </el-table-column>
          <el-table-column prop="question" label="问题" min-width="220" />
          <el-table-column prop="answer" label="答案" min-width="300" show-overflow-tooltip />
          <el-table-column prop="keywords" label="关键词" min-width="160" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'">
                {{ row.enabled ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openFaq(row)">编辑</el-button>
              <el-button link type="danger" @click="deleteFaq(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="faqCurrent"
          layout="prev, pager, next, total"
          :total="faqTotal"
          class="pagination"
          @current-change="loadFaqs"
        />
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <el-dialog v-model="policyDialog" :title="editingPolicyId ? '编辑策略' : '新增策略'" width="680px">
    <el-form :model="policyForm" label-width="110px">
      <el-form-item label="策略名称" required><el-input v-model="policyForm.policyName" maxlength="100" /></el-form-item>
      <el-form-item label="分类" required>
        <el-select v-model="policyForm.category">
          <el-option v-for="item in categories" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="条件类型" required>
        <el-select v-model="policyForm.conditionType">
          <el-option label="始终匹配" value="ALWAYS" />
          <el-option label="金额" value="AMOUNT" />
          <el-option label="信誉" value="REPUTATION" />
          <el-option label="金额与信誉" value="AMOUNT_REPUTATION" />
        </el-select>
      </el-form-item>
      <el-form-item label="金额区间">
        <el-input-number v-model="policyForm.minAmount" :min="0" placeholder="最低" />
        <span class="separator">至</span>
        <el-input-number v-model="policyForm.maxAmount" :min="0" placeholder="最高" />
      </el-form-item>
      <el-form-item label="最低信誉"><el-input-number v-model="policyForm.minReputation" :min="0" :max="100" /></el-form-item>
      <el-form-item label="处理动作" required>
        <el-select v-model="policyForm.action">
          <el-option label="自动通过建议" value="AUTO_APPROVE" />
          <el-option label="自动回复" value="AUTO_REPLY" />
          <el-option label="转人工" value="MANUAL" />
        </el-select>
      </el-form-item>
      <el-form-item label="SLA小时"><el-input-number v-model="policyForm.slaHours" :min="1" :max="720" /></el-form-item>
      <el-form-item label="匹配顺序"><el-input-number v-model="policyForm.priority" :min="0" /></el-form-item>
      <el-form-item label="回复模板"><el-input v-model="policyForm.replyTemplate" type="textarea" :rows="4" maxlength="5000" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="policyForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="policyDialog = false">取消</el-button>
      <el-button type="primary" @click="savePolicy">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="faqDialog" :title="editingFaqId ? '编辑FAQ' : '新增FAQ'" width="680px">
    <el-form :model="faqForm" label-width="90px">
      <el-form-item label="分类" required>
        <el-select v-model="faqForm.category">
          <el-option v-for="item in categories" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="问题" required><el-input v-model="faqForm.question" maxlength="500" /></el-form-item>
      <el-form-item label="答案" required><el-input v-model="faqForm.answer" type="textarea" :rows="5" maxlength="5000" /></el-form-item>
      <el-form-item label="关键词"><el-input v-model="faqForm.keywords" maxlength="500" placeholder="逗号分隔" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="faqForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="faqDialog = false">取消</el-button>
      <el-button type="primary" @click="saveFaq">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.header h2 {
  margin: 0 0 6px;
}

.header p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 16px;
}

.search {
  width: 300px;
}

.pagination {
  justify-content: flex-end;
  margin-top: 16px;
}

.separator {
  margin: 0 10px;
  color: #909399;
}
</style>

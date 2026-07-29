<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  requestAgentGroups, requestCreateAgentGroup, requestDeleteAgentGroup,
  requestTicketCategories, requestCreateTicketCategory, requestDeleteTicketCategory,
  requestUpdateAgentGroup, requestUpdateTicketCategory,
  type AgentGroup, type AgentGroupRequest, type TicketCategoryConfig,
} from '@/api/admin'
import { requestUserPage } from '@/api/user'
import type { UserProfile } from '@/api/user/type'

const loading = ref(false)
const groups = ref<AgentGroup[]>([])
const categories = ref<TicketCategoryConfig[]>([])
const agents = ref<UserProfile[]>([])
const groupDialog = ref(false)
const categoryDialog = ref(false)
const editingGroupId = ref<number>()
const editingCategoryId = ref<number>()
const groupForm = reactive<AgentGroupRequest>({ groupName: '', leaderId: null, description: '', enabled: 1, agentIds: [] })
const categoryForm = reactive<Omit<TicketCategoryConfig, 'id'>>({ categoryCode: '', categoryName: '', groupId: null, enabled: 1 })
const agentAvailableForGroup = (agent: UserProfile) =>
  agent.agentGroupId === null || agent.agentGroupId === editingGroupId.value

const load = async () => {
  loading.value = true
  try {
    const [groupRes, categoryRes, agentRes] = await Promise.all([
      requestAgentGroups(true), requestTicketCategories(true), requestUserPage(1, 100, 'AGENT'),
    ])
    groups.value = groupRes.data
    categories.value = categoryRes.data
    agents.value = agentRes.data.records
  } finally { loading.value = false }
}

const openGroup = (row?: AgentGroup) => {
  editingGroupId.value = row?.id
  Object.assign(groupForm, {
    groupName: row?.groupName || '', leaderId: row?.leaderId ?? null,
    description: row?.description || '', enabled: row?.enabled ?? 1,
    agentIds: row ? agents.value.filter((agent) => agent.agentGroupId === row.id).map((agent) => agent.id) : [],
  })
  groupDialog.value = true
}

const saveGroup = async () => {
  if (!groupForm.groupName.trim()) return ElMessage.warning('请输入坐席组名称')
  if (editingGroupId.value) await requestUpdateAgentGroup(editingGroupId.value, groupForm)
  else await requestCreateAgentGroup(groupForm)
  ElMessage.success('坐席组已保存')
  groupDialog.value = false
  await load()
}

const openCategory = (row?: TicketCategoryConfig) => {
  editingCategoryId.value = row?.id
  Object.assign(categoryForm, {
    categoryCode: row?.categoryCode || '', categoryName: row?.categoryName || '',
    groupId: row?.groupId ?? null, enabled: row?.enabled ?? 1,
  })
  categoryDialog.value = true
}

const saveCategory = async () => {
  if (!categoryForm.categoryCode.trim() || !categoryForm.categoryName.trim()) return ElMessage.warning('请填写分类编码和名称')
  if (categoryForm.enabled === 1 && categoryForm.groupId !== null
    && groups.value.find((group) => group.id === categoryForm.groupId)?.enabled !== 1) {
    return ElMessage.warning('启用分类不能绑定已停用的坐席组')
  }
  categoryForm.categoryCode = categoryForm.categoryCode.trim().toUpperCase()
  if (editingCategoryId.value) await requestUpdateTicketCategory(editingCategoryId.value, categoryForm)
  else await requestCreateTicketCategory(categoryForm)
  ElMessage.success('工单分类已保存')
  categoryDialog.value = false
  await load()
}

const removeGroup = async (row: AgentGroup) => {
  await ElMessageBox.confirm(`确认删除坐席组“${row.groupName}”？`, '确认删除')
  await requestDeleteAgentGroup(row.id); await load()
}
const removeCategory = async (row: TicketCategoryConfig) => {
  await ElMessageBox.confirm(`确认删除分类“${row.categoryName}”？`, '确认删除')
  await requestDeleteTicketCategory(row.id); await load()
}
const groupName = (id: number | null) => groups.value.find((item) => item.id === id)?.groupName || '未分配'
onMounted(load)
</script>

<template>
  <el-card v-loading="loading" shadow="never">
    <template #header><div class="header"><div><h2>系统业务配置</h2><p>维护坐席分组、组长、成员及工单分类的默认负责组。</p></div><el-button @click="load">刷新</el-button></div></template>
    <el-tabs>
      <el-tab-pane label="坐席分组">
        <el-button type="primary" class="add" @click="openGroup()">新增坐席组</el-button>
        <el-table :data="groups">
          <el-table-column prop="groupName" label="组名" />
          <el-table-column label="组长"><template #default="{ row }">{{ agents.find((a) => a.id === row.leaderId)?.nickname || '未指定' }}</template></el-table-column>
          <el-table-column prop="description" label="说明" />
          <el-table-column label="状态"><template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="160"><template #default="{ row }"><el-button link type="primary" @click="openGroup(row)">编辑</el-button><el-button link type="danger" @click="removeGroup(row)">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="工单分类">
        <el-button type="primary" class="add" @click="openCategory()">新增分类</el-button>
        <el-table :data="categories">
          <el-table-column prop="categoryCode" label="分类编码" />
          <el-table-column prop="categoryName" label="分类名称" />
          <el-table-column label="默认负责组"><template #default="{ row }">{{ groupName(row.groupId) }}</template></el-table-column>
          <el-table-column label="状态"><template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="160"><template #default="{ row }"><el-button link type="primary" @click="openCategory(row)">编辑</el-button><el-button link type="danger" @click="removeCategory(row)">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <el-dialog v-model="groupDialog" :title="editingGroupId ? '编辑坐席组' : '新增坐席组'" width="560px">
    <el-form label-width="90px">
      <el-form-item label="组名"><el-input v-model="groupForm.groupName" /></el-form-item>
      <el-form-item label="组长"><el-select v-model="groupForm.leaderId" clearable filterable><el-option v-for="agent in agents" :key="agent.id" :label="agent.nickname" :value="agent.id" :disabled="!agentAvailableForGroup(agent)" /></el-select></el-form-item>
      <el-form-item label="成员"><el-select v-model="groupForm.agentIds" multiple filterable><el-option v-for="agent in agents" :key="agent.id" :label="agent.nickname" :value="agent.id" :disabled="!agentAvailableForGroup(agent)" /></el-select></el-form-item>
      <el-form-item label="说明"><el-input v-model="groupForm.description" type="textarea" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="groupForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="groupDialog = false">取消</el-button><el-button type="primary" @click="saveGroup">保存</el-button></template>
  </el-dialog>

  <el-dialog v-model="categoryDialog" :title="editingCategoryId ? '编辑工单分类' : '新增工单分类'" width="520px">
    <el-form label-width="100px">
      <el-form-item label="分类编码"><el-input v-model="categoryForm.categoryCode" :disabled="Boolean(editingCategoryId)" placeholder="如 QUALITY" /></el-form-item>
      <el-form-item label="分类名称"><el-input v-model="categoryForm.categoryName" /></el-form-item>
      <el-form-item label="默认负责组"><el-select v-model="categoryForm.groupId" clearable><el-option v-for="group in groups" :key="group.id" :label="group.enabled ? group.groupName : `${group.groupName}（已停用）`" :value="group.id" :disabled="categoryForm.enabled === 1 && group.enabled !== 1" /></el-select></el-form-item>
      <el-form-item label="启用"><el-switch v-model="categoryForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="categoryDialog = false">取消</el-button><el-button type="primary" @click="saveCategory">保存</el-button></template>
  </el-dialog>
</template>

<style scoped>.header{display:flex;justify-content:space-between;align-items:center}.header h2{margin:0 0 6px}.header p{margin:0;color:#909399}.add{margin-bottom:14px}.el-select{width:100%}</style>

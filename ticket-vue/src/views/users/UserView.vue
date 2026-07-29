<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type TagProps } from 'element-plus'
import { requestKickoutUser, requestResetPassword, requestUserPage } from '@/api/user'
import type { UserProfile } from '@/api/user/type'
import type { Page } from '@/api/ticket/type'
import ErrorState from '@/components/ErrorState.vue'

const loading = ref(false)
const errorMsg = ref('')
const roleFilter = ref('')
const users = reactive<Page<UserProfile>>({
  records: [],
  total: 0,
  current: 1,
  size: 10,
})

const roleName = (role: string) => ({
  USER: '普通用户',
  AGENT: '客服',
  ADMIN: '管理员',
}[role] || role)

const roleTags: Record<string, TagProps['type']> = {
  USER: 'info',
  AGENT: 'warning',
  ADMIN: 'danger',
}

const roleTag = (role: string): TagProps['type'] => roleTags[role] || 'info'

const loadUsers = async (current = 1) => {
  loading.value = true
  errorMsg.value = ''
  try {
    const response = await requestUserPage(current, users.size, roleFilter.value)
    if (response.code !== 200) {
      errorMsg.value = response.msg || '用户列表加载失败'
      ElMessage.error(errorMsg.value)
      return
    }
    Object.assign(users, response.data)
  } catch {
    errorMsg.value = '网络异常，用户列表加载失败'
  } finally {
    loading.value = false
  }
}

const kickout = async (user: UserProfile) => {
  await ElMessageBox.confirm(`确认强制下线“${user.nickname}”？`, '强制下线')
  await requestKickoutUser(user.id)
  ElMessage.success('用户已被强制下线')
  await loadUsers(users.current)
}

const resetPassword = async (user: UserProfile) => {
  const { value } = await ElMessageBox.prompt(`为“${user.nickname}”设置新密码`, '重置密码', {
    inputType: 'password',
    inputValidator: (text) => (text?.length || 0) >= 6 || '密码至少 6 位',
  })
  await requestResetPassword(user.id, value)
  ElMessage.success('密码已重置，原登录会话已失效')
  await loadUsers(users.current)
}

onMounted(() => loadUsers())
</script>

<template>
  <el-card v-loading="loading" shadow="never">
    <template #header>
      <div class="header">
        <div>
          <h2>用户与客服</h2>
          <p>仅管理员可查看账号基础资料，不返回密码等敏感字段。</p>
        </div>
        <div class="filters">
          <el-select v-model="roleFilter" clearable placeholder="全部角色" @change="loadUsers(1)">
            <el-option label="普通用户" value="USER" />
            <el-option label="客服" value="AGENT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <el-button type="primary" @click="loadUsers(users.current)">刷新</el-button>
        </div>
      </div>
    </template>

    <ErrorState v-if="errorMsg" :message="errorMsg" @retry="loadUsers(users.current)" />
    <template v-else>
      <el-table :data="users.records" empty-text="暂无用户数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag :type="roleTag(row.role)">{{ roleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reputationScore" label="信誉分" width="90" />
        <el-table-column label="在线" width="90">
          <template #default="{ row }"><el-tag :type="row.online ? 'success' : 'info'">{{ row.online ? '在线' : '离线' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="170">
          <template #default="{ row }">{{ row.lastLoginTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column fixed="right" label="账号运维" width="170">
          <template #default="{ row }">
            <el-button link type="warning" :disabled="!row.online" @click="kickout(row)">强制下线</el-button>
            <el-button link type="danger" @click="resetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        layout="total, prev, pager, next"
        :current-page="users.current"
        :page-size="users.size"
        :total="users.total"
        @current-change="loadUsers"
      />
    </template>
  </el-card>
</template>

<style scoped>
.header,
.filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

h2 {
  margin: 0 0 6px;
}

p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.filters .el-select {
  width: 150px;
}

.pagination {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

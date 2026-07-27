<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance } from 'element-plus'

const loading = ref(false)
const router = useRouter()

const form = reactive({
  orderId: null as number | null,
  title: '',
  description: '',
  category: ''
})

const rules = {
  orderId: [{ required: true, message: '请选择关联订单', trigger: 'change' }],
  title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }]
}

const categoryOptions = [
  { label: '退款退货', value: 'REFUND' },
  { label: '物流异常', value: 'LOGISTICS' },
  { label: '商品破损', value: 'DAMAGE' },
  { label: '发票问题', value: 'INVOICE' },
  { label: '其他', value: 'OTHER' }
]

onMounted(async () => {

})

const handleSubmit = async () => {

}
</script>

<template>
  <div class="ticket-create">
    <el-card>
      <template #header>
        <span>创建工单</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 700px;">
        <el-form-item label="关联订单" prop="orderId">
          <el-select  placeholder="请选择关联订单" filterable style="width: 100%;">
            <el-option />
          </el-select>
        </el-form-item>
        <el-form-item label="工单标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入工单标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="问题分类" prop="category">
          <el-select v-model="form.category" placeholder="留空将由AI自动识别" clearable style="width: 100%;">
            <el-option  />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="请详细描述您遇到的问题..." maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交工单</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.ticket-create {
  max-width: 800px;
}
</style>
<script setup lang="ts">
import { ref,reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { requestTicketPage } from '@/api/ticket'   // @/   src目录下  
import type {R, Page, TicketVo, TicketMessageVo} from '@/api/ticket/type'

const router = useRouter()

const ticketData = reactive<Page<TicketVo>>({
  records:[],
  total:0,
  size:10,
  current:1,
  pages:0
})

const toTicketDetail = (value : TicketVo)=>{
  console.log("toTicketDetail")
  console.log(value)
  router.push("/home/tickets/" + value)
}

onMounted(() => {
  console.log("通过onMounted初始化工单列表页面")
  pageTicketPage(ticketData.current)
})

//工单分页列表查询
const pageTicketPage = async (current : number)=>{
  try{
    const res = await requestTicketPage(current)
    console.log(res)
    if(res.code == 200){
      ticketData.current = res.data.current
      ticketData.size = res.data.size
      ticketData.total = res.data.total
      ticketData.records = res.data.records
    }else{
      ElMessage.error(res.msg)
    }
  }catch(error){
    // 错误已在拦截器中处理
  }
}

//根据状态获取样式
const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'info', AI_PROCESSING: 'warning', MANUAL_REVIEW: '',
    RESOLVED: 'success', CLOSED: 'danger'
  }
  return map[status] || 'info'
}

//根据状态获取状态中文名称
const statusTagName = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理', AI_PROCESSING: 'AI预处理中', MANUAL_REVIEW: '人工复核',
    RESOLVED: '已解决', CLOSED: '已关闭'
  }
  return map[status] || '待处理'
}

</script>

<template>
<div>
   <!--搜索  按钮-->
  <el-card>
    <el-input  style="width: 240px" placeholder="请输入搜索的关键词" />
    <el-button type="primary">搜索</el-button>
    <el-button type="primary">创建工单</el-button>
  </el-card>

 <!--工单列表-->
  <el-card class="tiketTable">
    <el-table :data="ticketData.records" style="width: 100%" max-height="250">
      <el-table-column fixed prop="ticketNo" label="工单编号" width="120" />
      <el-table-column prop="title" label="工单标题" width="200" />
      <el-table-column prop="category" label="分类" width="80" />
      <el-table-column prop="status" label="状态" width="150">
        <template #default="{row}">
          <el-tag :type="statusTagType(row.status)" >{{statusTagName(row.status)}}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="agentName" label="客服" width="150" />
      <el-table-column prop="createTime" label="创建时间" width="200" />

      <el-table-column fixed="right" label="操作" min-width="120">
        <template #default="{row}">
          <el-button
            link
            type="primary"
            size="small"
            @click="toTicketDetail(row.id)"
          >
            查看工单详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

     <el-pagination
        @current-change="pageTicketPage"
        @prev-click="pageTicketPage"
        @next-click="pageTicketPage"
        :current-page="ticketData.current"
        :page-size="ticketData.size"
        :total="ticketData.total"
        layout="total, prev, pager, next"
        >
      </el-pagination> 
  </el-card>
</div>
</template>

<style scoped>
.tiketTable{
  padding-top: 20px;
}
</style>
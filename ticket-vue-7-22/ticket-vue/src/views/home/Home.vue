<script setup lang="ts">
  import Aside from "../../components/Aside.vue"  //../ 上一级目录   ./ 当前目录
  import { RouterView, RouterLink } from 'vue-router';
</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px"><Aside></Aside></el-aside>
      <el-container>
        <el-header>Header</el-header>
        <el-main><RouterView></RouterView></el-main>
      </el-container>
    </el-container>
  </div>
</template>


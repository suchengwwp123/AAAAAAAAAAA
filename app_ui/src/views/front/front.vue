<script setup>
//示例文档，可以自行删除
</script>

<template>
  <el-row>
    <el-col :span="4" class="logo">
      <el-avatar src="/public/logo.png" size="default"></el-avatar>
      <p>xx系统前台</p>
    </el-col>
    <el-col :span="8">
      <el-menu
          class="el-menu-popper-demo"
          mode="horizontal"
          router
          :popper-offset="16"
          style="max-width: 600px;border: none"
          background-color="#409EFF"
          text-color="#fff"
          active-text-color="#ffd04b"
      >
        <el-menu-item index="/front/index">系统首页</el-menu-item>
        <el-menu-item index="4">Info</el-menu-item>
        <el-menu-item index="5">Orders</el-menu-item>
      </el-menu>
    </el-col>
    <el-col :span="8"></el-col>
  </el-row>
  <div class="container">
    <router-view></router-view>
  </div>
</template>

<style scoped lang="scss">
.el-row {
  background-color: #409EFF;
  --el-menu-active-color: #fff;

  .logo {
    padding: 5px;
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;

    p {
      margin-left: 10px;
    }
  }
}

.container {
  width: 1200px;
  min-height: calc(100vh - 50px);
  overflow: auto;
  margin: 0 auto
}
</style>
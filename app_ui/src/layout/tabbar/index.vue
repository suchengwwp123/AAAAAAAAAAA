<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import {DataLine, Expand, Fold, FullScreen, InfoFilled, Loading, Setting} from '@element-plus/icons-vue'
import useSystemStore from '@/stores/system'
import router from '@/router'
import pinia from '@/stores/store'
import request from '@/utils/request'
import {ElMessage, ElMessageBox} from "element-plus";
import {useDark, useToggle} from "@vueuse/core";
import TabbarSettings from './settings/index.vue'

const isFullScreen = ref(false)
const systemStore = useSystemStore(pinia)
// const backgroudColor = localStorage.getItem('bgc')
//   ? ref(localStorage.getItem('bgc'))
//   : ref('#2f4056')
const $route = router
const drawer = ref(false)

// 修改图标
const changeIcon = () => {
  systemStore.fold = !systemStore.fold
}
//修改全屏
const changeFullScreen = () => {
  isFullScreen.value = !isFullScreen.value
  if (isFullScreen.value) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}
//刷新页面
const refsh = () => {
  window.location.reload()
}
// 退出登录
const handleLogOut = async () => {

  ElMessageBox.confirm(
      '确认退出登录?',
      '系统提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(async () => {
        await request.post('/auth/logout')
        systemStore.reset()
        await $route.replace('/login')

        ElMessage({
          showClose: true,
          message: '退出登录成功！',
          type: 'success'
        })

      })
      .catch(() => {

      })

}
// 修改个人信息
const handleInformation = async () => {
  await $route.replace('/information')
}
//展示抽屉
const handleDrawer = () => {
  drawer.value = true
}

// 暗黑模式
const notics = ref([])
const title = ref('')
let currentIndex = 0  // 用于跟踪当前显示的索引


const load = async () => {
  const res = await request.get('/notice')
  notics.value = res.data
}
load()
const handleChangeDrawer=async (value)=>{
  drawer.value=value
}

</script>
<script>
export default {
  name: 'Tabbar'
}
</script>
<template>
<!--  tabbbar组件-->
  <el-row
      class="layout-tabbar"

  >
    <el-col :xs="0" :sm="10" :md="10" :lg="10" :xl="10">
      <div
          class="layout-tabbar__fold"
      >
        <el-icon size="20"
                 class="layout-tabbar__fold-icon"
        >
          <component :is="systemStore.fold ? Expand : Fold" @click="changeIcon"></component>
        </el-icon>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item
              v-for="(item, index) in $route.currentRoute.value.matched"
              v-show="item.name"
              :key="index.toString()"
              :to="item.path"
          >
            <el-icon>
              <component :is="item.meta.icon"></component>
            </el-icon>
            {{ item.meta.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>

      </div>

    </el-col>

    <el-col :xs="24" :sm="14" :md="14" :lg="14" :xl="14">


      <div
          class="layout-tabbar__settings"
      >
        <el-button type="warning" @click="handleDrawer" :icon="Setting" circle/>
        <el-button type="primary" @click="refsh" :icon="Loading" circle/>
        <el-button type="success" @click="changeFullScreen" :icon="FullScreen" circle/>
        <el-avatar :src="systemStore.userInfo.avatar" :size="32"
                   class="layout-tabbar__settings-avatar"
        ></el-avatar>

        <el-dropdown>
        <span class="el-dropdown-link">
          {{ systemStore.userInfo.username }}
          <el-icon class="el-icon--right">
            <arrow-down/>
          </el-icon>
        </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleInformation">个人信息</el-dropdown-item>
            </el-dropdown-menu>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogOut">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <div class="layout-tabbar__settings-end"></div>
      </div>
    </el-col>
  </el-row>
<!--settings组件-->
  <TabbarSettings
  :drawer="drawer"
  @changeDrawer="handleChangeDrawer"
  ></TabbarSettings>
</template>

<style scoped lang="scss">
.layout-tabbar {
  height: 100%;
  box-shadow: 0px 1px 1px rgba(0, 0, 0, 0.1); /* 阴影效果 */
  .layout-tabbar__fold {
    height: 100%;
    display: flex;
    align-items: center;
    .layout-tabbar__fold-icon {
      margin-left: 10px;
    }
  }
  .layout-tabbar__settings {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: end;
    gap: 10px;
    .el-button{
      margin: 0;
    }
    .el-dropdown-link {
      cursor: pointer;

    }
    .layout-tabbar__settings-end{
      width: 40px;
    }
  }
}
</style>

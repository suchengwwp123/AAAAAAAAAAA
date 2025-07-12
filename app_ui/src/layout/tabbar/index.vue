<script setup>
import {onMounted, onUnmounted, ref, watch} from 'vue'
import {ChatDotRound, DataLine, Expand, Fold, FullScreen, InfoFilled, Loading, Setting} from '@element-plus/icons-vue'
import useSystemStore from '@/stores/system'
import router from '@/router'
import pinia from '@/stores/store'
import request from '@/utils/request'
import {ElMessage, ElMessageBox} from "element-plus";
import {useDark, useToggle} from "@vueuse/core";
import TabbarSettings from './settings/index.vue'
import AiChat from "@/layout/tabbar/ai/index.vue";
import {useRoute} from "vue-router";

const isFullScreen = ref(false)
const systemStore = useSystemStore(pinia)
// const backgroudColor = localStorage.getItem('bgc')
//   ? ref(localStorage.getItem('bgc'))
//   : ref('#2f4056')
const $router = router
const $route = useRoute()
const drawer = ref(false)
const aidrawer = ref(false)
// 修改图标
const changeIcon = () => {
  systemStore.fold = !systemStore.fold
}
const aiComp = ref(null)
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
        await $router.replace('/login')

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
  await $router.replace('/information')
}
//展示抽屉
const handleDrawer = () => {
  drawer.value = true

}
// 展示ai抽屉
const handleAiDrawer = async () => {
  aidrawer.value = true
  await aiComp.value.load()
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
// 关闭setting弹框
const handleChangeDrawer = async (value) => {
  drawer.value = value
}
// 关闭ai弹框
const handleChangeAiDrawer = async (value) => {
  aidrawer.value = value
}
const show = ref(true)
/**
 * 整体性路由使用$router
 * 单个路由对象使用$route
 */
watch(
    () => $route.path,
    (newPath, oldPath) => {
      show.value = false
      setTimeout(() => show.value = true, 100)
    }
)

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
      <div class="layout-breadcrumb el-zoom-in-center"
           v-if="show"
      >
        <!-- 折叠图标 -->
        <el-icon size="20" class="fold-icon" @click="changeIcon">
          <component :is="systemStore.fold ? Expand : Fold"/>
        </el-icon>

        <!-- 面包屑导航 -->
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item
              v-for="(item, index) in $route.matched"
              v-show="item.name"
              :key="index"
              :to="item.path"
          >
            <el-icon class="breadcrumb-icon">
              <component :is="item.meta.icon"/>
            </el-icon>
            <span class="breadcrumb-title">{{ item.meta.title }}</span>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>


    </el-col>

    <el-col :xs="24" :sm="14" :md="14" :lg="14" :xl="14">


      <div
          class="layout-tabbar__settings"
      >
        <el-tooltip
            class="box-item"
            effect="dark"
            content="AI助手"
            placement="top-start"
        >
          <el-button type="info" @click="handleAiDrawer" :icon="ChatDotRound" circle/>
        </el-tooltip>
        <el-tooltip
            class="box-item"
            effect="dark"
            content="外观设置"
            placement="top-start"
        >
          <el-button type="warning" @click="handleDrawer" :icon="Setting" circle/>
        </el-tooltip>
        <el-tooltip
            class="box-item"
            effect="dark"
            content="刷新页面"
            placement="top-start"
        >
          <el-button type="primary" @click="refsh" :icon="Loading" circle/>
        </el-tooltip>
        <el-tooltip
            class="box-item"
            effect="dark"
            content="全屏/非全屏展示"
            placement="top-start"
        >
          <el-button type="success" @click="changeFullScreen" :icon="FullScreen" circle/>
        </el-tooltip>
        <el-image :src="systemStore.userInfo.avatar" :size="32"
                  class="layout-tabbar__settings-avatar"
        ></el-image>

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
  <!--ai聊天组件-->
  <AiChat
      :aidrawer="aidrawer"
      ref="aiComp"
      @changeaidrawer="handleChangeAiDrawer"
  ></AiChat>

</template>

<style scoped lang="scss">
.layout-tabbar {
  height: 100%;
  box-shadow: 0px 1px 1px rgba(0, 0, 0, 0.1); /* 阴影效果 */
  .layout-breadcrumb {
    display: flex;
    align-items: center;
    height: 48px;
    padding: 0 10px;
    background-color: #fff;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  }

  .fold-icon {
    margin-right: 10px;
    cursor: pointer;
    color: #606266;
    transition: color 0.3s;
  }

  .fold-icon:hover {
    color: #409EFF;
  }

  .breadcrumb {
    flex: 1;
    display: flex;
    align-items: center;
    font-size: 14px;
  }

  .breadcrumb-icon {
    margin-right: 4px;
    color: #999;
  }

  .breadcrumb-title {
    color: #303133;
    font-weight: 500;
  }

  .layout-tabbar__settings {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: end;
    gap: 10px;

    .el-button {
      margin: 0;
    }

    .el-dropdown-link {
      cursor: pointer;

    }

    .layout-tabbar__settings-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
    }

    .layout-tabbar__settings-end {
      width: 40px;
    }
  }
}
</style>

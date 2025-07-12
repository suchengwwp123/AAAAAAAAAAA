<script setup>
import Tabbar from '@/layout/tabbar/index.vue'
import SysMenu from '@/layout/menu/index.vue'
import useSystemStore from '@/stores/system'
import Main from '@/layout/main/index.vue'
import Logo from '@/layout/menu/logo/index.vue'
import request from '@/utils/request'
import router from "@/router";
import {useRoute} from "vue-router";
const $router=router
const $route=useRoute()
const systemStore = useSystemStore()
</script>
<script>
export default {
  name: 'Layout'
}

</script>
<template>

  <!--整体布局-->
  <div class="layout">
    <!--    左侧菜单-->
    <div
        class="layout-menu"
        :class="{ fold: systemStore.fold ? true : false }"
        :style="{
        backgroundColor: systemStore.backgroundColor
      }"
    >
      <el-scrollbar>
        <Logo></Logo>
        <el-menu
            unique-opened
            :background-color="systemStore.backgroundColor"
            :text-color="systemStore.textColor"
            :collapse="systemStore.fold"
            :default-active="$route.path"
            :collapse-transition="false"
            :active-text-color="systemStore.activeTextcolor"
            router
        >
          <SysMenu :menuList="systemStore.routers"></SysMenu>
        </el-menu>
      </el-scrollbar>
    </div>
    <!--    头部导航栏-->
    <div class="layout-tabbar" :class="{ fold: systemStore.fold ? true : false }">
      <Tabbar></Tabbar>
    </div>

    <!--    main页面-->
    <div class="layout-main" :class="{ fold: systemStore.fold ? true : false }">
      <el-scrollbar>
        <a-watermark :content="systemStore.userInfo.username+'_'+systemStore.userInfo.nickname"
        v-if="systemStore.watermark==='1'"
        >
          <Main></Main>
        </a-watermark>
        <Main
        v-else
        ></Main>
      </el-scrollbar>
    </div>

  </div>

</template>

<style scoped lang="scss">
.el-menu {
  border-right: none !important;
}

.layout {
  width: 100%;
  height: 100dvh;

  .layout-menu {
    width: 200px;
    height: 100dvh;
    overflow: auto;
    position: fixed;
    z-index: 1;
    &.fold {
      width: 64px;
    }
    transition: all 0.1s ease-in-out;
  }

  .layout-tabbar {
    position: fixed;
    height: 50px;
    width: calc(100% - 200px);
    top: 0;
    left: 200px;
    background-color: #fff;
    z-index: 1;
    &.fold {
      width: calc(100% - 64px);
      left: 64px;
    }
    transition: all 0.1s ease-in-out;
  }

  .layout-main {
    z-index: 0;
    position: absolute;
    top: 50px;
    left: 200px;
    width: calc(100% - 200px);
    height: calc(100vh - 50px);
    overflow: auto;

    transition: all 0.1s ease-in-out;

    &.fold {
      width: calc(100% - 64px);
      left: 64px;
      transition: all 0.1s ease-in-out;
    }

  }

}

</style>

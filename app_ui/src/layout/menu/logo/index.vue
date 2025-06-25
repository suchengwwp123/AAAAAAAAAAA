<script setup>
import {ref, watch} from 'vue'
import useSystemStore from '@/stores/system'
import router from '@/router'
import settings from "../../../utils/settings";

const $route = router
const systemStore = useSystemStore()
const fold = ref(systemStore.fold)

// 监听 store.fold 变化，更新 fold 并切换 img key，强制触发动画
watch(() => systemStore.fold, (newVal) => {
  fold.value = false
  // 下一帧再变成 true，重置动画
  setTimeout(() => {
    fold.value = newVal
  }, 0)
})

const goHome = () => {
  $route.push('/')
}
</script>

<template>
  <div class="layout-menu__logo" :class="{ fold: fold }" @click="goHome">
    <!-- 用 key 强制重新渲染img，触发动画 -->
    <img
        :key="fold ? 'fold' : 'unfold'"
        class="layout-menu__logo-icon"
        :src="settings.logo_src"/>
    <span class="layout-menu__logo-systemname" v-if="!fold">{{ settings.layout_name }}</span>
  </div>
</template>

<style scoped lang="scss">
.layout-menu__logo {
  height: 50px;
  width: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.1s ease-in-out;

  .layout-menu__logo-icon {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    animation: dim-bright 0.5s ease forwards;
  }

  .layout-menu__logo-systemname {
    color: white;
    font-weight: bolder;
    cursor: pointer;
    animation: dim-bright 1s ease forwards;
  }


  &.fold {
    width: 64px;
    justify-content: center;

    .layout-menu__logo-systemname {
      display: none; /* 你想隐藏字体时用 */
    }
  }
}

@keyframes dim-bright {
  0% {
    opacity: 0;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}


</style>

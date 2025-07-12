<script setup>

import {useRoute} from 'vue-router';
import {reactive, ref, watch} from 'vue';


const $route = useRoute()
const show = ref(true)
/**
 * 整体性路由使用$router
 * 单个路由对象使用$route
 */
watch(
    () => $route.path,
    (newPath, oldPath) => {
      show.value=false
      setTimeout(() => show.value = true, 100)
    }
)
</script>


<script>
export default {
  name: 'Main'
}
</script>
<template>
  <!--  后续准备开发动画模块3.1.0-->
  <transition name="el-fade-in">
    <div class="layout-main__container"
         v-if="show">
      <router-view v-slot="{ Component, route }"
      >
        <component :is="Component" :key="route.path"/>
      </router-view>
    </div>
  </transition>
</template>

<style scoped lang="scss">

.layout-main__container {
  padding: 10px;
}


</style>

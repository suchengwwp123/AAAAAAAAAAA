<script setup>

import {RouterLink, useRouter} from 'vue-router';
import {reactive, ref, watch} from 'vue';
import useSystemStore from "@/stores/system";
import pinia from "@/stores/store";
import router from "@/router";

const $route = router
const show = ref(true)

watch(
    () => $route.currentRoute.value.fullPath,
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

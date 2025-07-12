<script setup>

import useSystemStore from '@/stores/system'
import { QuestionCircleOutlined } from '@ant-design/icons-vue';
import {onBeforeUnmount, onMounted, reactive, ref, watch} from 'vue'
import request from "@/utils/request";

const systemStore = useSystemStore()
// 监听窗口大小变化
const handleResize = () => {
  systemStore.fold = window.innerWidth < 768
  systemStore.updateWidth()
}
// main.js or App.vue





// 在组件挂载时开始监听
onMounted(() => {
  handleResize() // 初始化调用一次以确保初始状态设置正确
  window.addEventListener('resize', handleResize)
})

// 在组件销毁时移除事件监听器，以免内存泄漏
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
const message = ref('')
const entity=reactive(({}))
const  load=async () => {
  const res = await request.get('/notice/12')
  Object.assign(entity, res.data)

}
load()
</script>

<template>

<!--<div v-html="entity.content"></div>-->
  <RouterView />


</template>

<style scoped lang="scss"></style>

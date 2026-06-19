<script setup>
import useSystemStore from '@/stores/system'
import {onBeforeUnmount, onMounted, ref} from 'vue'
import PasswordGate from '@/components/PasswordGate.vue'

const systemStore = useSystemStore()

const handleResize = () => {
  systemStore.fold = window.innerWidth < 768
  systemStore.updateWidth()
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

const isPassed = ref(localStorage.getItem('gate_pass') === 'true')
const onPass = () => {
  isPassed.value = true
}
</script>

<template>
  <PasswordGate v-if="!isPassed" @pass="onPass" />
  <RouterView v-else />
</template>

<style scoped lang="scss"></style>

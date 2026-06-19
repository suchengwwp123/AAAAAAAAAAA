<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import useSystemStore from '@/stores/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const systemStore = useSystemStore()
const activeMenu = ref(route.path)

// 监听路由变化，自动更新选中状态
watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
})

const menuItems = [
  { index: '/front/index', label: '系统首页', icon: '🏠' },
  { index: '/front/vitae', label: '陪诊服务', icon: '🏥' },
  { index: '/front/order', label: '预约记录', icon: '📋' },

  { index: '/front/tickets', label: '工单记录', icon: '📝' },
  { index: '/front/complain', label: '投诉记录', icon: '💬' },
  { index: '/front/collect', label: '文章收藏', icon: '💬' },
  { index: '/front/contact', label: '联系我们', icon: '📞' },
  { index: '/front/info', label: '个人信息', icon: '👤' }
]

// 用户信息
const userInfo = computed(() => systemStore.userInfo)

const handleLogout = async () => {
  ElMessageBox.confirm('确认退出登录?', '系统提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await request.post('/auth/logout')
      await router.replace('/login')
      systemStore.reset()
      ElMessage({
        showClose: true,
        message: '退出登录成功！',
        type: 'success'
      })
    })
    .catch(() => {})
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-50 to-white">
    <!-- 顶部导航栏 -->
    <header
      class="sticky top-0 z-50 backdrop-blur-md bg-white/80 border-b border-gray-200 shadow-sm"
    >
      <nav class="w-full px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo区域 -->
          <div class="flex items-center space-x-2 group cursor-pointer">
            <div class="relative">
              <div
                class="absolute inset-0 bg-gradient-to-r from-teal-400 to-cyan-400 rounded-full blur opacity-75 group-hover:opacity-100 transition-opacity"
              ></div>
              <el-avatar
                src="/logo.png"
                :size="38"
                class="relative ring-2 ring-white shadow-lg"
              ></el-avatar>
            </div>
            <div>
              <h1
                class="text-base font-bold bg-gradient-to-r from-teal-600 to-cyan-600 bg-clip-text text-transparent"
              >
                医院陪诊系统
              </h1>
              <p class="text-[10px] text-gray-500">温暖相伴 • 贴心服务</p>
            </div>
          </div>

          <!-- 导航菜单 -->
          <div class="flex items-center space-x-1">
            <a
              v-for="item in menuItems"
              :key="item.index"
              @click="router.push(item.index)"
              :class="[
                'px-2 py-2 rounded-lg font-medium text-xs transition-all duration-300 cursor-pointer flex items-center space-x-1',
                activeMenu === item.index
                  ? 'bg-blue-500 text-white shadow-md shadow-blue-200'
                  : 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'
              ]"
            >
              <span>{{ item.icon }}</span>
              <span>{{ item.label }}</span>
            </a>
          </div>

          <!-- 右侧操作区 -->
          <div class="flex items-center space-x-4">
            <!-- 用户信息展示 -->
            <div class="flex items-center space-x-3 px-4 py-2 bg-teal-50 rounded-lg">
              <el-avatar :src="userInfo.avatar" :size="35" class="ring-2 ring-teal-200">
                <span class="text-teal-600 font-semibold">{{
                  userInfo.username?.charAt(0)?.toUpperCase() || 'U'
                }}</span>
              </el-avatar>
              <div class="flex flex-col">
                <span class="text-sm font-medium text-gray-800">{{
                  userInfo.username || '用户'
                }}</span>
                <span class="text-xs text-gray-500">{{
                  userInfo.email || userInfo.phone || ''
                }}</span>
              </div>
            </div>

            <!-- 退出登录按钮 -->
            <button
              @click="handleLogout"
              class="px-5 py-2 text-sm font-medium text-red-600 hover:text-white hover:bg-red-600 border border-red-600 rounded-lg transition-all duration-300"
            >
              退出登录
            </button>
          </div>
        </div>
      </nav>
    </header>

    <!-- 主内容区 -->
    <main class="w-full min-h-[calc(100vh-4rem)]">
      <div
        class="w-full bg-white shadow-sm border border-gray-100 overflow-hidden min-h-[calc(100vh-4rem)]"
      >
        <router-view v-slot="{ Component }">
          <transition
            enter-active-class="transition duration-300 ease-out"
            enter-from-class="opacity-0 translate-y-4"
            enter-to-class="opacity-100 translate-y-0"
            leave-active-class="transition duration-200 ease-in"
            leave-from-class="opacity-100"
            leave-to-class="opacity-0"
          >
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="mt-12 border-t border-gray-200 bg-gray-50">
      <div class="max-w-7xl mx-auto px-8 py-8">
        <div class="flex justify-between items-center">
          <div class="text-left">
            <p class="text-gray-600 text-sm">© 2025 医院陪诊系统 All rights reserved.</p>
            <p class="text-gray-400 text-xs mt-1">让就医更温暖，让关怀更贴心</p>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(to bottom, #14b8a6, #06b6d4);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(to bottom, #0d9488, #0891b2);
}
</style>
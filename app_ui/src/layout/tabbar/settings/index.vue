<script setup>
import useSystemStore from "@/stores/system";
import pinia from "@/stores/store";

import {ElMessage} from "element-plus";
import {ref} from "vue";


const systemStore = useSystemStore(pinia)
/**
 * 默认配色
 */
const predefineColors = ref([
  '#2f4056',
  '#FFFFFF',
  '#409EFF',
  '#ff4500',
  '#ff8c00',
  '#ffd700',
  '#90ee90',
  '#00ced1',
  '#1e90ff',
  '#c71585'
])
// 设置父组件传值
const props = defineProps(['drawer'])
// 子组件向父组件传值
const emit = defineEmits(['changeDrawer'])
// 设置背景色
const handleBgc = () => {
  if (systemStore.backgroundColor) {
    systemStore.setBgc(systemStore.backgroundColor)
  } else {
    systemStore.setBgc(localStorage.getItem('bgc') ? localStorage.getItem('bgc') : import.meta.env.VITE_BG_COLOR)
  }
}
const handleTextColor = () => {
  if (systemStore.textColor) {
    systemStore.setTextColor(systemStore.textColor)
  } else {
    systemStore.setTextColor(localStorage.getItem('tc') ? localStorage.getItem('tc') :  import.meta.env.VITE_TEXT_COLOR)
  }
}

const handleActiveTextcolor = () => {

  if (systemStore.activeTextcolor) {
    systemStore.setAtiveTextcolor(systemStore.activeTextcolor)
  } else {
    systemStore.setAtiveTextcolor(
        localStorage.getItem('at') ? localStorage.getItem('at') : import.meta.env.VITE_SELECTED_COLOR
    )
  }

}
const handleWaterMark = () => {

  if (systemStore.watermark) {
    systemStore.setWaterMark(systemStore.watermark)
  } else {
    systemStore.setWaterMark(
        localStorage.getItem('watermark') ? localStorage.getItem('watermark') : import.meta.env.VITE_IS_WATERMARK
    )
  }

  systemStore.setWaterMark(systemStore.watermark)
}
const resetSetting = () => {
  systemStore.setBgc(import.meta.env.VITE_BG_COLOR)
  systemStore.setTextColor(import.meta.env.VITE_TEXT_COLOR)
  systemStore.setAtiveTextcolor(import.meta.env.VITE_SELECTED_COLOR)
  systemStore.setWaterMark(import.meta.env.VITE_IS_WATERMARK)
  emit('changeDrawer', false)
  ElMessage({
    showClose: true,
    message: '重置成功',
    type: 'success'
  })
}
const handleBeforeClose = async () => {
  emit('changeDrawer', false)
}
const handleSumbmitForm = async () => {
  handleBgc()
  handleTextColor()
  handleActiveTextcolor()
  handleWaterMark()
  emit('changeDrawer', false)
  ElMessage({
    showClose: true,
    message: '保存成功，如果永久保存，请去.env多环境文件中进行详细配置',
    type: 'success'
  })
}

</script>
<script>
export default {
  name: 'TabbarSettings'
}
</script>

<template>
  <el-drawer :model-value="drawer" title="系统外观配置"
             :before-close="handleBeforeClose"
             size="40%"
  >

    <el-form inline>
      <el-row>
        <el-col>
          <el-form-item label="菜单背景">
            <el-color-picker
                v-model="systemStore.backgroundColor"
                size="small"

                :predefine="predefineColors"
            />
          </el-form-item>
          <el-form-item label="字体颜色">
            <el-color-picker
                v-model="systemStore.textColor"
                size="small"
                :predefine="predefineColors"
            />
          </el-form-item>
          <el-form-item label="选中颜色">
            <el-color-picker
                v-model="systemStore.activeTextcolor"
                size="small"
                :predefine="predefineColors"
            />
          </el-form-item>

        </el-col>
        <el-col>
          <el-form-item label="设置水印">
            <el-switch
                v-model="systemStore.watermark"
                inline-prompt
                active-text="展示"
                inactive-text="隐藏"
                :active-value="'1'"
                :inactive-value="'0'"
                style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            />
          </el-form-item>

        </el-col>
      </el-row>

    </el-form>
    <template
        #footer
    >
      <div>
        <div class="el-drawer__footer-container--line">
          <el-button @click="resetSetting">重置</el-button>
          <el-button type="primary" @click="handleSumbmitForm"> 保存</el-button>
        </div>
      </div>
    </template>
  </el-drawer>

</template>

<style scoped lang="scss">

</style>
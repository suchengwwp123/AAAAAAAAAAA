<script setup>
import useSystemStore from "@/stores/system";
import pinia from "@/stores/store";
import settings from "@/utils/settings";
import {ElMessage} from "element-plus";
import {ref} from "vue";
import {Refresh} from "@element-plus/icons-vue";

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
    systemStore.setBgc(localStorage.getItem('bgc') ? localStorage.getItem('bgc') : settings.menu_bgcolor)
  }
}
const handleTextColor = () => {
  if (systemStore.textColor) {
    systemStore.setTextColor(systemStore.textColor)
  } else {
    systemStore.setTextColor(localStorage.getItem('tc') ? localStorage.getItem('tc') : settings.menu_fontcolor)
  }
}

const handleActiveTextcolor = () => {

  if (systemStore.activeTextcolor) {
    systemStore.setAtiveTextcolor(systemStore.activeTextcolor)
  } else {
    systemStore.setAtiveTextcolor(
        localStorage.getItem('at') ? localStorage.getItem('at') : settings.menu_selected_bgcolor
    )
  }

}
const handleWaterMark = () => {
  systemStore.setWaterMark(systemStore.watermark)
}
const resetSetting = () => {
  systemStore.setBgc(settings.menu_bgcolor)
  systemStore.setTextColor(settings.menu_fontcolor)
  systemStore.setAtiveTextcolor(settings.menu_selected_bgcolor)
  systemStore.setWaterMark(settings.is_watermark)
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
    message: '保存成功，如果永久保存，请去app_ui/src/utils/settings.js进行配置',
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
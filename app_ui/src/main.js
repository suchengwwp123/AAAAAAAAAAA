import '@/styles/reset.css'
import '@/styles/global.css'
import '@/styles/tailwind.css'
import Antd from 'ant-design-vue';
import TDesign from 'tdesign-vue-next';
import TDesignChat from '@tdesign-vue-next/chat'; // 引入chat组件
import '@tdesign-vue-next/chat/es/style/index.css'; // 引入chat组件的少量全局样式变量
// 导入elementui
import 'ant-design-vue/dist/reset.css';
import ElementPlus from 'element-plus'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import Directive from '@/utils/index'
import locale from 'element-plus/es/locale/lang/zh-cn'
import pinia from '@/stores/store'
import App from './App.vue'
import router from './router'
import {createApp} from 'vue'
import UndrawUi from 'undraw-ui'
import 'undraw-ui/dist/style.css'
import '@/styles/theme.scss'
const app = createApp(App)
app.use(UndrawUi)
app.use(router)
app.use(pinia)
// 使用饿了么ui
app.use(ElementPlus, {locale})
// 导入图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
// 注册自定义事件
app.use(Directive)

app.use(Antd)
app.use(TDesign).use(TDesignChat);



app.mount('#app')

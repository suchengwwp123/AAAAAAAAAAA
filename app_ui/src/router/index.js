import {createRouter, createWebHistory} from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import useSystemStore from '@/stores/system'
import pinia from '@/stores/store'
import constrouters, {getRouters} from '@/router/routers'
import {ref} from 'vue'
import request from '../utils/request'

const systemStore = useSystemStore(pinia)

// 进度条配置项这样写
NProgress.configure({
    easing: 'ease', // 动画方式，和css动画属性一样（默认：ease）
    speed: 500, // 递增进度条的速度，单位ms（默认： 200）
    showSpinner: true, // 是否显示加载ico
    trickle: true, //是否自动递增
    trickleSpeed: 200, // 自动递增间隔
    minimum: 0.3 // 初始化时的最小百分比，0-1（默认：0.08）
})
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: constrouters,
    scrollBehavior(to, from, savedPosition) {
        // 如果有保存的位置（例如浏览器后退），则返回到该位置
        if (savedPosition) {
            return savedPosition
        } else {
            // 否则滚动到顶部
            return { top: 0, behavior: 'smooth' }
        }
    }
})
const whiteUrl = [
    '/login', '/register', '/forget'
]
// 路由前置拦截
router.beforeEach(async (to, from, next) => {
    NProgress.start()
    if (whiteUrl.includes(to.path)) {
        next()
    } else if (!systemStore.hasRouter) {
        const res = await request.get('/auth/authInfo')
        systemStore.routers = res.data.routers
        systemStore.userInfo = res.data.userInfo
        systemStore.permissionPerms = res.data.permissionPerms
        const asyncRouters = getRouters(systemStore.routers)
        asyncRouters.forEach((req) => {
            router.addRoute(req)
        })
        systemStore.routers.unshift({
            path: "/home",
            name: "home",
            component: "/system/home/index",
            meta: {
                icon: "HomeFilled",
                title: "系统首页",
                hidden: false
            },
            children: []
        })
        systemStore.hasRouter = true

        next({ ...to, replace: true })

    } else {
        next()
    }
})
// 路由跳转后钩子函数中 - 执行进度条加载结束
router.afterEach((to, from, next) => {
    NProgress.done()
})
export default router

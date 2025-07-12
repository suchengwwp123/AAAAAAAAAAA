import {defineStore} from 'pinia'


// useStore 可以是 useUser、useCart 之类的任何东西
// 第一个参数是应用程序中 store 的唯一 id
const useSystemStore = defineStore('System', {
    // other options...
    // 推荐使用 完整类型推断的箭头函数
    state: () => {
        return {
            // 所有这些属性都将自动推断其类型
            fold: false,
            token: localStorage.getItem('token'),
            userInfo: {},
            hasRouter: false,
            routers: [],
            windowWidth: window.innerWidth,
            permissionPerms: [],
            backgroundColor: localStorage.getItem('bgc') ? localStorage.getItem('bgc') : import.meta.env.VITE_BG_COLOR,
            textColor: localStorage.getItem('tc') ? localStorage.getItem('tc') : import.meta.env.VITE_TEXT_COLOR,
            activeTextcolor: localStorage.getItem('at') ? localStorage.getItem('at') : import.meta.env.VITE_SELECTED_COLOR,
            watermark: localStorage.getItem('watermark') ? localStorage.getItem('watermark') : import.meta.env.VITE_IS_WATERMARK,
        }
    },
    actions: {
        setToken(token) {
            localStorage.setItem('token', token)
            this.token = localStorage.getItem('token')
        },
        setBgc(value) {
            localStorage.setItem('bgc', value)
            this.backgroundColor = localStorage.getItem('bgc')
        },
        setTextColor(value) {
            localStorage.setItem('tc', value)
            this.textColor = localStorage.getItem('tc')
        },
        setAtiveTextcolor(value) {

            localStorage.setItem('at', value)
            this.activeTextcolor = localStorage.getItem('at')
        },
        setWaterMark(value) {

            localStorage.setItem('watermark', value)
            this.watermark = localStorage.getItem('watermark')

        },
        setUserInfo(userInfo) {
            this.userInfo = userInfo
        },
        setRouters(routers) {
            this.routers = routers
        },
        setAuthorities(permissionPerms) {
            this.permissionPerms = permissionPerms
        },
        updateWidth() {
            this.windowWidth = window.innerWidth
        },
        reset() {
            localStorage.clear()
            ;(this.token = undefined),
                (this.userInfo = {}),
                (this.routers = []),
                (this.permissionPerms = [])
            this.hasRouter = false
        }
    },
    getters: {}
})
export default useSystemStore

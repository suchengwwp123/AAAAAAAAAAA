import axios from 'axios'
import useSystemStore from '@/stores/system'
import pinia from '@/stores/store'

import {ElMessage} from 'element-plus'
import router from '@/router'
import 'nprogress/nprogress.css'
import {useRequestThrottleStore} from '@/stores/request'
import NProgress from "nprogress";

const $router = router
const systemStore = useSystemStore(pinia)
/**
 * 防抖白名单
 * @type {string[]}
 */
const whiteList = [
    '/user/onlinepage',
    '/file/upload'


]
NProgress.configure({
    easing: 'ease', // 动画方式，和css动画属性一样（默认：ease）
    speed: 500, // 递增进度条的速度，单位ms（默认： 200）
    showSpinner: true, // 是否显示加载ico
    trickle: true, //是否自动递增
    trickleSpeed: 200, // 自动递增间隔
    minimum: 0.3 // 初始化时的最小百分比，0-1（默认：0.08）
})
const request = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 100000
})


const requestThrottleStore = useRequestThrottleStore()
const generateReqKey = (config) => `${config.method}&${config.url}`


// ✅ 请求拦截器
request.interceptors.request.use((config) => {

    // ✅ 加 token
    if (systemStore.token) {
      // console.log(systemStore.token)
        config.headers.Authorization = systemStore.token
    }
    // ✅ 判断是否跳过防抖
    const key = generateReqKey(config)
    // ✅ 判断是否命中白名单
    const isWhiteListed = whiteList.some((path) => config.url?.includes(path))
    NProgress.start()
    // if (!isWhiteListed) {
    //     // ✅ 非白名单接口，做请求节流判断
    //     if (!requestThrottleStore.canRequest(key, 500)) {
    //         return Promise.reject(new axios.Cancel('请求过于频繁，已被取消'))
    //     }
    // }
    return config
})

// ✅ 响应拦截器
request.interceptors.response.use(
    (response) => {
        NProgress.done()
        if (response.data instanceof Blob) {
            return response
        } else if (response.data.code === 200) {
            return response.data
        } else {
            showErrorMessage(response.data.msg || '系统异常，请联系管理员！')
            return Promise.reject(response.data.msg)
        }
    },
    (error) => {

        NProgress.done()


        console.log(error)
        // 超时处理
        if (error.code === 'ECONNABORTED' || error.message === "Network Error" || error.message.includes("timeout")) {
            showErrorMessage('请求超时，请检查网络或稍后再试！')
            return Promise.reject('请求超时')
        }


        const status = error.response.status
        const msg = error.response.data?.msg || ''

        switch (status) {
            case 400:
                showErrorMessage(msg || '请求参数有误')
                break
            case 401:
                systemStore.reset()
                $router.replace('/login')
                break
            case 403:
                showErrorMessage('无权限访问当前资源')
                break
            case 404:
                showErrorMessage('接口地址不存在')
                break
            case 405:
                showErrorMessage('请求方法不被允许')
                break
            case 408:
                showErrorMessage('请求超时')
                break
            case 415:
                showErrorMessage('请求类型不支持，请检查 Content-Type')
                break
            case 422:
                showErrorMessage('参数格式校验失败')
                break
            case 429:
                showErrorMessage('请求过于频繁，请稍后再试')
                break
            case 500:
                showErrorMessage(msg || '服务器内部错误，请联系管理员')
                break
            case 502:
                showErrorMessage('网关错误')
                break
            case 503:
                showErrorMessage('服务不可用，可能正在维护')
                break
            case 504:
                showErrorMessage('网关超时，请稍后重试')
                break
            default:
                showErrorMessage(`系统异常（${status}）`)
        }

        return Promise.reject(error.response.data?.msg || '请求异常')
    }
)

// ✅ 弹窗封装
const showErrorMessage = (msg) => {
    ElMessage({
        showClose: true,
        message: msg,
        type: 'error'
    })
}

export default request

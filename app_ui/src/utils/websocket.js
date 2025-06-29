// utils/useWebSocket.js
import useSystemStore from "@/stores/system";
import pinia from "@/stores/store";


const systemStore = useSystemStore(pinia)

export function useWebSocket(channelId) {
    let socket = null
    const messages = []
    let onMessageCallback = () => {
    }

    // 🔒 固定写死 token


    const connect = () => {
        const wsUrl = `${import.meta.env.VITE_WS_URL}/ws-connect?channel=${channelId}&Authorization=${systemStore.token}`
        socket = new WebSocket(wsUrl)

        socket.onopen = () => {
            console.log('✅ WebSocket 连接成功')
            messages.push('[连接成功]')
        }

        socket.onmessage = (event) => {
            console.log('📨 收到消息:', event.data)
            messages.push('服务器：' + event.data)
            onMessageCallback(event.data)
        }

        socket.onerror = (err) => {
            console.error('❌ WebSocket 错误:', err)
            messages.push('[连接出错]')
        }

        socket.onclose = () => {
            console.log('🔌 WebSocket 连接关闭')
            messages.push('[连接已关闭]')
        }
    }

    const send = (msg) => {
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(msg)
        } else {
            messages.push('[未连接，无法发送]')
        }
    }

    const disconnect = () => {
        if (socket) {
            socket.close()
            socket = null
        }
    }

    const onMessage = (callback) => {
        onMessageCallback = callback
    }

    return {
        connect,
        disconnect,
        send,
        onMessage,
        getMessages: () => messages,
    }
}

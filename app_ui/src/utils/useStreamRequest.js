// utils/useStreamRequestWithAbort.js

import pinia from "@/stores/store";
import useSystemStore from "@/stores/system";
const api=import.meta.env.VITE_API_URL
let controller = null; // 全局变量保存 controller 实例

const systemStore = useSystemStore(pinia)
export async function streamRequest(modelType, prompt, deepThink, onDelta, onFinish) {
    controller = new AbortController();

    try {
        const response = await fetch(`${api}/ai/stream/${encodeURIComponent(modelType)}/${encodeURIComponent(prompt)}/${encodeURIComponent(deepThink)}`, {
            method: 'GET',
            signal: controller.signal,
            headers: {
                'Authorization': systemStore.token, // 或根据你的实际存储方式改为 cookie、sessionStorage 等
            },
        });

        if (!response.ok) {
            console.error('❌ 请求失败：', response.status);
            return;
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let fullText = '';

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            const chunk = decoder.decode(value, { stream: true });
            fullText += chunk;
            if (onDelta) onDelta(chunk);
        }

        if (onFinish) onFinish(fullText);
    } catch (err) {
        if (err.name === 'AbortError') {
            console.warn('🚫 流式请求已被手动终止');
        } else {
            console.error('❌ 请求异常：', err);
        }
    }
}
/**
 * 停止流式请求
 */
export function stopStream() {
    if (controller) {
        controller.abort();
        controller = null;
        return true
    }
}

// stores/requestThrottle.js
import {defineStore} from 'pinia'

export const useRequestThrottleStore = defineStore('requestThrottle', {
    state: () => ({
        lastRequestTimeMap: JSON.parse(localStorage.getItem('lastRequestTimeMap') || '{}')
    }),
    actions: {
        canRequest(key, interval = 500) {
            const now = Date.now()
            if (!this.lastRequestTimeMap[key] || now - this.lastRequestTimeMap[key] > interval) {
                this.lastRequestTimeMap[key] = now
                localStorage.setItem('lastRequestTimeMap', JSON.stringify(this.lastRequestTimeMap))
                return true
            }
            return false
        }
    }

})

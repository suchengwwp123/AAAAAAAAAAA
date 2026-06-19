<template>
  <div class="password-gate">
    <div class="gate-card">
      <div class="lock-icon">🔒</div>
      <h2>医院陪诊系统</h2>
      <p class="subtitle">请输入访问密码</p>
      <div class="input-group">
        <input
          v-model="inputPassword"
          type="password"
          placeholder="请输入密码"
          @keyup.enter="checkPassword"
          :class="{ error: showError }"
          ref="passwordInput"
        />
        <button @click="checkPassword">进入</button>
      </div>
      <p v-if="showError" class="error-text">密码错误，请重试</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const PASSWORD = '200401'
const inputPassword = ref('')
const showError = ref(false)
const passwordInput = ref(null)

onMounted(() => {
  passwordInput.value?.focus()
})

const emit = defineEmits(['pass'])

const checkPassword = () => {
  if (inputPassword.value === PASSWORD) {
    localStorage.setItem('gate_pass', 'true')
    emit('pass')
  } else {
    showError.value = true
    inputPassword.value = ''
    setTimeout(() => {
      showError.value = false
    }, 2000)
  }
}
</script>

<style scoped>
.password-gate {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 99999;
}
.gate-card {
  background: white;
  padding: 48px 40px;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  text-align: center;
  max-width: 380px;
  width: 90%;
}
.lock-icon {
  font-size: 48px;
  margin-bottom: 16px;
}
h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1a1a2e;
  font-weight: 600;
}
.subtitle {
  margin: 0 0 28px;
  font-size: 14px;
  color: #888;
}
.input-group {
  display: flex;
  gap: 10px;
}
.input-group input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
}
.input-group input:focus {
  border-color: #667eea;
}
.input-group input.error {
  border-color: #ff4d4f;
}
.input-group button {
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
  white-space: nowrap;
  transition: opacity 0.2s;
}
.input-group button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.error-text {
  margin: 12px 0 0;
  color: #ff4d4f;
  font-size: 13px;
}
</style>

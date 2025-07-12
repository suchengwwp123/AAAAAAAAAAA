<script setup>
import {onBeforeMount, reactive, ref} from 'vue'
import request from '@/utils/request'
import {ElMessage, ElMessageBox, ElNotification} from 'element-plus'
import useSystemStore from '@/stores/system'
import router from '@/router'


import {rsaEncrypt} from '@/utils/rsa'
import LoginCover from "../../../components/SystembackgroundCover.vue";
import SystembackgroundCover from "../../../components/SystembackgroundCover.vue";
import SystemBackGroundInfo from "@/components/SystemBackGroundInfo.vue";

const copyright = import.meta.env.VITE_COPYRIGHT_NAME
const $router = router
const systemStore = useSystemStore()
const formSize = ref('default')
const ruleFormRef = ref()
const captchaImg = ref()

const ruleForm = reactive({
  username: '',
  password: '',
  code: '',
  token: ''
})

// 校验规则
const rules = reactive({
  username: [
    {required: true, message: '请输入账户名', trigger: 'blur'},
    {
      pattern: /^(?!\d+$)[A-Za-z\d]{5,10}$/,
      message: '账户名必须是5到10位字母或字母数字组合',
      trigger: 'blur'
    }
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 5, max: 10, message: '密码在5到10位', trigger: 'blur'},
    {
      pattern: /^[A-Za-z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]{5,10}$/,
      message: '密码格式不正确',
      trigger: 'blur'
    }
  ],
  code: [
    {required: true, message: '请输入验证码', trigger: 'blur'},
    {
      pattern: /^\d{5}$/,
      message: '验证码必须是5位数字',
      trigger: 'blur'
    }
  ]
})
//登录方法
const submitForm = async (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid, fields) => {
    if (valid) {
      try {

        const encrypted = rsaEncrypt.encrypt(ruleForm.password)
        const submitData = {
          username: '',
          password: '',
          code: '',
          token: ''
        }
        Object.assign(submitData, ruleForm)
        submitData.password = encrypted
        const res = await request.post('/auth/login', submitData)

        systemStore.setToken(res.data.tokenValue)
        ElNotification({
          title: '登录成功',
          message: `${ruleForm.username} 欢迎您!`,
          type: 'success'
        })

        $router.replace('/')
      } catch (error) {
        getCaptcha()
      }
    }
  })
}
//重置方法
const resetForm = (formEl) => {
  if (!formEl) return
  formEl.resetFields()
  getCaptcha()
}
//获取验证码
const getCaptcha = async () => {
  const res = await request.get('/auth/captcha')
  captchaImg.value = res.data.captchaImg
  ruleForm.token = res.data.token
  ruleForm.code = ''
}
// 初始化获取公钥
const initRsa = async () => {
  try {
    const res = await request.get('/auth/public-key')
    rsaEncrypt.init(res.data)
  } catch (error) {
    console.error('获取公钥失败:', error)
    throw error
  }
}
// 判断是否登录
const checkLogin = async () => {
  try {
    const res = await request.get(`/auth/islogin`)
    if (res.data) {
      ElMessageBox.confirm(
          '系统检测到当前您已经登录了，确认重新登录？',
          '重要提示',
          {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
          }
      )
          .then(async () => {
            await request.post(`/auth/logout`)
            await initRsa()
            await getCaptcha()
          })
          .catch(async () => {
            // ElMessage({
            //   type: 'info',
            //   message: '取消成功',
            // })
            $router.replace(`/`)
          })
    } else {
      await initRsa()
      await getCaptcha()
    }

  } catch (e) {
    console.log(e)

  }
}

onBeforeMount(() => {
  checkLogin()

})


</script>

<template>
  <el-row
      class="login"
      justify="center"
  >
    <el-col :xs="0" :sm="0" :md="12" :lg="16" :xl="16"
    >
      <SystemBackGroundInfo></SystemBackGroundInfo>
    </el-col>
    <el-col :xs="24" :sm="18" :md="12" :lg="8" :xl="8"
    >
      <div
          class="login-form-container"
      >
        <div
            class="login-form-container__header"
        >
          <h1 class="login-form-container__header-title text-3xl">登录</h1>
          <el-text>没有账户?</el-text>

          <el-text type="warning"
                   class="login-form-container__header-link"
                   @click="$router.push(`/register`)">
            立即注册
          </el-text>
        </div>

        <el-form
            class="login-form-container__content"
            ref="ruleFormRef"
            :model="ruleForm"
            :rules="rules"
            :size="formSize"
            status-icon
        >
          <el-form-item  prop="username">
            <el-input v-model="ruleForm.username"

                      placeholder="请输入账户名"
                      prefix-icon="User"/>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
                v-model="ruleForm.password"
                prefix-icon="Lock"
                placeholder="请输入密码"
                type="password"
                show-password
            />
          </el-form-item>
          <el-form-item  prop="code">
            <div class="login-form-container__content-captcha">
              <el-input
                  v-model="ruleForm.code"
                  placeholder="请输入验证码"
                  prefix-icon="Crop"

              />
              <el-image
                  :src="captchaImg"
                  @click="getCaptcha"
                  class="login-form-container__content-captcha-img"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon>
                      <MagicStick/>
                    </el-icon>
                  </div>
                </template>
              </el-image>
            </div>
          </el-form-item>
          <el-button type="primary" class="login-form-container__content-submitbutton--full"
                     @click="submitForm(ruleFormRef)">
            立即登录
          </el-button>
          <div class="login-form-container__content-forgetbutton--text">
            <el-button type="success" text @click="$router.push(`/forget`)">忘记密码？</el-button>
          </div>
        </el-form>
        <footer class="login-form-container__footer">
          <el-text>{{ copyright }}</el-text>
        </footer>

      </div>
    </el-col>
  </el-row>

</template>

<style scoped lang="scss">
.login {
  width: 100%;
  height: 100vh;


  .login-form-container {
    padding: 0px 30px;
    display: flex;
    flex-direction: column;
    justify-content: end;
    height: 100dvh;

    .login-form-container__header {
      height: 20vh;

      .login-form-container__header-title {
      }

      .login-form-container__header-link {
        cursor: pointer;
      }
    }

    .login-form-container__content {
      //border: 1px solid red;
      height: 50vh;

      .login-form-container__content-captcha {
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 10px;

        .login-form-container__content-captcha-img {
          cursor: pointer;
          border-radius: 3px;
        }
      }

      .login-form-container__content-submitbutton--full {
        width: 100%;
        border-radius: 0
      }

      .login-form-container__content-forgetbutton--text {
        display: flex;
        justify-content: end;
      }

    }

    .login-form-container__footer {
      height: 10vh;
      display: flex;
      justify-content: center;

    }
  }

}


</style>

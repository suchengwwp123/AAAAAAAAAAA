<script setup>
import {reactive, ref} from 'vue'
import useSystemStore from '@/stores/system'
import pinia from '@/stores/store'
import request from '@/utils/request'
import {ElMessage, ElNotification} from 'element-plus'
import router from '@/router'
import settings from "@/utils/settings";
import {rsaEncrypt} from '@/utils/rsa'
import SystembackgroundCover from "../../../components/SystembackgroundCover.vue";

const $route = router
const systemStore = useSystemStore(pinia)
const userInfo = systemStore.userInfo
const captchaImg = ref('')


setTimeout(() => show.value = true, 500)
const formSize = ref('default')
const ruleFormRef = ref()
const ruleForm = reactive({
  avatar: 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',
  nickname: undefined,
  username: undefined,
  phone: undefined,
  email: undefined,
  content: undefined,
  password: undefined,
  checkPass: undefined
})

// 校验新密码
const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 5 || value.length > 10) {
    return callback(new Error('密码长度在5到10位之间'))
  } else {
    if (ruleForm.checkPass !== '') {
      if (!ruleFormRef.value) return
      ruleFormRef.value.validateField('checkPass', () => null)
    }
    callback()
  }
}
// 确认密码
const validatePass2 = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value.length < 5 || value.length > 10) {
    return callback(new Error('密码长度在5到10位之间'))
  } else if (!ruleForm.password) {
    return ruleFormRef.value.validateField('password', () => null)
  } else if (value !== ruleForm.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}
const rules = reactive({
  username: [
    {required: true, message: '输入账户名', trigger: 'blur'},
    {min: 5, max: 10, message: '长度在5到10位之间', trigger: 'blur'}
  ],
  nickname: [
    {required: true, message: '输入用户昵称', trigger: 'blur'},
    {min: 2, max: 5, message: '长度在2到5位之间', trigger: 'blur'}
  ],
  phone: [
    {required: true, message: '请输入手机号码', trigger: 'blur'},
    {
      pattern: /^1[3456789]\d{9}$/,
      message: '手机号格式不正确',
      trigger: 'blur'
    }
  ],
  email: [
    {required: true, message: '请输入邮箱号', trigger: 'blur'},
    {
      type: 'email',
      message: '邮箱地址格式不正确',
      trigger: ['blur']
    }
  ],
  code: [
    {required: true, message: '请输入验证码', trigger: 'blur'},
    {min: 5, max: 5, message: '验证码为长度为5', trigger: 'blur'}
  ],
  password: [{required: true, validator: validatePass, trigger: 'blur'}],
  checkPass: [{required: true, validator: validatePass2, trigger: 'blur'}]
})

const submitForm = async (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid, fields) => {
    if (valid) {
      try {
        const encrypted = rsaEncrypt.encrypt(ruleForm.password)
        const submitData = {
          avatar: 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',
          nickname: undefined,
          username: undefined,
          phone: undefined,
          email: undefined,
          content: undefined,
          password: undefined,
          checkPass: undefined
        }
        Object.assign(submitData, ruleForm)
        submitData.password = encrypted
        submitData.checkPass = encrypted
        await request.post('/auth/register', submitData)

        ElNotification({
          title: '注册成功',
          message: `${ruleForm.nickname} 欢迎您!`,
          type: 'success'
        })

        $route.replace('/login')
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

const options = Array.from({length: 10000}).map((_, idx) => ({
  value: `${idx + 1}`,
  label: `${idx + 1}`
}))
const handleAvatarSuccess = (response, uploadFile) => {
  ruleForm.avatar = URL.createObjectURL(uploadFile.raw)
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
initRsa()
getCaptcha()
</script>
<script>
export default {
  name: 'Register'
}
</script>
<template>

  <el-row
      class="register"
      justify="center"
  >
    <el-col :xs="0" :sm="0" :md="12" :lg="16" :xl="16"

    >
      <SystembackgroundCover></SystembackgroundCover>
    </el-col>
    <el-col :xs="24" :sm="18" :md="12" :lg="8" :xl="8"
    >
      <div
          class="register-form-container"
      >
        <div
            class="register-form-container__header"
        >
          <h1 class="register-form-container__header-title">注册</h1>
          <el-text>已有账户?</el-text>

          <el-text type="warning"
                   class="register-form-container__header-link"
                   @click="$route.push(`/login`)">
            立即登录
          </el-text>
        </div>

        <el-form
            class="register-form-container__content"
            ref="ruleFormRef"
            :model="ruleForm"
            :rules="rules"
            label-width="80px"
            :size="formSize"
            status-icon
        >
          <el-form-item label="账户名称" prop="username">
            <el-input v-model="ruleForm.username"
                      placeholder="请输入账户名称"
            />
          </el-form-item>


          <el-form-item label="用户昵称" prop="nickname">
            <el-input v-model="ruleForm.nickname"
                      placeholder="请输入用户昵称"
            />
          </el-form-item>


          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="ruleForm.phone"
                      placeholder="请输入手机号"
            />
          </el-form-item>


          <el-form-item label="个人邮箱" prop="email">
            <el-input v-model="ruleForm.email"
                      placeholder="请输入邮箱号"
            />
          </el-form-item>

          <el-form-item label="登录密码" prop="password">
            <el-input v-model="ruleForm.password" type="password"
                      placeholder="请输入登录密码"
                      autocomplete="off"/>
          </el-form-item>
          <el-form-item label="确认密码" prop="checkPass">
            <el-input v-model="ruleForm.checkPass" type="password"
                      placeholder="请确认登录密码"
                      autocomplete="off"/>
          </el-form-item>

          <el-form-item label="验证码&nbsp;&nbsp;" prop="code">
            <div class="register-form-container__content-captcha">
              <el-input
                  v-model="ruleForm.code"
                  placeholder="请输入验证码"
                  prefix-icon="Crop"

              />
              <el-image
                  :src="captchaImg"
                  @click="getCaptcha"
                  class="register-form-container__content-captcha-img"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon>
                      <icon-picture/>
                    </el-icon>
                  </div>
                </template>
              </el-image>
            </div>
          </el-form-item>
          <el-button type="primary" class="register-form-container__content-submitbutton--full"
                     @click="submitForm(ruleFormRef)">
            立即注册
          </el-button>

        </el-form>
        <footer class="register-form-container__footer">
          <el-text>{{ settings.copyright }}</el-text>
        </footer>

      </div>
    </el-col>
  </el-row>
</template>

<style scoped lang="scss">
.register {
  width: 100%;
  height: 100vh;


  .register-form-container {
    height: 100vh;
    padding: 0px 80px;
    display: flex;
    flex-direction: column;
    justify-content: end;

    .register-form-container__header {
      height: 15vh;

      .register-form-container__header-title {
      }

      .register-form-container__header-link {
        cursor: pointer;
      }
    }

    .register-form-container__content {
      //border: 1px solid red;
      height: 60vh;

      .register-form-container__content-captcha {
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 10px;

        .register-form-container__content-captcha-img {
          cursor: pointer;
          border-radius: 3px;
        }
      }

      .register-form-container__content-submitbutton--full {
        width: 100%;
        border-radius: 0
      }


    }

    .register-form-container__footer {
      height: 10vh;
      display: flex;
      justify-content: center;

    }
  }

}


</style>

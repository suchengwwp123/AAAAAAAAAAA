<script setup>
import { reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import router from '../../../router'
import settings from "@/utils/settings";
import { rsaEncrypt } from '@/utils/rsa'
import SystembackgroundCover from "../../../components/SystembackgroundCover.vue";
const ruleFormRef = ref()
const show=ref(false)
const $route=router
const formSize = ref('default')
setTimeout(()=>show.value = true,500)
// 校验原密码
const checkPass = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入原密码'))
  }

  if (value.toString().length < 5 || value.toString().length > 10) {
    return callback(new Error('密码长度在5到10位之间'))
  }
  callback()
}
// 校验新密码
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else if (value.toString().length < 5 || value.toString().length > 10) {
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
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value.toString().length < 5 || value.toString().length > 10) {
    return callback(new Error('密码长度在5到10位之间'))
  } else if (!ruleForm.password) {
    return ruleFormRef.value.validateField('password', () => null)
  } else if (value !== ruleForm.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const ruleForm = reactive({
  password: '',
  checkPass: '',
  oldpass: ''
})

const rules = reactive({
  password: [
    { required: true, message: '输入新密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  checkPass: [
    { required: true, message: '确认新密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  username: [
    { required: true, message: '输入用户账户名', trigger: 'blur' },
    { min: 5, max: 10, message: '长度在5到10位之间', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '输入邮箱验证码', trigger: 'blur' },
    {min: 5, max: 5, message: '验证码为长度为5', trigger: 'blur'}
  ]
})

const submitForm = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {


      const oldencrypted = rsaEncrypt.encrypt(ruleForm.oldpass)
      const passwordencrypted = rsaEncrypt.encrypt(ruleForm.password)
      const checkencrypted = rsaEncrypt.encrypt(ruleForm.checkPass)
      const submitData={
        password: '',
        checkPass: '',
        oldpass: ''
      }
      Object.assign(submitData,ruleForm)
      submitData.oldpass=oldencrypted
      submitData.password=passwordencrypted
      submitData.checkPass=checkencrypted
      await request.post('/auth/findpassword', submitData)
      ElMessage({
        showClose: true,
        message: '修改密码成功！',
        type: 'success'
      })
     $route.go(-1)
    } else {
      return false
    }
  })
}
// 初始化获取公钥
const initRsa = async () => {
  try {
    const res= await request.get('/auth/public-key')
    rsaEncrypt.init(res.data)
  } catch (error) {
    console.error('获取公钥失败:', error)
    throw error
  }
}
initRsa()
const resetForm = (formEl) => {
  if (!formEl) return
  formEl.resetFields()
}
// 获取验证码
const handleGetEmailCode = async () => {
  if (!ruleForm.username) {
    ElMessage({
      showClose: true,
      message: '账户名不能为空',
      type: 'error'
    })
    return false
  } else {
    await request.get(`/auth/email/${ruleForm.username} `)
    ElMessage({
      showClose: true,
      message: '验证码已经发送到您的邮箱，有效时间为5分钟,请及时填写！',
      type: 'success'
    })
  }
}
</script>
<script>
export default {
  name: 'Forget'
}
</script>
<template>
  <el-row
      class="forget"
      justify="center"
  >
    <el-col :xs="0" :sm="0" :md="12" :lg="16" :xl="16"

    >
      <SystembackgroundCover />
    </el-col>
    <el-col :xs="24" :sm="18" :md="12" :lg="8" :xl="8"
    >
      <div
          class="forget-form-container"
      >
        <div
            class="forget-form-container__header"
        >
          <h1 class="forget-form-container__header-title">找回密码</h1>
          <el-text>已知密码?</el-text>

          <el-text type="warning"
                   class="forget-form-container__header-link"
                   @click="$route.push(`/login`)">
            立即登录
          </el-text>
        </div>

        <el-form
            class="forget-form-container__content"
            ref="ruleFormRef"
            :model="ruleForm"
            :rules="rules"
            label-width="80px"
            :size="formSize"
            status-icon
        >
          <el-form-item label="账户名&nbsp;&nbsp;&nbsp;&nbsp;" prop="username">
            <el-input v-model="ruleForm.username" autocomplete="off"
            placeholder="请输入账户名"
            />
          </el-form-item>
          <el-form-item label="新的密码" prop="password">
            <el-input v-model="ruleForm.password"
                      placeholder="请输入新的密码"
                      type="password" autocomplete="off" />
          </el-form-item>
          <el-form-item label="确认密码" prop="checkPass">
            <el-input v-model="ruleForm.checkPass"
                      placeholder="请确认密码"
                      type="password" autocomplete="off" />
          </el-form-item>
          <el-form-item label="验证码&nbsp;&nbsp;&nbsp;&nbsp;" prop="code">
            <el-row :gutter="10">
              <el-col :span="12">
                <el-input v-model="ruleForm.code"
                          placeholder="请输入验证码"
                          autocomplete="off" />
              </el-col>
              <el-col :span="12">
                <el-button type="warning" @click="handleGetEmailCode">获取验证码</el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <el-button type="primary" class="forget-form-container__content-submitbutton--full" @click="submitForm(ruleFormRef)">
            找回密码
          </el-button>
        </el-form>
        <footer class="forget-form-container__footer">
          <el-text>{{ settings.copyright }}</el-text>
        </footer>

      </div>
    </el-col>
  </el-row>
</template>

<style scoped lang="scss">
.forget {
  width: 100%;
  height: 100vh;



  .forget-form-container {
    padding: 0px 80px;
    display: flex;
    flex-direction: column;
    justify-content: end;
    height: 100vh;

    .forget-form-container__header {
      height: 20vh;

      .forget-form-container__header-title {
      }

      .forget-form-container__header-link {
        cursor: pointer;
      }
    }

    .forget-form-container__content {
      //border: 1px solid red;
      height: 50vh;
      .forget-form-container__content-captcha {
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 10px;

        .forget-form-container__content-captcha-img {
          cursor: pointer;
          border-radius: 3px;
        }
      }

      .forget-form-container__content-submitbutton--full {
        width: 100%;
        border-radius: 0
      }

      .forget-form-container__content-forgetbutton--text {
        display: flex;
        justify-content: end;
      }

    }

    .forget-form-container__footer {
      height: 10vh;
      display: flex;
      justify-content: center;

    }
  }

}


</style>
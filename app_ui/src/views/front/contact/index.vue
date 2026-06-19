<script setup >
import { onMounted, reactive, ref } from 'vue'
import {
  Check,
  CircleCloseFilled,
  Delete,
  Download,
  Edit,
  InfoFilled,
  Plus,
  Refresh,
  Search,
  Upload
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import {ElMessage, ElMessageBox} from 'element-plus'

import downloadExcel from '@/utils/downloads'

import 'md-editor-v3/lib/style.css';
import {useRoute} from "vue-router";
import router from '@/router'
const $router = router
const $route = useRoute()
// 弹框头部名称
const headerTitle = ref()
// 当前页数
const pageNum = ref(1)
// 每页展示量
const pageSize = ref(10)
// 分页数据总数
const total = ref(0)
// 表格数据
const tableData = ref([])
// 批量选择的数组
const multipleSelection = ref([])
// 是否禁用按钮
const disabled = ref(true)
// 是否展示弹框
const dialogVisible = ref(false)
// 定义关联列表
//定义查询值
//   nameList
const  name=ref('')
//   statuList
const  statu=ref('')
// 表单数据定义
const form = reactive({
  id: undefined,
  name: undefined,
  phone: undefined,
  email: undefined,
  des: undefined,
  statu:'未联系',
})
// 遍历多图片上传






// 表单样式
const formSize = ref('default')
// 表单ref标识数据
const ruleFormRef = ref()
// 自定义校验规则

// 表单校验规则
const rules = reactive({

  name: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

  phone: [
    {required: true, message: '请输入手机号码', trigger: 'blur'},
    {
      pattern: /^1[3456789]\d{9}$/,
      message: '手机号格式不正确',
      trigger: 'blur',
    },
  ],
  email:
    [
      {required: true, message: '请输入邮箱号', trigger: 'blur'},
      {
        type: 'email',
        message: '邮箱地址格式不正确',
        trigger: ['blur'],
      },
    ],
  des: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

})

//新增方法
const handleAdd = async () => {
  headerTitle.value = reactive('新增数据')
  dialogVisible.value = true
}
// 修改方法
const handleUpdate = async (id) => {
  const entity={}
  const res = await request.get(`/contact/${id}`)
  Object.assign(entity, res.data)
  entity.statu='已联系'
  await request.put(`/contact/${id}`, entity)

  ElMessage({
    showClose: true,
    message: '联系成功',
    type: 'success'
  })
  await load()



}
// 单个删除方法
const handleDel = async (id) => {
  await request.delete(`/contact/${id}`)
  ElMessage({
    showClose: true,
    message: '删除成功',
    type: 'success'
  })
  await load()
}
// 批量删除方法
const handleBatchDel = async () => {
  const ids = []
  multipleSelection.value.forEach((row) => {
    ids.push(row.id)
  })
  await request.delete(`/contact/batch/${ids}`)
  ElMessage({
    showClose: true,
    message: '批量删除成功',
    type: 'success'
  })
  await load()
}

// 分页查询方法（初始化方法，页面加载成功以后就调用的方法）
const load = async () => {
  const res = await request.get('/contact/page', {
    params: {

      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name:name.value,
      statu:statu.value,
    }
  })
  pageNum.value = res.data.current
  pageSize.value = res.data.size
  total.value = res.data.total
  tableData.value = res.data.records
}
// 加载页面初始化调用load方法
load()

// 清空查询数据重置方法
const handleReset = () => {
  name.value=''
  statu.value=''
  load()
}
// 修改每页展示的数据量方法
const handleSizeChange = (size) => {
  pageSize.value = size
  load()
}
// 翻页方法
const handleCurrentChange = (current) => {
  pageNum.value = current
  load()
}
// 多选按钮处理方法
const handleSelectionChange = (val) => {
  multipleSelection.value = val
  disabled.value = val.length === 0
}

// 关闭弹框提示方法
const handleClose = (done) => {
  ElMessageBox.confirm('确定关闭窗口?')
    .then(() => {
      handleResetForm(ruleFormRef.value)
    })
    .then(() => {
      done()
    })
    .catch(() => {
      // catch error
    })
}


// 提交表单校验方法
const handleSubmitForm = async (formEl) => {
  if (!formEl) return




  await formEl.validate(async (valid, fields) => {
    if (valid) {
      await request({
        method: form.id ? 'put' : 'post',
        url: form.id ? `/contact/${form.id}` : '/contact',
        data: form
      })
      ElMessage({
        showClose: true,
        message: '提交成功，会有客服尽快跟您联系！',
        type: 'success'
      })
      await handleResetForm(formEl)
    } else {
      console.log('error submit!', fields)
    }
  })
}
// 批量导入读数据写到后端数据库中
const beforeBatchUpload = async (file) => {
  let fd = new FormData()
  fd.append('file', file)
  await request.post('/contact/batch/upload', fd, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  ElMessage({
    showClose: true,
    message: '上传成功',
    type: 'success'
  })
  await load()
}
// 批量导出方法
const handleBatchExport = async () => {
  const ids = multipleSelection.value.map((row) => row.id)
  const res = await request(
    {
      url: `/contact/batch/export/${ids}`,
      method: 'get',
      responseType: 'blob'
    } //在请求中加上这一行，特别重要
  )
  downloadExcel(res, '导出数据表')
}
// 取消弹框方法
const handleResetForm = async (formEl) => {
  if (!formEl) return
  formEl.resetFields()

  Object.assign(form,
    {
      id: undefined,
      name: undefined,
      phone: undefined,
      email: undefined,
      des: undefined,
      statu:'未联系',
    }
  )
  // formEl.clearValidate("img")
  dialogVisible.value = false
  await load()
}

// 文件上传方法

// 文件下载
const dowload = async (url) => {
  window.open(url)
}
// 富文本文件上传
const onUploadImg = async (files, callback) => {
  let i = 0;
  const res = await Promise.all(
    files.map((file) => {

      if (i > 0) {

        return false
      }
      return new Promise((rev, rej) => {
        const formdata = new FormData();
        formdata.append('file', file);

        request
          .post('/file/upload', formdata, {
            headers: {
              'Content-Type': 'multipart/form-data',
            }
          })
          .then((res) => {
            i++
            rev(res.data)
          })
          .catch((error) => rej(error));
      });

    })
  );
  callback(res);
}

onMounted(() => {
  if (!window.BMap) {
    const script = document.createElement('script');
    script.src = 'https://api.map.baidu.com/api?v=3.0&ak=42Tk3LYkoAQgnrTHUrNNJ2URO4p6i2Fc&callback=initMap';
    document.body.appendChild(script);

    window.initMap = () => {
      const map = new BMap.Map('map');
      const point = new BMap.Point(113.975464,23.062664);
      map.centerAndZoom(point, 16);
      const marker = new BMap.Marker(point);
      map.addOverlay(marker);
      marker.disableDragging();
      map.enableScrollWheelZoom(true);
      // 禁止地图拖动
      map.disableDragging();
      // 禁止手势缩放（触控设备）
      map.disablePinchToZoom();
      // 禁止滚轮缩放
      map.disableScrollWheelZoom();
    };
  } else {
    const map = new BMap.Map('map');
    const point = new BMap.Point(121.395788, 31.372937);
    map.centerAndZoom(point, 16);
    const marker = new BMap.Marker(point);
    map.addOverlay(marker);
    marker.disableDragging();
    map.enableScrollWheelZoom(true);
    // 禁止地图拖动
    map.disableDragging();
    // 禁止手势缩放（触控设备）
    map.disablePinchToZoom();
    // 禁止滚轮缩放
    map.disableScrollWheelZoom();
  }
});
</script>
<template>


  <div class="bg-gray-50 min-h-[calc(100vh-64px)] dark:bg-gray-900 flex items-center justify-center">
    <div class="mx-auto max-w-7xl px-6 lg:px-8 py-5 sm:py-5">
      <!-- 标题 -->
      <div class="text-center lg:text-left mb-10">

        <p class="mt-4 text-lg text-gray-600 dark:text-gray-400">
          我们的总部位于广东省东莞市，专注为客户提供专业服务。欢迎联系我们，了解更多信息。
        </p>
      </div>

      <!-- 左右布局 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-10">

        <!-- 左边表单 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow p-8">
          <el-form
            ref="ruleFormRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            class="ruleForm"
            :size="formSize"
            status-icon
          >
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name"/>
            </el-form-item>

            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone"/>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email"/>
            </el-form-item>

            <el-form-item label="联系事由" prop="des">
              <el-input v-model="form.des" type="textarea"/>
            </el-form-item>

            <el-form-item>
              <el-button class="w-full" type="primary" @click="handleSubmitForm(ruleFormRef)">提交</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 右边展示 -->
        <div class="flex flex-col gap-6">
          <!-- 联系方式卡片 -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <div class="bg-white dark:bg-gray-800 rounded-xl shadow p-6 border-l-4 border-indigo-600">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">地址</h3>
              <p class="text-gray-900 cursor-pointer">
                广东省东莞市

              </p>
              <p class="text-gray-900 cursor-pointer" >
                松山湖园区东园大道松山湖段2号
              </p>
            </div>

            <div class="bg-white dark:bg-gray-800 rounded-xl shadow p-6 border-l-4 border-indigo-600">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">电话邮箱</h3>
              <p class="text-gray-900 cursor-pointer" >+86 18344161980 </p>
              <p class="text-gray-900 cursor-pointer" >2924195934@qq.com</p>
            </div>
          </div>

          <!-- 地图 + 微信二维码 -->
          <div class="mt-2 flex flex-col lg:flex-row gap-4">
            <!-- 地图 -->
            <div class="flex-1 rounded-xl overflow-hidden shadow-lg">
              <div id="map" class="w-full h-full"></div>

            </div>

            <!-- 微信二维码 -->
            <div
              class="w-48 rounded-xl overflow-hidden shadow-lg bg-white flex flex-col items-center justify-center p-3"
            >
              <img
                src="/wx.jpg"
                alt="在线客服微信二维码"
                class="w-32 h-auto object-cover rounded-lg"
              />
              <p class="text-center text-gray-700 text-sm mt-2">在线客服微信</p>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>

</template>

<style scoped lang="scss">

</style>

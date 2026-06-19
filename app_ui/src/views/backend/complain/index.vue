<script setup>
import { reactive, ref } from 'vue'
import {
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
import { ElMessage, ElMessageBox } from 'element-plus'

import downloadExcel from '@/utils/downloads'

import 'md-editor-v3/lib/style.css'
import { useRoute } from 'vue-router'
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
//   orderList
const orderList = ref([])
//   userList
const userList = ref([])
//   vitaeList
const vitaeList = ref([])
//定义查询值
//   orderIdList
const orderId = ref('')
//   userIdList
const userId = ref('')
//   vaIdList
const vaId = ref('')
//   statuList
const statu = ref('')
// 表单数据定义
const form = reactive({
  id: undefined,
  orderId: undefined,
  des: undefined,
  userId: undefined,
  vaId: undefined,
  statu: '未处理',
  ress: undefined,
  createTime: undefined,
  updateTime: undefined
})
// 遍历多图片上传

// 表单样式
const formSize = ref('default')
// 表单ref标识数据
const ruleFormRef = ref()
// 自定义校验规则

// 表单校验规则
const rules = reactive({
  des: [{ required: true, message: '必选项不能为空', trigger: 'blur' }],

  ress: [{ required: true, message: '必选项不能为空', trigger: 'blur' }]
})

// 身份证号脱敏
const maskIdNumber = (idnumber) => {
  if (!idnumber || idnumber.length < 10) return idnumber
  return idnumber.slice(0, 6) + '********' + idnumber.slice(-4)
}

//新增方法
const handleAdd = async () => {
  headerTitle.value = reactive('新增数据')
  dialogVisible.value = true
}
// 修改方法
const handleUpdate = async (id) => {
  dialogVisible.value = true
  headerTitle.value = reactive('编辑数据')
  const res = await request.get(`/complain/${id}`)
  Object.assign(form, res.data)
}
// 单个删除方法
const handleDel = async (id) => {
  await request.delete(`/complain/${id}`)
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
  await request.delete(`/complain/batch/${ids}`)
  ElMessage({
    showClose: true,
    message: '批量删除成功',
    type: 'success'
  })
  await load()
}

// 分页查询方法（初始化方法，页面加载成功以后就调用的方法）
const load = async () => {
  const res = await request.get('/complain/page', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      orderId: orderId.value,
      userId: userId.value,
      vaId: vaId.value,
      statu: statu.value
    }
  })
  pageNum.value = res.data.current
  pageSize.value = res.data.size
  total.value = res.data.total
  tableData.value = res.data.records
  //   orderList
  const resorder = await request.get(`/order`)
  orderList.value = resorder.data
  //   userList
  const resuser = await request.get(`/user`)
  userList.value = resuser.data
  //   vitaeList
  const resvitae = await request.get(`/vitae`)
  vitaeList.value = resvitae.data
}
// 加载页面初始化调用load方法
load()

// 清空查询数据重置方法
const handleReset = () => {
  orderId.value = ''
  userId.value = ''
  vaId.value = ''
  statu.value = ''
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
      form.statu='已处理'
      await request({
        method: form.id ? 'put' : 'post',
        url: form.id ? `/complain/${form.id}` : '/complain',
        data: form
      })
      ElMessage({
        showClose: true,
        message: '操作成功',
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
  await request.post('/complain/batch/upload', fd, {
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
      url: `/complain/batch/export/${ids}`,
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

  Object.assign(form, {
    id: undefined,
    orderId: undefined,
    des: undefined,
    userId: undefined,
    vaId: undefined,
    statu: '未处理',
    ress: undefined,
    createTime: undefined,
    updateTime: undefined
  })
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
  let i = 0
  const res = await Promise.all(
    files.map((file) => {
      if (i > 0) {
        return false
      }
      return new Promise((rev, rej) => {
        const formdata = new FormData()
        formdata.append('file', file)

        request
          .post('/file/upload', formdata, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          })
          .then((res) => {
            i++
            rev(res.data)
          })
          .catch((error) => rej(error))
      })
    })
  )
  callback(res)
}
</script>
<template>
  <!--  编辑弹框-->
  <el-drawer
    :modelValue="dialogVisible"
    :title="headerTitle"
    :before-close="handleClose"
    append-to-body
    size="40%"
  >
    <el-form
      ref="ruleFormRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="ruleForm"
      :size="formSize"
      status-icon
    >
      <el-form-item label="情况描述" prop="des">
        <el-input v-model="form.des" disabled />
      </el-form-item>

      <el-form-item label="处理结果" prop="ress">
        <el-input v-model="form.ress" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="el-drawer__footer-container--line">
        <el-button @click="handleResetForm(ruleFormRef)">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm(ruleFormRef)"> 提交</el-button>
      </div>
    </template>
  </el-drawer>

  <el-row class="page">
    <!--  分页查询表单按钮-->
    <el-col>
      <el-form :inline="true">
        <!--        查询输入框-->
        <el-row>
          <el-col>
            <el-form-item label-width="5px">
              <el-select v-model="orderId" placeholder="请选择订单" filterable @change="load">
                <el-option
                  v-for="(item, index) in orderList"
                  :label="item.ordernum"
                  :value="item.id"
                  :key="index.toString()"
                ></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label-width="5px">
              <el-select v-model="userId" filterable placeholder="请选择创建人" @change="load">
                <el-option
                  v-for="(item, index) in userList"
                  :label="item.nickname + '-' + item.phone"
                  :value="item.id"
                  :key="index.toString()"
                ></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label-width="5px">
              <el-select v-model="vaId" filterable placeholder="请选择陪诊师信息" @change="load">
                <el-option
                  v-for="(item, index) in vitaeList"
                  :label="item.name + '-' + item.phone"
                  :value="item.id"
                  :key="index.toString()"
                ></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label-width="5px">
              <!--                          <el-input v-model="statu" placeholder="输入当前状态"/>-->
              <el-radio-group v-model="statu">
                <el-radio-button label="未处理"></el-radio-button>
                <el-radio-button label="已处理"></el-radio-button>
              </el-radio-group>
            </el-form-item>

            <!--        查询按钮 and 重置按钮-->
            <el-form-item label-width="5px">
              <el-button type="primary" :icon="Search" @click="load">查询</el-button>
              <el-button type="warning" :icon="Refresh" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-col>
    <!-- 新增和批量按钮 -->
    <el-col class="page-operate--layout">
      <!--   新增   -->
      <div v-permission="'sys:complain:add'">
        <el-button type="primary" :icon="Plus" @click="handleAdd" size="small" plain
          >新增
        </el-button>
      </div>
      <!--   批量导入   -->
      <div v-permission="'sys:complain:batch:upload'">
        <el-upload action="" :before-upload="beforeBatchUpload" :show-file-list="true">
          <el-button type="success" :icon="Upload" size="small" plain>批量导入</el-button>
        </el-upload>
      </div>
      <!--   批量删除   -->
      <div v-permission="'sys:complain:batch:delete'">
        <el-popconfirm
          confirm-button-text="确定"
          cancel-button-text="取消"
          :icon="InfoFilled"
          icon-color="#626AEF"
          title="确认要批量删除吗？"
          @confirm="handleBatchDel"
          @cancel="load"
        >
          <template #reference>
            <el-button type="danger" :disabled="disabled" :icon="Delete" size="small" plain
              >批量删除
            </el-button>
          </template>
        </el-popconfirm>
      </div>

      <div v-permission="'sys:complain:batch:export'">
        <el-button
          type="warning"
          :icon="Download"
          :disabled="disabled"
          size="small"
          @click="handleBatchExport"
          plain
          >批量导出
        </el-button>
      </div>
    </el-col>
    <!--  表格页面-->
    <el-col class="page-table">
      <el-table
        :data="tableData"
        :header-cell-style="{ background: '#eef1f6', color: '#606266' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" align="center" width="70" />
        <el-table-column prop="orderId" label="订单编号" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.order">{{ scope.row.order.ordernum }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="des" label="情况描述" show-overflow-tooltip />
        <el-table-column prop="userId" label="创建人" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.user"
              >{{ scope.row.user.nickname }}-{{ scope.row.user.phone }}</span
            >
          </template>
        </el-table-column>
        <el-table-column prop="vaId" label="陪诊师" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.vitae">{{ scope.row.vitae.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="vaId" label="电话" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.vitae">{{ scope.row.vitae.phone }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="vaId" label="身份证号" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.vitae">{{ maskIdNumber(scope.row.vitae.idnumber) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="statu" label="当前状态" show-overflow-tooltip >
          <template #default="scope">
            <el-tag>{{scope.row.statu}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ress" label="处理结果" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="修改时间" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="240">
          <template #default="scope">
            <div class="page-table-editout--layout">
              <div v-permission="'sys:complain:update'">
                <el-button
                  type="primary"
                  :icon="Edit"
                  size="small"
                  @click="handleUpdate(scope.row.id)"
                  :disabled="scope.row.statu==='已处理'"
                  >处理
                </el-button>
              </div>
              <div v-permission="'sys:complain:delete'">
                <el-popconfirm
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  :icon="InfoFilled"
                  icon-color="#626AEF"
                  title="确认要删除吗？"
                  @confirm="handleDel(scope.row.id)"
                  @cancel="load"
                >
                  <template #reference>
                    <el-button type="danger" :icon="Delete" size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <!--      分页按钮-->
      <div class="page-pagination">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[10, 20, 30, 50, 100, 500, 1000]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="Number(total)"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-col>
  </el-row>
</template>

<style scoped lang="scss"></style>

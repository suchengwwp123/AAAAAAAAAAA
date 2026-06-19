<script setup >
  import {reactive, ref} from 'vue'
  import {
    Aim,
    Check,
    CircleCloseFilled, Close,
    Delete,
    Download,
    Edit,
    InfoFilled,
    Plus,
    Refresh,
    Search,
    Upload,
    Wallet,
    CircleCheck
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
          //   userList
          const  userList=ref([])
          //   vitaeList
          const  vitaeList=ref([])
  //定义查询值
          //   userIdList
          const  userId=ref('')
          //   resumeIdList
          const  resumeId=ref('')
          //   ordernumList
          const  ordernum=ref('')
          //   statuList
          const  statu=ref('')
  // 表单数据定义
  const form = reactive({
                  id: undefined,
                  userId: undefined,
                  resumeId: undefined,
                  address: undefined,
                  time: undefined,
                  workerId: undefined,
                  statu:'未接单',
                  des: undefined,
                  stime: undefined,
                  etime: undefined,
                  total: undefined,
                  ordernum: undefined,
                  alinum: undefined,
                  rate: undefined,
                  content: undefined,
                  createTime: undefined,
  })
  // 遍历多图片上传
















  // 表单样式
  const formSize = ref('default')
  // 表单ref标识数据
  const ruleFormRef = ref()
  // 自定义校验规则

  // 表单校验规则
  const rules = reactive({

                          userId: [{required: true, message: '必选项不能为空', trigger: 'change'}],

                          resumeId: [{required: true, message: '必选项不能为空', trigger: 'change'}],

                          address: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

                      time: [{required: true, message: '必选项不能为空', trigger: 'change'}],
                          workerId: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

                          statu: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

                          total: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

                      rate: [{required: true, message: '必选项不能为空', trigger: 'change'}],
                          content: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

  })

  //新增方法
  const handleAdd = async () => {
    headerTitle.value = reactive('新增数据')
    dialogVisible.value = true
  }
  // 修改方法
  const handleUpdate = async (id) => {
    dialogVisible.value = true
    headerTitle.value = reactive('编辑数据')
    const res = await request.get(`/order/${id}`)
    Object.assign(form, res.data)
















  }
  // 接单方法
  const handleAcceptOrder = async (id) => {
    try {
      await ElMessageBox.confirm('确定接受此订单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      })
      
      // 读取订单详细信息
      const res = await request.get(`/order/${id}`)
      // 将数据赋值给form
      Object.assign(form, res.data)
      // 修改状态为已接单
      form.statu = '已接单'
      // 提交完整的form数据
      await request.put(`/order/${id}`, form)
      
      ElMessage({
        showClose: true,
        message: '接单成功',
        type: 'success'
      })
      await load()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('接单失败：', error)
      }
    }
  }

  // 拒绝接单方法
  const handleRejectOrder = async (id) => {
    try {
      await ElMessageBox.confirm('确定拒绝此订单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      // 读取订单详细信息
      const res = await request.get(`/order/${id}`)
      // 将数据赋值给form
      Object.assign(form, res.data)
      // 修改状态为拒绝接单
      form.statu = '拒绝接单'
      // 提交完整的form数据
      await request.put(`/order/${id}`, form)
      
      ElMessage({
        showClose: true,
        message: '已拒绝接单',
        type: 'info'
      })
      await load()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('拒绝接单失败：', error)
      }
    }
  }

  // 付款方法
  const handlePayment = async (id) => {
    try {
      await ElMessageBox.confirm('确认此订单已付款？', '提示', {
        confirmButtonText: '确认付款',
        cancelButtonText: '取消',
        type: 'success'
      })
      
      // 读取订单详细信息
      const res = await request.get(`/order/${id}`)
      // 将数据赋值给form
      Object.assign(form, res.data)
      // 修改状态为已支付
      form.statu = '已支付'
      // 提交完整的form数据
      await request.put(`/order/${id}`, form)
      
      ElMessage({
        showClose: true,
        message: '付款确认成功',
        type: 'success'
      })
      await load()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('付款确认失败：', error)
      }
    }
  }

  // 开始服务方法
  const handleStartService = async (id) => {
    try {
      await ElMessageBox.confirm('确认开始陪诊服务？', '提示', {
        confirmButtonText: '开始服务',
        cancelButtonText: '取消',
        type: 'success'
      })
      
      // 读取订单详细信息
      const res = await request.get(`/order/${id}`)
      // 将数据赋值给form
      Object.assign(form, res.data)
      // 设置开始时间为当前时间
      const now = new Date()
      form.stime = now.toLocaleString('zh-CN', { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit', 
        hour: '2-digit', 
        minute: '2-digit', 
        second: '2-digit',
        hour12: false 
      }).replace(/\//g, '-')
      // 修改状态为进行中
      form.statu = '进行中'
      // 提交完整的form数据
      await request.put(`/order/${id}`, form)
      
      ElMessage({
        showClose: true,
        message: '服务已开始',
        type: 'success'
      })
      await load()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('开始服务失败：', error)
      }
    }
  }

  // 结束服务方法
  const handleEndService = async (id) => {
    try {
      const { value: totalCost } = await ElMessageBox.prompt('请输入实际费用（元）', '结束服务', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^\d+(\.\d{1,2})?$/,
        inputErrorMessage: '请输入正确的金额格式',
        inputPlaceholder: '请输入实际费用'
      })
      
      // 读取订单详细信息
      const res = await request.get(`/order/${id}`)
      // 将数据赋值给form
      Object.assign(form, res.data)
      // 设置结束时间为当前时间
      const now = new Date()
      form.etime = now.toLocaleString('zh-CN', { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit', 
        hour: '2-digit', 
        minute: '2-digit', 
        second: '2-digit',
        hour12: false 
      }).replace(/\//g, '-')
      // 设置实际费用
      form.total = parseFloat(totalCost)
      // 修改状态为已完成
      form.statu = '已完成'
      // 提交完整的form数据
      await request.put(`/order/${id}`, form)
      
      ElMessage({
        showClose: true,
        message: '服务已完成',
        type: 'success'
      })
      await load()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('结束服务失败：', error)
      }
    }
  }

  // 单个删除方法
  const handleDel = async (id) => {
    await request.delete(`/order/${id}`)
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
    await request.delete(`/order/batch/${ids}`)
    ElMessage({
      showClose: true,
      message: '批量删除成功',
      type: 'success'
    })
    await load()
  }

  // 分页查询方法（初始化方法，页面加载成功以后就调用的方法）
  const load = async () => {
    const res = await request.get('/order/page', {
      params: {

        pageNum: pageNum.value,
        pageSize: pageSize.value,
    userId:userId.value,
    resumeId:resumeId.value,
    ordernum:ordernum.value,
    statu:statu.value,
  }
  })
    pageNum.value = res.data.current
    pageSize.value = res.data.size
    total.value = res.data.total
    tableData.value = res.data.records
    //   userList
    const resuser = await request.get(`/user`)
    userList.value =resuser.data
    //   vitaeList
    const resvitae = await request.get(`/vitae`)
    vitaeList.value =resvitae.data
  }
  // 加载页面初始化调用load方法
  load()

  // 清空查询数据重置方法
  const handleReset = () => {
        userId.value=''
        resumeId.value=''
        ordernum.value=''
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
          url: form.id ? `/order/${form.id}` : '/order',
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
    await request.post('/order/batch/upload', fd, {
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
          url: `/order/batch/export/${ids}`,
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
                        userId: undefined,
                        resumeId: undefined,
                        address: undefined,
                        time: undefined,
                        workerId: undefined,
                        statu:'未接单',
                        des: undefined,
                        stime: undefined,
                        etime: undefined,
                        total: undefined,
                        ordernum: undefined,
                        alinum: undefined,
                        rate: undefined,
                        content: undefined,
                        createTime: undefined,
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

             
                      <el-form-item label="当前状态" prop="statu">
                        <el-input v-model="form.statu"/>
                      </el-form-item>


                      <el-form-item label="实际费用" prop="total">
                        <el-input v-model="form.total"/>
                      </el-form-item>

           

    </el-form>
    <template #footer>
      <div class="el-drawer__footer-container--line">
        <el-button @click="handleResetForm(ruleFormRef)">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm(ruleFormRef)"> 提交 </el-button>
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
                          <el-select v-model="userId"  placeholder="请选择下单人员" @change="load">
                            <el-option
                                v-for="(item,index) in userList"
                                :label="item.username+'-'+item.nickname"
                                :value="item.id"
                                :key="index.toString()"
                            ></el-option>
                          </el-select>
                        </el-form-item>



                        <el-form-item label-width="5px">
                          <el-select v-model="resumeId"  placeholder="请选择陪诊人员" @change="load">
                            <el-option
                                v-for="(item,index) in vitaeList"
                                :label="item.name+'-'+item.idnumber"
                                :value="item.id"
                                :key="index.toString()"
                            ></el-option>
                          </el-select>
                        </el-form-item>






                        <el-form-item label-width="5px">
                          <el-select v-model="statu" placeholder="请选择订单状态" clearable @change="load">
                            <el-option label="未接单" value="未接单" />
                            <el-option label="已接单" value="已接单" />
                            <el-option label="已支付" value="已支付" />
                            <el-option label="进行中" value="进行中" />
                            <el-option label="已完成" value="已完成" />
                            <el-option label="已评价" value="已评价" />
                            <el-option label="已取消" value="已取消" />
                            <el-option label="拒绝接单" value="拒绝接单" />
                          </el-select>
                        </el-form-item>
            <el-form-item label-width="5px">
              <el-input v-model="ordernum" placeholder="输入订单编号"/>
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
      <div v-permission="'sys:order:add'">
        <el-button type="primary" :icon="Plus" @click="handleAdd" size="small" plain
        >新增
        </el-button>
      </div>
      <!--   批量导入   -->
      <div v-permission="'sys:order:batch:upload'">
        <el-upload action="" :before-upload="beforeBatchUpload" :show-file-list="true">
          <el-button type="success" :icon="Upload" size="small" plain>批量导入</el-button>
        </el-upload>
      </div>
      <!--   批量删除   -->
      <div v-permission="'sys:order:batch:delete'">
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

      <div v-permission="'sys:order:batch:export'">
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
    <el-col
        class="page-table"
    >
      <el-table
          :data="tableData"
          :header-cell-style="{ background: '#eef1f6', color: '#606266' }"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="expand">
          <template #default="scope">
            <div style="padding: 20px;">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="订单编号">{{ scope.row.ordernum || '-' }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ scope.row.createTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="下单人员">
                  {{ scope.row.user.username}}- {{ scope.row.user.nickname}}-{{ scope.row.user.phone}}
                </el-descriptions-item>
                <el-descriptions-item label="陪诊人员">
                  {{ scope.row.vitae.name }}-     {{ scope.row.vitae.idnumber }}-   {{ scope.row.vitae.phone}}
                </el-descriptions-item>
                <el-descriptions-item label="预约地址">
                  {{ scope.row.address || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="预约时间">{{ scope.row.time || '-' }}</el-descriptions-item>
                <el-descriptions-item label="当前状态">
                  <el-tag :type="scope.row.statu === '已完成' ? 'success' : scope.row.statu === '进行中' ? 'warning' : 'info'">
                    {{ scope.row.statu }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="陪诊师账号">{{ scope.row.worker.username || '' }}</el-descriptions-item>

                <el-descriptions-item label="开始时间">{{ scope.row.stime || '' }}</el-descriptions-item>
                <el-descriptions-item label="结束时间">{{ scope.row.etime || '' }}</el-descriptions-item>

                <el-descriptions-item label="评分">
                  <el-rate v-model="scope.row.rate" disabled show-score v-if="scope.row.rate"/>
                  <span v-else>-</span>
                </el-descriptions-item>
                <el-descriptions-item label="评价信息">
                  {{ scope.row.content || '' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        <el-table-column type="selection" width="55"/>
        <el-table-column type="index" label="序号" align="center" width="70"/>
        <el-table-column prop="ordernum" label="订单编号" show-overflow-tooltip width="180"/>
        <el-table-column prop="userId" label="下单人员" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.user">{{ scope.row.user.username }}-{{ scope.row.user.nickname }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="resumeId" label="陪诊师" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.vitae">{{ scope.row.vitae.name }}-{{ scope.row.vitae.idnumber}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="des" label="用户需求" show-overflow-tooltip>

        </el-table-column>
        <el-table-column prop="time" label="预约时间" show-overflow-tooltip width="160"/>
        <el-table-column prop="statu" label="当前状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.statu === '已完成' ? 'success' : scope.row.statu === '进行中' ? 'warning' : 'info'" size="small">
              {{ scope.row.statu }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="实际费用" align="right" width="100">
          <template #default="scope">
            {{ scope.row.total ? '¥' + scope.row.total : '' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <div style="display: flex; gap: 8px; flex-wrap: wrap;"         v-permission="'sys:order:update'">
              <!-- 未接单状态显示接单和拒绝按钮 -->
              <template v-if="scope.row.statu === '未接单'">
                <el-button
                    type="success"
                    size="small"
                    :icon="Check"
                    @click="handleAcceptOrder(scope.row.id)"
                >接单
                </el-button>
                <el-button
                    type="warning"
                    size="small"
                    plain
                    :icon="Close"
                    @click="handleRejectOrder(scope.row.id)"
                >拒绝
                </el-button>
              </template>
              
              <!-- 已接单状态显示付款按钮 -->
           
              
              <!-- 已支付状态显示服务按钮 -->
              <el-button
              v-permission="'sys:order:update'"
                  v-if="scope.row.statu === '已接单'"
                  type="primary"
                  size="small"
                  :icon="Aim"
                  @click="handleStartService(scope.row.id)"
              >服务
              </el-button>
              
              <!-- 进行中状态显示结束按钮 -->
              <el-button
              v-permission="'sys:order:update'"
                  v-if="scope.row.statu === '进行中'"
                  type="warning"
                  size="small"
                  :icon="CircleCheck"
                  @click="handleEndService(scope.row.id)"
              >结束
              </el-button>
              
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
                  <el-button      v-permission="'sys:order:delete'" type="danger" :icon="Delete" size="small">删除</el-button>
                </template>
              </el-popconfirm>
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

<style scoped lang="scss">

</style>

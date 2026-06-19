<script setup >
  import {reactive, ref} from 'vue'
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
  // 表单数据定义
  const form = reactive({
                  id: undefined,
                  name: undefined,
                  img: undefined,
                  des: undefined,
                  url: undefined,
                  createTime: undefined,
                  updateTime: undefined,
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

                      img: [{required: true, message: '必选项不能为空', trigger: 'change'}],
                          des: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

                          url: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

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
    const res = await request.get(`/banner/${id}`)
    Object.assign(form, res.data)







  }
  // 单个删除方法
  const handleDel = async (id) => {
    await request.delete(`/banner/${id}`)
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
    await request.delete(`/banner/batch/${ids}`)
    ElMessage({
      showClose: true,
      message: '批量删除成功',
      type: 'success'
    })
    await load()
  }

  // 分页查询方法（初始化方法，页面加载成功以后就调用的方法）
  const load = async () => {
    const res = await request.get('/banner/page', {
      params: {

        pageNum: pageNum.value,
        pageSize: pageSize.value,
    name:name.value,
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
          url: form.id ? `/banner/${form.id}` : '/banner',
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
    await request.post('/banner/batch/upload', fd, {
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
          url: `/banner/batch/export/${ids}`,
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
                        img: undefined,
                        des: undefined,
                        url: undefined,
                        createTime: undefined,
                        updateTime: undefined,
        }
    )
    // formEl.clearValidate("img")
    dialogVisible.value = false
    await load()
  }

  // 文件上传方法
          const uploadimg= async (file) => {
            let fd = new FormData()
            fd.append('file', file)
            const res = await request.post('/file/upload', fd, {
              headers: {
                'Content-Type': 'multipart/form-data'
              }
            })
            ElMessage({
              showClose: true,
              message: '上传成功',
              type: 'success'
            })
            form.img = res.data
            ruleFormRef.value.clearValidate('img')
          }

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

                      <el-form-item label="轮播标题" prop="name">
                        <el-input v-model="form.name"/>
                      </el-form-item>

                  <el-form-item label="轮播图片" prop="img">
                    <el-upload action="" :before-upload="uploadimg" :show-file-list="true">
                      <el-button type="primary" size="small">点击上传</el-button>
                    </el-upload>
                  </el-form-item>

                      <el-form-item label="轮播描述" prop="des">
                        <el-input v-model="form.des" type="textarea"/>
                      </el-form-item>

                      <el-form-item label="访问路径" prop="url">
                        <el-input v-model="form.url" type="textarea"/>
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
                          <el-input v-model="name" placeholder="输入轮播标题"/>
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
      <div v-permission="'sys:banner:add'">
        <el-button type="primary" :icon="Plus" @click="handleAdd" size="small" plain
        >新增
        </el-button>
      </div>
      <!--   批量导入   -->
      <div v-permission="'sys:banner:batch:upload'">
        <el-upload action="" :before-upload="beforeBatchUpload" :show-file-list="true">
          <el-button type="success" :icon="Upload" size="small" plain>批量导入</el-button>
        </el-upload>
      </div>
      <!--   批量删除   -->
      <div v-permission="'sys:banner:batch:delete'">
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

      <div v-permission="'sys:banner:batch:export'">
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
        <el-table-column type="selection" width="55"/>
                <el-table-column type="index"  label="序号" align="center"  width="70"  />
                <el-table-column prop="name" label="轮播标题"  show-overflow-tooltip/>
                <el-table-column prop="img" label="轮播图片" align="center">
                  <template #default="scope">
                    <el-image
                        style="width: 50px; height: 50px"
                        :src="scope.row.img"
                        :preview-src-list="[scope.row.img]"
                        :zoom-rate="1.2"
                        :max-scale="7"
                        :preview-teleported="true"
                        :min-scale="0.2"
                        :initial-index="0"
                        fit="contain"
                    />
                  </template>
                </el-table-column>
                <el-table-column prop="des" label="轮播描述"  show-overflow-tooltip/>
                <el-table-column prop="url" label="访问路径"  show-overflow-tooltip/>
                <el-table-column prop="createTime" label="创建时间"  show-overflow-tooltip/>
                <el-table-column prop="updateTime" label="修改时间"  show-overflow-tooltip/>
        <el-table-column label="操作" align="center" width="240">
          <template #default="scope">
            <div class="page-table-editout--layout">
              <div v-permission="'sys:banner:update'">
                <el-button
                    type="primary"
                    :icon="Edit"
                    size="small"
                    @click="handleUpdate(scope.row.id)"
                >编辑
                </el-button>
              </div>
              <div v-permission="'sys:banner:delete'">
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

<style scoped lang="scss">

</style>

<script setup >
import { onBeforeUnmount, reactive, ref, shallowRef } from 'vue'
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
  import { areaList } from '@vant/area-data'
  
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
  //定义查询值
          //   nameList
          const  name=ref('')
          //   userIdList
          const  userId=ref('')
          //   addressList
          const  address=ref('')
  // 表单数据定义
  const form = reactive({
                  id: undefined,
                  name: undefined,
                  sex: undefined,
                  phone: undefined,
                  img: undefined,
                  file: undefined,
                  des: undefined,
                  content: undefined,
                  userId: undefined,
                  address: undefined,
                  idnumber: undefined,
                  price: undefined,
  })
  // 遍历多图片上传

  // 区域数据处理函数
  function buildOptions(province_list, city_list, county_list) {
    const provinces = Object.entries(province_list).map(([pCode, pName]) => {
      const province = {
        value: pCode,
        label: pName,
        children: []
      }

      Object.entries(city_list).forEach(([cCode, cName]) => {
        if (cCode.slice(0, 2) === pCode.slice(0, 2)) {
          const city = {
            value: cCode,
            label: cName,
            children: []
          }

          Object.entries(county_list).forEach(([coCode, coName]) => {
            if (coCode.slice(0, 4) === cCode.slice(0, 4)) {
              city.children.push({
                value: coCode,
                label: coName
              })
            }
          })

          province.children.push(city)
        }
      })

      return province
    })

    return provinces
  }

  function formatRegion(codes) {
    if (!codes) return ''

    // 如果传进来是字符串，先转数组
    const arr = Array.isArray(codes) ? codes : String(codes).split(',')

    return arr
      .map(
        (code) =>
          areaList.province_list[code] ||
          areaList.city_list[code] ||
          areaList.county_list[code] ||
          code
      )
      .join(' / ')
  }

  const options = buildOptions(areaList.province_list, areaList.city_list, areaList.county_list)

  import {Editor, Toolbar} from "@wangeditor/editor-for-vue";
  import '@wangeditor/editor/dist/css/style.css'

  const editorRef = shallowRef()
  const toolbarConfig = {}
  const editorConfig = { placeholder: '请自行添加描述...',
    MENU_CONF: {}
  }
  editorConfig.MENU_CONF['uploadImage'] = {
    server: 'http://localhost:9090/api/file/wangupload',
    fieldName: 'file'
    // 继续写其他配置...
    //【注意】不需要修改的不用写，wangEditor 会去 merge 当前其他配置
  }
  onBeforeUnmount(() => {
    const editor = editorRef.value
    if (editor == null) return
    editor.destroy()
  })

  const handleCreated = async (editor) => {
    editorRef.value = editor // 记录 editor 实例，重要！

  }






  // 表单样式
  const formSize = ref('default')
  // 表单ref标识数据
  const ruleFormRef = ref()
  // 自定义校验规则

  // 表单校验规则
  const rules = reactive({

                          name: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

                          sex: [{required: true, message: '请选择性别', trigger: 'change'}],

                      phone: [
                  {required: true, message: '请输入手机号码', trigger: 'blur'},
                  {
                    pattern: /^1[3456789]\d{9}$/,
                    message: '手机号格式不正确',
                    trigger: 'blur',
                  },
                ],
                      img: [{required: true, message: '必选项不能为空', trigger: 'change'}],
                      file: [{required: true, message: '请上传资质文件', trigger: 'change'}],
                          des: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

                          content: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

                          userId: [{required: true, message: '必选项不能为空', trigger: 'change'}],

                          address: [{required: true, message: '必选项不能为空', trigger: 'blur'}],

                      idnumber: [
                  {required: true, message: '请输入身份证号', trigger: 'blur'},
                  {
                    pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
                    message: '身份证号格式不正确',
                    trigger: 'blur',
                  },
                ],

                      price: [{required: true, message: '请输入参考金额', trigger: 'blur'}],

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
    const res = await request.get(`/vitae/${id}`)
    Object.assign(form, res.data)









  }
  // 单个删除方法
  const handleDel = async (id) => {
    await request.delete(`/vitae/${id}`)
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
    await request.delete(`/vitae/batch/${ids}`)
    ElMessage({
      showClose: true,
      message: '批量删除成功',
      type: 'success'
    })
    await load()
  }
  // 分页查询方法（初始化方法，页面加载成功以后就调用的方法）
  const load = async () => {
    userId.value = $route.query.id
    if (!userId.value) {
      ElMessage({
        showClose: true,
        message: '缺少用户ID参数',
        type: 'warning'
      })
      $router.back()
      return
    }
    
    // 查询该用户的简历
    try {
    const res = await request.get('/vitae/page', {
      params: {
          userId: userId.value,
          pageNum: 1,
          pageSize: 1
        }
      })
      if (res.data && res.data.records && res.data.records.length > 0) {
        // 找到了简历，加载数据
        const vitaeData = res.data.records[0]
        
        // 将地址字符串转换为数组（如果是字符串的话）
        if (vitaeData.address && typeof vitaeData.address === 'string') {
          vitaeData.address = vitaeData.address.split(',')
        }
        
        Object.assign(form, vitaeData)
      } else {
        // 没有简历，可以新建
        console.log('未找到简历数据，可以新建')
      }
    } catch (error) {
      console.log('查询简历失败', error)
    }
  }
  
  // 加载页面初始化调用load方法
  load()


  // 清空查询数据重置方法
  const handleReset = () => {
        name.value=''
        userId.value=''
        address.value=''
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

    // 设置用户ID
    form.userId = $route.query.id

    await formEl.validate(async (valid, fields) => {
      if (valid) {
        try {
          // 将地址数组转换为字符串
          const submitData = {
            ...form,
            address: Array.isArray(form.address) ? form.address.toString() : form.address
          }
          
        await request({
          method: form.id ? 'put' : 'post',
          url: form.id ? `/vitae/${form.id}` : '/vitae',
            data: submitData
        })
        ElMessage({
          showClose: true,
            message: '保存成功',
          type: 'success'
        })
          // 提交成功后返回上一页
          setTimeout(() => {
            $router.back()
          }, 1000)
        } catch (error) {
          ElMessage({
            showClose: true,
            message: '保存失败：' + (error.message || '未知错误'),
            type: 'error'
          })
        }
      } else {
        console.log('error submit!', fields)
      }
    })
  }
  // 批量导入读数据写到后端数据库中
  const beforeBatchUpload = async (file) => {
    let fd = new FormData()
    fd.append('file', file)
    await request.post('/vitae/batch/upload', fd, {
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
          url: `/vitae/batch/export/${ids}`,
          method: 'get',
          responseType: 'blob'
        } //在请求中加上这一行，特别重要
    )
    downloadExcel(res, '导出数据表')
  }
  // 重置表单方法
  const handleResetForm = async (formEl) => {
    if (!formEl) return
    formEl.resetFields()

    Object.assign(form,
        {
                        id: undefined,
                        name: undefined,
                        sex: undefined,
                        phone: undefined,
                        img: undefined,
                        file: undefined,
                        des: undefined,
                        content: undefined,
                        userId: undefined,
                        address: undefined,
                        idnumber: undefined,
                        price: undefined,
        }
    )
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

  // 资质文件上传方法
  const uploadCertificate = async (file) => {
    let fd = new FormData()
    fd.append('file', file)
    const res = await request.post('/file/upload', fd, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    ElMessage({
      showClose: true,
      message: '资质文件上传成功',
      type: 'success'
    })
    form.file = res.data
    ruleFormRef.value.clearValidate('file')
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
  <div class="vitae-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <button class="back-btn" @click="$router.back()">
          <span class="back-icon">←</span>
          <span>返回</span>
        </button>
        <div class="header-title">
          <h1>📋 简历信息编辑</h1>
          <p>完善陪诊师的个人简历信息</p>
        </div>
      </div>
    </div>

    <!-- 表单容器 -->
    <div class="form-container">
      <div class="form-wrapper">
    <el-form
        ref="ruleFormRef"
        :model="form"
        :rules="rules"
        label-width="120px"
          class="vitae-form"
        :size="formSize"
        status-icon
    >
          <!-- 基本信息 -->
          <div class="form-section">
            <div class="section-header">
              <span class="section-icon">👤</span>
              <h3 class="section-title">基本信息</h3>
            </div>
            
            <div class="form-grid">
                      <el-form-item label="姓名" prop="name">
                <el-input 
                  v-model="form.name" 
                  placeholder="请输入姓名"
                  clearable
                />
              </el-form-item>

              <el-form-item label="性别" prop="sex">
                <el-select 
                  v-model="form.sex" 
                  placeholder="请选择性别"
                  clearable
                  class="full-width"
                >
                  <el-option label="男性" value="男性" />
                  <el-option label="女性" value="女性" />
                </el-select>
                      </el-form-item>

                  <el-form-item label="电话" prop="phone">
                <el-input 
                  v-model="form.phone" 
                  placeholder="请输入手机号"
                  clearable
                />
                  </el-form-item>

              <el-form-item label="身份证号" prop="idnumber">
                <el-input 
                  v-model="form.idnumber" 
                  placeholder="请输入身份证号"
                  clearable
                />
                      </el-form-item>

              <el-form-item label="参考金额/天" prop="price">
                <el-input-number 
                  v-model="form.price" 
                  :min="0"
                  :max="10000"
                  :step="50"
                  :precision="0"
                  placeholder="请输入参考金额"
                  class="full-width"
                />
                      </el-form-item>

                      <el-form-item label="所在区域" prop="address">
                <el-cascader
                  clearable
                  v-model="form.address"
                  :options="options"
                  :props="{ checkStrictly: false }"
                  placeholder="请选择所在区域"
                  class="full-width"
                />
              </el-form-item>
            </div>

            <!-- 显示选择的区域 -->
            <div v-if="form.address && form.address.length > 0" class="selected-region">
              <span class="region-label">已选择区域：</span>
              <span class="region-value">{{ formatRegion(form.address) }}</span>
            </div>


          </div>

          <!-- 照片和资质 -->
          <div class="form-section">
            <div class="section-header">
              <span class="section-icon">📸</span>
              <h3 class="section-title">照片与资质</h3>
            </div>

            <el-form-item label="个人照片" prop="img">
              <div class="upload-container">
                <el-upload 
                  action="" 
                  :before-upload="uploadimg" 
                  :show-file-list="false"
                  class="avatar-uploader"
                >
                  <img v-if="form.img" :src="form.img" class="avatar-preview" />
                  <div v-else class="upload-placeholder">
                    <span class="upload-icon">📷</span>
                    <span class="upload-text">点击上传照片</span>
                  </div>
                </el-upload>
                <div class="upload-tips">
                  <p>建议上传3:4证件照格式</p>
                  <p>正面免冠，白底或蓝底</p>
                  <p>支持 JPG、PNG 格式，大小不超过 2MB</p>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="资质文件" prop="file">
              <div class="certificate-upload-area">
                <el-upload 
                  action="" 
                  :before-upload="uploadCertificate" 
                  :show-file-list="false"
                  class="file-uploader"
                >
                  <el-button type="primary" :icon="Upload">
                    {{ form.file ? '重新上传' : '选择文件' }}
                  </el-button>
                </el-upload>
                
                <div v-if="form.file" class="file-info-box">
                  <div class="file-icon">📦</div>
                  <div class="file-details">
                    <p class="file-status">✅ 文件已上传</p>
                    <a :href="form.file" target="_blank" class="download-link">
                      <el-button type="success" size="small" :icon="Download">
                        下载查看文件
                      </el-button>
                    </a>
                  </div>
                </div>
                
                <div class="upload-instructions">
                  <p class="instruction-title">📋 上传说明：</p>
                  <ul class="instruction-list">
                    <li>支持上传健康证、职业资格证等资质文件</li>
                    <li>支持格式：ZIP、RAR、PDF、JPG、PNG</li>
                    <li>文件大小不超过 10MB</li>
                  </ul>
                </div>
              </div>
                      </el-form-item>

            <el-form-item label="个人描述" prop="des">
              <el-input 
                v-model="form.des" 
                type="textarea"
                :rows="4"
                placeholder="请输入个人简介或职业特长"
                maxlength="200"
                show-word-limit
              />
                      </el-form-item>
          </div>

          <!-- 简历详情 -->
          <div class="form-section">
            <div class="section-header">
              <span class="section-icon">📝</span>
              <h3 class="section-title">简历详情</h3>
            </div>

            <el-form-item label="详细内容" prop="content">
              <div class="editor-wrapper">
          <Toolbar
                  class="editor-toolbar"
            :editor="editorRef"
            :defaultConfig="toolbarConfig"
            mode="default"
          />
          <Editor
                  class="editor-content"
            v-model="form.content"
            :defaultConfig="editorConfig"
            mode="default"
            @onCreated="handleCreated"
          />
        </div>
      </el-form-item>
          </div>
    </el-form>

        <!-- 底部按钮 -->
        <div class="form-footer">
          <el-button 
            type="primary" 
            size="large"
            @click="handleSubmitForm(ruleFormRef)"
            class="submit-btn"
          >
            <span class="btn-icon">✓</span>
            <span>提交保存</span>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.vitae-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f0f4f8 0%, #ffffff 100%);
}

/* 页面头部 */
.page-header {
  background: white;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.5rem 2rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 0.75rem;
  color: #374151;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #f9fafb;
  border-color: #3b82f6;
  color: #3b82f6;
  transform: translateX(-4px);
}

.back-icon {
  font-size: 1.2rem;
}

.header-title h1 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.25rem 0;
}

.header-title p {
  font-size: 0.95rem;
  color: #6b7280;
  margin: 0;
}

/* 表单容器 */
.form-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem 2rem 2rem;
}

.form-wrapper {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  padding: 2.5rem;
}

/* 表单分区 */
.form-section {
  margin-bottom: 2.5rem;
}

.form-section:last-of-type {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f3f4f6;
}

.section-icon {
  font-size: 1.5rem;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

/* 表单网格 */
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

.full-width {
  width: 100%;
}

/* 上传容器 */
.upload-container {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.avatar-uploader {
  flex-shrink: 0;
}

.avatar-preview {
  width: 120px;
  height: 160px;
  border-radius: 0.5rem;
  object-fit: cover;
  border: 2px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-preview:hover {
  border-color: #3b82f6;
  transform: scale(1.05);
}

.upload-placeholder {
  width: 120px;
  height: 160px;
  border: 2px dashed #d1d5db;
  border-radius: 0.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f9fafb;
}

.upload-placeholder:hover {
  border-color: #3b82f6;
  background: #eff6ff;
}

.upload-icon {
  font-size: 2.5rem;
}

.upload-text {
  font-size: 0.9rem;
  color: #6b7280;
}

.upload-tips {
  flex: 1;
}

.upload-tips p {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 0.5rem 0;
  line-height: 1.6;
}

.upload-tips p:first-child {
  color: #374151;
  font-weight: 500;
}

/* 资质文件上传区域 */
.certificate-upload-area {
  width: 100%;
}

.file-uploader {
  margin-bottom: 1rem;
}

.file-info-box {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px solid #3b82f6;
  border-radius: 0.75rem;
  margin: 1rem 0;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.file-icon {
  font-size: 2.5rem;
  flex-shrink: 0;
}

.file-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.file-status {
  font-weight: 600;
  color: #059669;
  margin: 0;
  font-size: 1rem;
}

.download-link {
  text-decoration: none;
}

.upload-instructions {
  padding: 1rem;
  background: #f9fafb;
  border-radius: 0.5rem;
  border-left: 4px solid #6b7280;
  margin-top: 1rem;
}

.instruction-title {
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.75rem 0;
  font-size: 0.95rem;
}

.instruction-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.instruction-list li {
  color: #6b7280;
  font-size: 0.875rem;
  line-height: 1.8;
  padding-left: 1.25rem;
  position: relative;
}

.instruction-list li:before {
  content: "•";
  position: absolute;
  left: 0.5rem;
  color: #9ca3af;
  font-weight: bold;
}

/* 富文本编辑器 */
.editor-wrapper {
  border: 1px solid #d1d5db;
  border-radius: 0.75rem;
  overflow: hidden;
  transition: border-color 0.3s ease;
}

.editor-wrapper:hover {
  border-color: #3b82f6;
}

.editor-toolbar {
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.editor-content {
  height: 600px;
  overflow-y: auto;
  min-height: 500px;
}

/* 底部按钮 */
.form-footer {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 2px solid #f3f4f6;
}

.form-footer .el-button {
  min-width: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-icon {
  font-size: 1.1rem;
}

.submit-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

/* 表单项样式优化 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}

:deep(.el-input__inner) {
  border-radius: 0.5rem;
}

:deep(.el-textarea__inner) {
  border-radius: 0.5rem;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-cascader) {
  width: 100%;
}

:deep(.el-input-number) {
  width: 100%;
}

/* 选中区域显示 */
.selected-region {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  background: #eff6ff;
  border-left: 4px solid #3b82f6;
  border-radius: 0.5rem;
  
  .region-label {
    font-weight: 600;
    color: #374151;
    margin-right: 0.5rem;
  }
  
  .region-value {
    color: #3b82f6;
    font-weight: 500;
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .header-content {
    padding: 1rem;
  }

  .header-title h1 {
    font-size: 1.25rem;
  }

  .form-container {
    padding: 1rem;
  }

  .form-wrapper {
    padding: 1.5rem;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .upload-container {
    flex-direction: column;
    gap: 1rem;
  }

  .form-footer {
    flex-direction: column;
    gap: 1rem;
  }

  .form-footer .el-button {
    width: 100%;
  }
}
</style>

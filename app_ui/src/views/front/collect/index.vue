<script setup>
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
//   articleList
const articleList = ref([])
//   userList
const userList = ref([])
//定义查询值
//   articleIdList
const articleId = ref('')
//   userIdList
const userId = ref('')
// 表单数据定义
const form = reactive({
  id: undefined,
  articleId: undefined,
  userId: undefined,
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

  articleId: [{required: true, message: '必选项不能为空', trigger: 'change'}],

  userId: [{required: true, message: '必选项不能为空', trigger: 'change'}],

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
  const res = await request.get(`/collect/${id}`)
  Object.assign(form, res.data)


}
// 单个删除方法
const handleDel = async (id) => {
  await request.delete(`/collect/${id}`)
  ElMessage({
    showClose: true,
    message: '取消成功',
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
  await request.delete(`/collect/batch/${ids}`)
  ElMessage({
    showClose: true,
    message: '批量删除成功',
    type: 'success'
  })
  await load()
}

// 分页查询方法（初始化方法，页面加载成功以后就调用的方法）
const load = async () => {
  const res = await request.get('/collect/page', {
    params: {

      pageNum: pageNum.value,
      pageSize: pageSize.value,
      articleId: articleId.value,
      userId: userId.value,
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
  articleId.value = ''
  userId.value = ''
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
        url: form.id ? `/collect/${form.id}` : '/collect',
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
  await request.post('/collect/batch/upload', fd, {
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
        url: `/collect/batch/export/${ids}`,
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
        articleId: undefined,
        userId: undefined,
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
  <div class="collect-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">💖 我的收藏</h1>
        <p class="page-subtitle">共收藏了 {{ total }} 篇文章</p>
      </div>

      <!-- 文章列表 -->
      <div v-if="tableData.length > 0" class="article-grid">
        <div
          v-for="item in tableData"
          :key="item.id"
          class="article-card"
        >
          <!-- 文章封面 -->
          <div class="card-cover" @click="$router.push(`/front/detail?id=${item.articleId}`)">
            <img :src="item.article.img" :alt="item.article.name" />
            <div class="cover-overlay">
              <span class="view-text">查看详情</span>
            </div>
          </div>

          <!-- 文章信息 -->
          <div class="card-body">
            <h3 class="article-title" :title="item.article.name">
              {{ item.article.name }}
            </h3>
            <p class="article-desc" :title="item.article.des">
              {{ item.article.des }}
            </p>
            
            <!-- 收藏时间 -->
            <div class="collect-time">
              <span class="time-icon">🕒</span>
              <span class="time-text">{{ item.createTime }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="card-footer">
            <button 
              class="btn btn-primary"
              @click="$router.push(`/front/detail?id=${item.articleId}`)"
            >
              <span class="btn-icon">📖</span>
              <span>阅读</span>
            </button>
            <button 
              class="btn btn-danger"
              @click="handleDel(item.id)"
            >
              <span class="btn-icon">🗑️</span>
              <span>取消收藏</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">📚</div>
        <p class="empty-text">还没有收藏任何文章</p>
        <p class="empty-hint">去首页看看有什么感兴趣的吧</p>
        <el-button type="primary" @click="$router.push('/front/index')">
          去首页看看
        </el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[8, 16, 24, 32]"
          layout="total, sizes, prev, pager, next"
          :total="Number(total)"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.collect-page {
  min-height: calc(100vh - 4rem);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #6b7280;
}

/* 文章网格 */
.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

/* 文章卡片 */
.article-card {
  background: white;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.article-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

/* 封面 */
.card-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  cursor: pointer;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-card:hover .card-cover img {
  transform: scale(1.1);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card-cover:hover .cover-overlay {
  opacity: 1;
}

.view-text {
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
}

/* 卡片主体 */
.card-body {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.75rem;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-desc {
  color: #6b7280;
  font-size: 0.95rem;
  line-height: 1.6;
  margin-bottom: 1rem;
  flex: 1;
  height: 4.8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.collect-time {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #eff6ff;
  border-radius: 0.5rem;
  margin-top: auto;
}

.time-icon {
  font-size: 1rem;
}

.time-text {
  font-size: 0.875rem;
  color: #3b82f6;
  font-weight: 500;
}

/* 卡片底部 */
.card-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 0.75rem;
}

.btn {
  flex: 1;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-icon {
  font-size: 1.1rem;
}

.btn-primary {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.btn-danger {
  background: white;
  color: #ef4444;
  border: 2px solid #ef4444;
}

.btn-danger:hover {
  background: #ef4444;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 1.5rem;
}

.empty-text {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.empty-hint {
  font-size: 1rem;
  color: #6b7280;
  margin-bottom: 2rem;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 2rem 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }

  .article-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .card-footer {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>

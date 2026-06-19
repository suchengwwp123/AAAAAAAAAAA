<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import useSystemStore from '@/stores/system'
import { areaList } from '@vant/area-data'

const route = useRoute()
const router = useRouter()
const systemStore = useSystemStore()

const vitae = ref({
  name: '',
  sex: '',
  phone: '',
  idnumber: '',
  address: '',
  img: '',
  des: '',
  content: '',
  file: '',
  price: ''
})

const loading = ref(false)
const dialogVisible = ref(false)
const ruleFormRef = ref()

// 预约表单
const orderForm = reactive({
  address: '',
  time: '',
  des: ''
})

// 表单校验规则
const rules = reactive({
  address: [{ required: true, message: '请输入预约地址', trigger: 'blur' }],
  time: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
})

// 身份证号脱敏处理
const maskIdNumber = (idNumber) => {
  if (!idNumber || idNumber.length < 10) return idNumber
  return idNumber.slice(0, 6) + '********' + idNumber.slice(-4)
}

// 地区代码转换为地区名称
const formatRegion = (codes) => {
  if (!codes) return '全市'
  
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

// 加载陪诊师详情
const loadVitaeDetail = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('参数错误')
    router.back()
    return
  }

  try {
    loading.value = true
    const res = await request.get(`/vitae/${id}`)
    if (res.code === 200) {
      vitae.value = res.data
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载陪诊师详情失败：', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 下载资质文件
const downloadFile = () => {
  if (vitae.value.file) {
    window.open(vitae.value.file, '_blank')
  }
}

// 立即预约
const handleContact = () => {
  // 检查是否登录
  if (!systemStore.userInfo || !systemStore.userInfo.id) {
    ElMessage.warning('请先登录后再预约')
    router.push('/login')
    return
  }
  
  dialogVisible.value = true
}

// 关闭对话框
const handleClose = () => {
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields()
  }
  Object.assign(orderForm, {
    address: '',
    time: '',
    des: ''
  })
  dialogVisible.value = false
}

// 提交预约
const handleSubmitOrder = async () => {
  if (!ruleFormRef.value) return
  
  await ruleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const orderData = {
          userId: systemStore.userInfo.id,
          resumeId: vitae.value.id,
          address: orderForm.address,
          time: orderForm.time,
          des: orderForm.des,
          statu: '未接单'
        }
        
        await request.post('/order', orderData)
        ElMessage.success('预约成功！陪诊师会尽快与您联系')
        handleClose()
      } catch (error) {
        console.error('预约失败：', error)
        ElMessage.error('预约失败，请稍后重试')
      }
    }
  })
}

onMounted(() => {
  loadVitaeDetail()
})
</script>

<template>
  <div class="vitae-detail-page">
    <!-- 页头：返回按钮 + 预约按钮 -->
    <div class="top-bar">
      <el-button @click="router.back()" class="back-btn">
        <span class="icon">←</span>
        <span>返回列表</span>
      </el-button>
      <el-button type="primary" @click="handleContact" class="contact-btn-header">
      立即预约
      </el-button>
    </div>

    <!-- 主内容区 -->
    <div class="detail-container" v-loading="loading">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <h2>📋 陪诊师信息</h2>
        </div>
        
        <div class="card-body">
          <!-- 左侧：照片 -->
          <div class="photo-section">
            <div class="photo-wrapper">
              <img :src="vitae.img || 'https://via.placeholder.com/120x160'" :alt="vitae.name" />
            </div>
            <div class="name-tag">{{ vitae.name }}</div>
          </div>

          <!-- 右侧：详细信息 -->
          <div class="info-section">
            <div class="info-grid">
              <div class="info-item">
                <span class="label">👤 姓名：</span>
                <span class="value">{{ vitae.name || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">⚧ 性别：</span>
                <span class="value">{{ vitae.sex || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">📞 电话：</span>
                <span class="value">{{ vitae.phone || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">🆔 身份证：</span>
                <span class="value">{{ maskIdNumber(vitae.idnumber) || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">💰 参考价格：</span>
                <span class="value price-highlight">{{ vitae.price ? `¥${vitae.price}/天` : '-' }}</span>
              </div>
              <div class="info-item full-width">
                <span class="label">📍 所在区域：</span>
                <span class="value">{{ formatRegion(vitae.address) }}</span>
              </div>
            </div>

            <!-- 简介 -->
            <div class="description">
              <div class="desc-label">💼 个人简介</div>
              <div class="desc-content">{{ vitae.des || '暂无简介' }}</div>
            </div>

            <!-- 资质文件 -->
            <div class="certificate" v-if="vitae.file">
              <div class="cert-label">📎 资质文件</div>
              <el-button type="primary" size="small" @click="downloadFile" class="download-btn">
                下载资质文件
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 简历详情卡片 -->
      <div class="resume-card">
        <div class="card-header">
          <h2>📝 简历详情</h2>
        </div>
        <div class="card-body">
          <div class="resume-content" v-html="vitae.content || '<p style=\'color: #999;\'>暂无详细简历</p>'"></div>
        </div>
      </div>

    </div>

    <!-- 预约对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="📅 预约陪诊服务"
      width="600px"
      :before-close="handleClose"
      class="order-dialog"
    >
      <el-form
        ref="ruleFormRef"
        :model="orderForm"
        :rules="rules"
        label-width="100px"
        class="order-form"
      >
        <!-- 陪诊师信息展示 -->
        <div class="order-info-box">
          <div class="info-row">
            <span class="info-label">陪诊师：</span>
            <span class="info-value">{{ vitae.name }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">工作区域：</span>
            <span class="info-value">{{ formatRegion(vitae.address) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">参考价格：</span>
            <span class="info-value highlight">¥{{ vitae.price }}/天（具体费用需沟通）</span>
          </div>
        </div>

        <el-form-item label="预约地址" prop="address">
          <el-input
            v-model="orderForm.address"
            placeholder="请输入就医医院或详细地址"
            clearable
          />
          <div class="form-tip">
            <span class="tip-icon">💡</span>
            <span class="tip-text">建议选择 <strong>{{ formatRegion(vitae.address) }}</strong> 范围内的医院，以便陪诊师更好地为您服务</span>
          </div>
        </el-form-item>

        <el-form-item label="预约时间" prop="time">
          <el-date-picker
            v-model="orderForm.time"
            type="datetime"
            placeholder="选择预约日期和时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="(time) => time.getTime() < Date.now() - 86400000"
            style="width: 100%"
          />
          <div class="form-tip" style="background: #f0f9ff; border-left-color: #3b82f6;">
            <span class="tip-icon">ℹ️</span>
            <span class="tip-text" style="color: #1e40af;">预约成功后，陪诊师会与您联系协商具体服务时间和费用</span>
          </div>
        </el-form-item>

        <el-form-item label="其他需求" prop="des">
          <el-input
            v-model="orderForm.des"
            type="textarea"
            :rows="4"
            placeholder="请描述您的特殊需求或注意事项（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" @click="handleSubmitOrder">
            确认预约
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.vitae-detail-page {
  min-height: calc(100vh - 4rem);
  background: linear-gradient(to bottom right, #f8fafc, #e2e8f0);
  padding: 0;
}

.top-bar {
  background: white;
  border-bottom: 1px solid #e5e7eb;
  padding: 1rem 2rem;
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .back-btn {
    border: none;
    background: transparent;
    color: #3b82f6;
    font-size: 14px;
    padding: 8px 16px;
    transition: all 0.3s;
    
    .icon {
      font-size: 18px;
      margin-right: 4px;
    }
    
    &:hover {
      background: #eff6ff;
      color: #2563eb;
    }
  }
  
  .contact-btn-header {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    border: none;
    padding: 10px 24px;
    font-size: 14px;
    font-weight: 600;
    border-radius: 1.5rem;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }
  }
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem 2rem 2rem;
}

.info-card,
.resume-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
  overflow: hidden;
  
  .card-header {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    padding: 1.5rem 2rem;
    
    h2 {
      margin: 0;
      color: white;
      font-size: 20px;
      font-weight: 600;
    }
  }
  
  .card-body {
    padding: 2rem;
  }
}

.info-card {
  .card-body {
    display: flex;
    gap: 2rem;
  }
}

.photo-section {
  flex-shrink: 0;
  text-align: center;
  
  .photo-wrapper {
    width: 180px;
    height: 240px;
    border-radius: 0.75rem;
    overflow: hidden;
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
    margin-bottom: 1rem;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .name-tag {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    padding: 0.5rem;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
    border-radius: 0.5rem;
  }
}

.info-section {
  flex: 1;
  
  .info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem;
    margin-bottom: 1.5rem;
    
    .info-item {
      display: flex;
      align-items: center;
      
      &.full-width {
        grid-column: 1 / -1;
      }
      
      .label {
        font-size: 14px;
        color: #6b7280;
        font-weight: 500;
        min-width: 100px;
      }
      
      .value {
        font-size: 15px;
        color: #1f2937;
        font-weight: 500;
      }
      
      .value.price-highlight {
        color: #3b82f6;
        font-size: 16px;
        font-weight: 600;
      }
    }
  }
  
  .description {
    background: #f9fafb;
    padding: 1.5rem;
    border-radius: 0.75rem;
    border-left: 4px solid #3b82f6;
    margin-bottom: 1.5rem;
    
    .desc-label {
      font-size: 14px;
      color: #6b7280;
      font-weight: 600;
      margin-bottom: 0.75rem;
    }
    
    .desc-content {
      font-size: 15px;
      color: #374151;
      line-height: 1.8;
      max-height: 5.4rem; // 3行的高度 (1.8 * 3)
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
    }
  }
  
  .certificate {
    display: flex;
    align-items: center;
    gap: 1rem;
    
    .cert-label {
      font-size: 14px;
      color: #6b7280;
      font-weight: 600;
    }
    
    .download-btn {
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      border: none;
      
      &:hover {
        opacity: 0.9;
      }
    }
  }
}

.resume-card {
  .resume-content {
    font-size: 15px;
    line-height: 1.8;
    color: #374151;
    
    :deep(p) {
      margin: 0.75rem 0;
    }
    
    :deep(ul) {
      margin: 0.75rem 0;
      padding-left: 1.5rem;
    }
    
    :deep(li) {
      margin: 0.5rem 0;
      line-height: 1.8;
    }
    
    :deep(strong) {
      color: #1f2937;
      font-weight: 600;
    }
    
    :deep(h1, h2, h3, h4, h5, h6) {
      margin: 1.5rem 0 0.75rem;
      color: #1f2937;
      font-weight: 600;
    }
  }
}

/* 预约对话框样式 */
.form-tip {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin-top: 0.5rem;
  padding: 0.75rem 1rem;
  background: #fffbeb;
  border-left: 3px solid #f59e0b;
  border-radius: 0.375rem;
  
  .tip-icon {
    font-size: 16px;
    flex-shrink: 0;
  }
  
  .tip-text {
    font-size: 13px;
    color: #92400e;
    line-height: 1.6;
    
    strong {
      color: #3b82f6;
      font-weight: 600;
    }
  }
}

.order-info-box {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-left: 4px solid #3b82f6;
  border-radius: 0.5rem;
  padding: 1rem 1.5rem;
  margin-bottom: 1.5rem;
  
  .info-row {
    display: flex;
    align-items: center;
    margin-bottom: 0.5rem;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .info-label {
      font-size: 14px;
      color: #6b7280;
      font-weight: 500;
      min-width: 80px;
    }
    
    .info-value {
      font-size: 15px;
      color: #1f2937;
      font-weight: 600;
      
      &.highlight {
        color: #3b82f6;
        font-size: 15px;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

:deep(.order-dialog) {
  .el-dialog__header {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    padding: 1.5rem;
    margin: 0;
    
    .el-dialog__title {
      color: white;
      font-size: 18px;
      font-weight: 600;
    }
    
    .el-dialog__headerbtn .el-dialog__close {
      color: white;
      
      &:hover {
        color: #dbeafe;
      }
    }
  }
  
  .el-dialog__body {
    padding: 2rem;
  }
  
  .el-dialog__footer {
    padding: 1rem 2rem;
    border-top: 1px solid #e5e7eb;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .top-bar {
    padding: 0.75rem 1rem;
    
    .contact-btn-header {
      padding: 8px 16px;
      font-size: 12px;
    }
  }
  
  .detail-container {
    padding: 1rem;
  }
  
  .info-card .card-body {
    flex-direction: column;
    align-items: center;
  }
  
  .photo-section {
    margin-bottom: 1.5rem;
  }
  
  .info-section .info-grid {
    grid-template-columns: 1fr;
  }
  
  :deep(.order-dialog) {
    width: 90% !important;
    
    .el-dialog__body {
      padding: 1.5rem 1rem;
    }
  }
}
</style>
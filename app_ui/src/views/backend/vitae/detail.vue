<script setup>
import { ref, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { areaList } from '@vant/area-data'
import useSystemStore from '@/stores/system'

const systemStore = useSystemStore()

// 简历数据
const vitae = ref({})
const loading = ref(false)

// 身份证号脱敏
const maskIdNumber = (idnumber) => {
  if (!idnumber || idnumber.length < 10) return idnumber
  return idnumber.slice(0, 6) + '********' + idnumber.slice(-4)
}

// 格式化地址
const formatRegion = (codes) => {
  if (!codes) return ''
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

// 加载简历详情
const loadVitaeDetail = async () => {
  loading.value = true
  try {
    // 使用当前登录用户的ID查询简历
    const res = await request.get('/vitae/page', {
      params: {
        userId: systemStore.userInfo?.id,
        pageNum: 1,
        pageSize: 1
      }
    })
    
    if (res.data && res.data.records && res.data.records.length > 0) {
      vitae.value = res.data.records[0]
    } else {
      ElMessage.warning('暂无简历信息，联系管理员！')
    }
  } catch (error) {
    console.error('加载简历详情失败：', error)
    ElMessage.error('加载简历详情失败')
  } finally {
    loading.value = false
  }
}

// 下载资质文件
const downloadFile = (url) => {
  if (url) {
    window.open(url, '_blank')
  }
}

// 页面加载
onMounted(() => {
  loadVitaeDetail()
})
</script>

<template>
  <div class="vitae-detail-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-empty description="加载中..." />
    </div>

    <!-- 主内容区 -->
    <div v-else class="detail-container">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <h3 class="card-title">
          <span class="title-icon">👤</span>
          基本信息
        </h3>

        <div class="info-content">
          <!-- 左侧照片 -->
          <div class="photo-section">
            <div class="photo-wrapper">
              <img
                v-if="vitae.img"
                :src="vitae.img"
                :alt="vitae.name"
                class="vitae-photo"
              />
              <div v-else class="photo-placeholder">
                <span class="placeholder-icon">👤</span>
              </div>
            </div>
            <div class="name-tag">{{ vitae.name || '姓名' }}</div>
          </div>

          <!-- 右侧信息 -->
          <div class="info-section">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">姓名</span>
                <span class="info-value">{{ vitae.name || '-' }}</span>
              </div>

              <div class="info-item">
                <span class="info-label">性别</span>
                <span class="info-value">{{ vitae.sex || '-' }}</span>
              </div>

              <div class="info-item">
                <span class="info-label">联系电话</span>
                <span class="info-value phone">{{ vitae.phone || '-' }}</span>
              </div>

              <div class="info-item">
                <span class="info-label">身份证号</span>
                <span class="info-value">{{ vitae.idnumber ? maskIdNumber(vitae.idnumber) : '-' }}</span>
              </div>

              <div class="info-item full-width">
                <span class="info-label">所在区域</span>
                <span class="info-value">{{ vitae.address ? formatRegion(vitae.address) : '-' }}</span>
              </div>

              <div class="info-item">
                <span class="info-label">参考金额</span>
                <span class="info-value price-highlight">
                  <span v-if="vitae.price" class="price-value">¥{{ vitae.price }}/天</span>
                  <span v-else>-</span>
                </span>
              </div>

              <div class="info-item full-width" v-if="vitae.des">
                <span class="info-label">个人简介</span>
                <div class="desc-content">{{ vitae.des }}</div>
              </div>

              <div class="info-item full-width" v-if="vitae.file">
                <span class="info-label">资质文件</span>
                <div class="file-download">
                  <el-button
                    type="success"
                    :icon="Download"
                    size="small"
                    @click="downloadFile(vitae.file)"
                  >
                    下载资质文件
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 简历详情卡片 -->
      <div class="resume-card" v-if="vitae.content">
        <h3 class="card-title">
          <span class="title-icon">📝</span>
          简历详情
        </h3>
        <div class="resume-content" v-html="vitae.content"></div>
      </div>

      <!-- 空状态提示 -->
      <div v-if="!vitae.content" class="empty-resume">
        <el-empty description="暂无简历详情" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.vitae-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
  padding: 2rem 0;
}

/* 主内容区 */
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.loading-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 3rem;
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 卡片通用样式 */
.info-card,
.resume-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 1.5rem;
  overflow: hidden;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 8px 20px rgba(59, 130, 246, 0.15);
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  padding: 1.5rem 2rem;
  margin: 0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 2px solid #e5e7eb;

  .title-icon {
    font-size: 1.5rem;
  }
}

/* 基本信息卡片 */
.info-content {
  display: flex;
  gap: 2rem;
  padding: 2rem;
}

.photo-section {
  flex-shrink: 0;
  text-align: center;
}

.photo-wrapper {
  width: 160px;
  height: 213px;
  border-radius: 0.75rem;
  overflow: hidden;
  border: 3px solid #3b82f6;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 8px 16px rgba(59, 130, 246, 0.3);
  margin-bottom: 1rem;
}

.vitae-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);

  .placeholder-icon {
    font-size: 4rem;
    color: white;
  }
}

.name-tag {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  padding: 0.75rem 1rem;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-radius: 0.5rem;
  border: 2px solid #3b82f6;
}

.info-section {
  flex: 1;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;

  &.full-width {
    grid-column: 1 / -1;
  }

  .info-label {
    font-size: 0.875rem;
    font-weight: 600;
    color: #6b7280;
  }

  .info-value {
    font-size: 1rem;
    color: #1f2937;
    font-weight: 500;

    &.phone {
      color: #3b82f6;
      font-family: 'Courier New', monospace;
    }
  }

  .desc-content {
    font-size: 0.95rem;
    color: #4b5563;
    line-height: 1.8;
    padding: 1rem;
    background: #f9fafb;
    border-left: 3px solid #3b82f6;
    border-radius: 0.5rem;
  }

  .file-download {
    margin-top: 0.5rem;
  }
}

.price-highlight {
  display: flex;
  align-items: center;
  gap: 0.5rem;

  .price-value {
    font-size: 1.25rem;
    font-weight: 700;
    color: #f59e0b;
    background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
    padding: 0.5rem 1rem;
    border-radius: 0.5rem;
    border: 2px solid #f59e0b;
  }
}

/* 简历详情卡片 */
.resume-content {
  padding: 2rem;
  color: #374151;
  line-height: 1.8;
  font-size: 0.95rem;

  // 富文本内容样式
  :deep(h1) {
    font-size: 1.75rem;
    font-weight: 700;
    color: #1f2937;
    margin: 1.5rem 0 1rem;
    padding-bottom: 0.5rem;
    border-bottom: 2px solid #e5e7eb;
  }

  :deep(h2) {
    font-size: 1.5rem;
    font-weight: 700;
    color: #374151;
    margin: 1.25rem 0 0.75rem;
  }

  :deep(h3) {
    font-size: 1.25rem;
    font-weight: 600;
    color: #4b5563;
    margin: 1rem 0 0.5rem;
  }

  :deep(p) {
    margin: 0.75rem 0;
    line-height: 1.8;
  }

  :deep(ul),
  :deep(ol) {
    margin: 0.75rem 0;
    padding-left: 2rem;
  }

  :deep(li) {
    margin: 0.5rem 0;
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 0.5rem;
    margin: 1rem 0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  :deep(blockquote) {
    border-left: 4px solid #3b82f6;
    padding: 1rem 1.5rem;
    margin: 1rem 0;
    background: #f9fafb;
    border-radius: 0.5rem;
    color: #4b5563;
    font-style: italic;
  }

  :deep(code) {
    background: #f3f4f6;
    padding: 0.2rem 0.4rem;
    border-radius: 0.25rem;
    font-family: 'Courier New', monospace;
    font-size: 0.9em;
    color: #dc2626;
  }

  :deep(pre) {
    background: #1f2937;
    color: #f9fafb;
    padding: 1rem;
    border-radius: 0.5rem;
    overflow-x: auto;
    margin: 1rem 0;

    code {
      background: transparent;
      color: #f9fafb;
      padding: 0;
    }
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 1rem 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    th,
    td {
      padding: 0.75rem 1rem;
      border: 1px solid #e5e7eb;
      text-align: left;
    }

    th {
      background: #f9fafb;
      font-weight: 600;
      color: #1f2937;
    }

    tr:hover {
      background: #f9fafb;
    }
  }

  :deep(a) {
    color: #3b82f6;
    text-decoration: none;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}

.empty-resume {
  background: white;
  border-radius: 1rem;
  padding: 3rem;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .vitae-detail-page {
    padding: 1rem 0;
  }

  .detail-container {
    padding: 0 1rem;
  }

  .info-content {
    flex-direction: column;
    padding: 1.5rem;
  }

  .photo-section {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .card-title {
    padding: 1rem 1.5rem;
    font-size: 1.1rem;
  }

  .resume-content {
    padding: 1.5rem;
  }
}
</style>

<script setup>
import { reactive, ref } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import useSystemStore from '@/stores/system'

const $router = useRouter()
const systemStore = useSystemStore()

// 当前页数
const pageNum = ref(1)
// 每页展示量
const pageSize = ref(8)
// 分页数据总数
const total = ref(0)
// 投诉列表
const complainList = ref([])
// 查询条件
const searchForm = reactive({
  statu: ''
})

// 详情弹窗
const detailVisible = ref(false)
const currentComplain = ref(null)

// 状态配置
const statusConfig = {
  '未处理': { color: '#f59e0b', bgColor: '#fef3c7', icon: '⏳' },
  '已处理': { color: '#10b981', bgColor: '#d1fae5', icon: '✅' }
}

// 身份证号脱敏
const maskIdNumber = (idnumber) => {
  if (!idnumber || idnumber.length < 10) return idnumber
  return idnumber.slice(0, 6) + '********' + idnumber.slice(-4)
}

// 加载投诉列表
const load = async () => {
  try {
  const res = await request.get('/complain/page', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
        userId: systemStore.userInfo?.id,
        statu: searchForm.statu || undefined
    }
  })
  pageNum.value = res.data.current
  pageSize.value = res.data.size
  total.value = res.data.total
    complainList.value = res.data.records
  } catch (error) {
    console.error('加载投诉列表失败：', error)
    ElMessage.error('加载投诉列表失败')
  }
}

// 页面加载
load()

// 查询
const handleSearch = () => {
  pageNum.value = 1
  load()
}

// 重置查询
const handleReset = () => {
  searchForm.statu = ''
  pageNum.value = 1
  load()
}

// 查看详情
const handleViewDetail = (complain) => {
  currentComplain.value = complain
  detailVisible.value = true
}

// 分页
const handleSizeChange = (size) => {
  pageSize.value = size
  load()
}

const handleCurrentChange = (current) => {
  pageNum.value = current
  load()
}
</script>

<template>
  <div class="complain-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <h1>💬 我的投诉</h1>
          <p>查看和管理您的投诉记录</p>
      </div>
      </div>
      </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 搜索筛选区域 -->
      <div class="search-bar">
        <div class="search-left">
          <el-select
            v-model="searchForm.statu"
            placeholder="投诉状态"
            clearable
            style="width: 180px"
            @change="handleSearch"
          >
            <el-option label="未处理" value="未处理" />
            <el-option label="已处理" value="已处理" />
          </el-select>

          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- 投诉列表 -->
      <div class="complain-list" v-if="complainList.length > 0">
        <div class="complain-card" v-for="complain in complainList" :key="complain.id">
          <!-- 投诉头部 -->
          <div class="complain-header">
            <div class="complain-info-row">
              <span class="complain-label">投诉编号：</span>
              <span class="complain-value">#{{ complain.id }}</span>
      </div>
            <div
              class="complain-status"
              :style="{
                color: statusConfig[complain.statu]?.color,
                backgroundColor: statusConfig[complain.statu]?.bgColor
              }"
            >
              <span class="status-icon">{{ statusConfig[complain.statu]?.icon }}</span>
              <span>{{ complain.statu }}</span>
            </div>
          </div>

          <!-- 投诉主体 -->
          <div class="complain-body">
            <!-- 订单信息 -->
            <div class="order-info" v-if="complain.order">
              <div class="info-item">
                <span class="info-icon">📋</span>
                <div class="info-content">
                  <span class="info-label">关联订单</span>
                  <span class="info-value">{{ complain.order.ordernum || '-' }}</span>
                </div>
              </div>
            </div>

            <!-- 陪诊师信息 -->
            <div class="worker-info" v-if="complain.vitae">
              <div class="worker-avatar-wrapper">
                <img
                  v-if="complain.vitae?.img"
                  :src="complain.vitae?.img"
                  :alt="complain.vitae?.name"
                  class="worker-avatar"
                />
                <div v-else class="worker-avatar-placeholder">
                  {{ complain.vitae?.name?.charAt(0) || '?' }}
                </div>
              </div>
              <div class="worker-detail">
                <h4 class="worker-name">{{ complain.vitae?.name || '陪诊师' }}</h4>
                <p class="worker-phone">{{ complain.vitae?.phone || '-' }}</p>
              </div>
            </div>

            <!-- 投诉内容 -->
            <div class="complain-content">
              <div class="content-label">投诉内容：</div>
              <div class="content-text">{{ complain.des }}</div>
            </div>

            <!-- 处理结果 -->
            <div class="complain-result" v-if="complain.ress && complain.statu === '已处理'">
              <div class="result-label">处理结果：</div>
              <div class="result-text">{{ complain.ress }}</div>
            </div>
          </div>

          <!-- 投诉底部 -->
          <div class="complain-footer">
            <span class="create-time">创建时间：{{ complain.createTime || '-' }}</span>
            <div class="action-buttons">
              <el-button size="small" @click="handleViewDetail(complain)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else description="暂无投诉记录" class="empty-state">
        <el-button type="primary" @click="$router.push('/front/order')">
          查看我的预约
        </el-button>
      </el-empty>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="complainList.length > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[8, 16, 24, 32]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="投诉详情" width="700px" :close-on-click-modal="false">
      <div class="detail-dialog" v-if="currentComplain">
        <div class="detail-section">
          <h4 class="section-title">📋 投诉信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">投诉编号：</span>
              <span class="value">#{{ currentComplain.id }}</span>
            </div>
            <div class="info-item">
              <span class="label">投诉状态：</span>
              <span
                class="value status-tag"
                :style="{
                  color: statusConfig[currentComplain.statu]?.color,
                  backgroundColor: statusConfig[currentComplain.statu]?.bgColor
                }"
              >
                {{ statusConfig[currentComplain.statu]?.icon }} {{ currentComplain.statu }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ currentComplain.createTime || '-' }}</span>
            </div>
            <div class="info-item" v-if="currentComplain.updateTime">
              <span class="label">更新时间：</span>
              <span class="value">{{ currentComplain.updateTime }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentComplain.order">
          <h4 class="section-title">📝 关联订单</h4>
          <div class="info-grid">
            <div class="info-item full-width">
              <span class="label">订单编号：</span>
              <span class="value">{{ currentComplain.order.ordernum || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentComplain.vitae">
          <h4 class="section-title">👨‍⚕️ 陪诊师信息</h4>
          <div class="worker-detail-info">
            <div class="detail-avatar-wrapper">
              <img
                v-if="currentComplain.vitae?.img"
                :src="currentComplain.vitae?.img"
                :alt="currentComplain.vitae?.name"
                class="detail-avatar"
              />
              <div v-else class="detail-avatar-placeholder">
                {{ currentComplain.vitae?.name?.charAt(0) || '?' }}
              </div>
            </div>
            <div class="worker-info-text">
              <h3>{{ currentComplain.vitae?.name || '陪诊师' }}</h3>
              <p v-if="currentComplain.vitae?.phone">联系电话：{{ currentComplain.vitae?.phone }}</p>
              <p v-if="currentComplain.vitae?.idnumber">
                身份证号：{{ maskIdNumber(currentComplain.vitae?.idnumber) }}
              </p>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">💬 投诉内容</h4>
          <div class="content-box">
            {{ currentComplain.des }}
          </div>
        </div>

        <div class="detail-section" v-if="currentComplain.ress && currentComplain.statu === '已处理'">
          <h4 class="section-title">✅ 处理结果</h4>
          <div class="result-box">
            {{ currentComplain.ress }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.complain-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
}

/* 页面头部 */
.page-header {
  background: white;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1.5rem 2rem;
}

.header-title h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.header-title p {
  font-size: 1rem;
  color: #6b7280;
  margin: 0;
}

/* 主内容区 */
.main-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem 2rem 2rem 2rem;
}

.search-bar {
  background: white;
  border-radius: 1rem;
  padding: 1.25rem;
  margin-bottom: 1rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .search-left {
    display: flex;
    gap: 1rem;
    align-items: center;
  }
}

.complain-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.complain-card {
  background: white;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.15);
    transform: translateY(-2px);
  }
}

.complain-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e5e7eb;

  .complain-info-row {
    .complain-label {
      font-size: 13px;
      color: #6b7280;
      margin-right: 0.5rem;
    }

    .complain-value {
      font-size: 14px;
      font-weight: 600;
      color: #1f2937;
      font-family: 'Courier New', monospace;
    }
  }

  .complain-status {
    padding: 0.375rem 1rem;
    border-radius: 2rem;
    font-size: 13px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 0.25rem;

    .status-icon {
      font-size: 14px;
    }
  }
}

.complain-body {
  padding: 1.25rem;

  .order-info {
    margin-bottom: 1rem;
    padding: 0.75rem 1rem;
    background: #f9fafb;
    border-radius: 0.5rem;
    border-left: 3px solid #3b82f6;

    .info-item {
      display: flex;
      gap: 0.75rem;
      align-items: center;

      .info-icon {
        font-size: 18px;
      }

      .info-content {
        display: flex;
        gap: 0.5rem;
        align-items: center;

        .info-label {
          font-size: 13px;
          color: #6b7280;
        }

        .info-value {
          font-size: 14px;
          font-weight: 600;
          color: #1f2937;
          font-family: 'Courier New', monospace;
        }
      }
    }
  }

  .worker-info {
    display: flex;
    gap: 1rem;
    margin-bottom: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #e5e7eb;

    .worker-avatar-wrapper {
      flex-shrink: 0;
      width: 60px;
      height: 80px;
      border-radius: 0.5rem;
      overflow: hidden;
      border: 2px solid #3b82f6;
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);

      .worker-avatar {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .worker-avatar-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        font-weight: 600;
        color: white;
      }
    }

    .worker-detail {
      flex: 1;

      .worker-name {
        font-size: 18px;
        font-weight: 600;
        color: #1f2937;
        margin: 0 0 0.5rem 0;
      }

      .worker-phone {
        font-size: 14px;
        color: #6b7280;
        margin: 0;
      }
    }
  }

  .complain-content {
    margin-bottom: 1rem;

    .content-label {
      font-size: 13px;
      font-weight: 600;
      color: #6b7280;
      margin-bottom: 0.5rem;
    }

    .content-text {
      font-size: 14px;
      color: #1f2937;
      line-height: 1.8;
      padding: 1rem;
      background: #fef3c7;
      border-left: 3px solid #f59e0b;
      border-radius: 0.5rem;
    }
  }

  .complain-result {
    .result-label {
      font-size: 13px;
      font-weight: 600;
      color: #6b7280;
      margin-bottom: 0.5rem;
    }

    .result-text {
      font-size: 14px;
      color: #1f2937;
      line-height: 1.8;
      padding: 1rem;
      background: #d1fae5;
      border-left: 3px solid #10b981;
      border-radius: 0.5rem;
    }
  }
}

.complain-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;

  .create-time {
    font-size: 13px;
    color: #6b7280;
  }

  .action-buttons {
    display: flex;
    gap: 0.5rem;
  }
}

.empty-state {
  background: white;
  border-radius: 1rem;
  padding: 3rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
  padding: 1.25rem;
  background: white;
  border-radius: 1rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

// 详情对话框样式
.detail-dialog {
  .detail-section {
    margin-bottom: 1rem;

    &:last-child {
      margin-bottom: 0;
    }

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 0.75rem 0;
      padding-bottom: 0.5rem;
      border-bottom: 2px solid #e5e7eb;
    }
  }

  .info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0.75rem;

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;

      &.full-width {
        grid-column: 1 / -1;
      }

      .label {
        font-size: 13px;
        color: #6b7280;
      }

      .value {
        font-size: 14px;
        color: #1f2937;
        font-weight: 500;

        &.status-tag {
          display: inline-flex;
          align-items: center;
          padding: 0.25rem 0.75rem;
          border-radius: 1rem;
          font-size: 13px;
          width: fit-content;
        }
      }
    }
  }

  .worker-detail-info {
    display: flex;
    gap: 1.5rem;
    align-items: flex-start;

    .detail-avatar-wrapper {
      flex-shrink: 0;
      width: 90px;
      height: 120px;
      border-radius: 0.5rem;
      overflow: hidden;
      border: 2px solid #3b82f6;
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);

      .detail-avatar {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .detail-avatar-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 36px;
        font-weight: 600;
        color: white;
      }
    }

    .worker-info-text {
      flex: 1;

      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #1f2937;
        margin: 0 0 0.5rem 0;
      }

      p {
        font-size: 14px;
        color: #6b7280;
        margin: 0.25rem 0;
        line-height: 1.6;
      }
    }
  }

  .content-box,
  .result-box {
    padding: 1rem;
    background: #f9fafb;
    border-radius: 0.5rem;
    font-size: 14px;
    color: #1f2937;
    line-height: 1.8;
    border-left: 3px solid #3b82f6;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .header-content {
    padding: 1.5rem 1rem;
  }

  .header-title h1 {
    font-size: 1.5rem;
  }

  .main-container {
    padding: 1rem;
  }

  .search-bar .search-left {
    flex-direction: column;
    width: 100%;

    > * {
      width: 100% !important;
    }
  }

  .detail-dialog .info-grid {
    grid-template-columns: 1fr;

    .info-item.full-width {
      grid-column: 1;
    }
  }
}
</style>

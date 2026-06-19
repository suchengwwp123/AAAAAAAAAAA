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
// 工单列表
const ticketList = ref([])
// 查询条件
const searchForm = reactive({
  statu: ''
})

// 详情弹窗
const detailVisible = ref(false)
const currentTicket = ref(null)

// 状态配置
const statusConfig = {

  '初始状态': { color: '#f59e0b', bgColor: '#fef3c7', icon: '⏳' },
  '已经办结': { color: '#10b981', bgColor: '#d1fae5', icon: '✅' }
}

// 身份证号脱敏
const maskIdNumber = (idnumber) => {
  if (!idnumber || idnumber.length < 10) return idnumber
  return idnumber.slice(0, 6) + '********' + idnumber.slice(-4)
}

// 加载工单列表
  const load = async () => {
  try {
    const res = await request.get('/tickets/page', {
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
    ticketList.value = res.data.records
  } catch (error) {
    console.error('加载工单列表失败：', error)
    ElMessage.error('加载工单列表失败')
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
const handleViewDetail = (ticket) => {
  currentTicket.value = ticket
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
  <div class="ticket-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <h1>📋 我的工单</h1>
          <p>查看低评分订单生成的工单记录</p>
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
            placeholder="工单状态"
            clearable
            style="width: 180px"
            @change="handleSearch"
          >

            <el-option label="初始状态" value="初始状态" />
            <el-option label="已经办结" value="已经办结" />
                          </el-select>

          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- 工单列表 -->
      <div class="ticket-list" v-if="ticketList.length > 0">
        <div class="ticket-card" v-for="ticket in ticketList" :key="ticket.id">
          <!-- 工单头部 -->
          <div class="ticket-header">
            <div class="ticket-info-row">
              <span class="ticket-label">工单编号：</span>
              <span class="ticket-value">#{{ ticket.id }}</span>
      </div>
            <div
              class="ticket-status"
              :style="{
                color: statusConfig[ticket.statu]?.color,
                backgroundColor: statusConfig[ticket.statu]?.bgColor
              }"
            >
              <span class="status-icon">{{ statusConfig[ticket.statu]?.icon }}</span>
              <span>{{ ticket.statu }}</span>
            </div>
          </div>

          <!-- 工单主体 -->
          <div class="ticket-body">
            <!-- 订单信息 -->
            <div class="order-info" v-if="ticket.order">
              <div class="info-item">
                <span class="info-icon">📋</span>
                <div class="info-content">
                  <span class="info-label">关联订单</span>
                  <span class="info-value">{{ ticket.order.ordernum || '-' }}</span>
                </div>
              </div>
              <div class="info-item" v-if="ticket.order.rate">
                <span class="info-icon">⭐</span>
                <div class="info-content">
                  <span class="info-label">评分</span>
                    <el-rate
                    :model-value="ticket.order.rate"
                      disabled
                      show-score
                      text-color="#ff9900"
                    style="display: inline-flex; align-items: center;"
                  />
                </div>
              </div>
            </div>

            <!-- 评价信息 -->
            <div class="ticket-content" v-if="ticket.order && ticket.order.content">
              <div class="content-label">评价内容：</div>
              <div class="content-text">{{ ticket.order.content }}</div>
            </div>

            <!-- 陪诊师信息 -->
            <div class="worker-info" v-if="ticket.vitae">
              <div class="worker-avatar-wrapper">
                <img
                  v-if="ticket.vitae?.img"
                  :src="ticket.vitae?.img"
                  :alt="ticket.vitae?.name"
                  class="worker-avatar"
                />
                <div v-else class="worker-avatar-placeholder">
                  {{ ticket.vitae?.name?.charAt(0) || '?' }}
                </div>
              </div>
              <div class="worker-detail">
                <h4 class="worker-name">{{ ticket.vitae?.name || '陪诊师' }}</h4>
                <p class="worker-phone">{{ ticket.vitae?.phone || '-' }}</p>
              </div>
            </div>

            <!-- 处理结果 -->
            <div class="ticket-result" v-if="ticket.ress && ticket.statu === '已经办结'">
              <div class="result-label">处理结果：</div>
              <div class="result-text">{{ ticket.ress }}</div>
            </div>
          </div>

          <!-- 工单底部 -->
          <div class="ticket-footer">
            <span class="create-time">创建时间：{{ ticket.createTime || '-' }}</span>
            <div class="action-buttons">
              <el-button size="small" @click="handleViewDetail(ticket)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else description="暂无工单记录" class="empty-state">
        <el-button type="primary" @click="$router.push('/front/order')">
          查看我的预约
        </el-button>
      </el-empty>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="ticketList.length > 0">
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
    <el-dialog v-model="detailVisible" title="工单详情" width="700px" :close-on-click-modal="false">
      <div class="detail-dialog" v-if="currentTicket">
        <div class="detail-section">
          <h4 class="section-title">📋 工单信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">工单编号：</span>
              <span class="value">#{{ currentTicket.id }}</span>
            </div>
            <div class="info-item">
              <span class="label">工单状态：</span>
              <span
                class="value status-tag"
                :style="{
                  color: statusConfig[currentTicket.statu]?.color,
                  backgroundColor: statusConfig[currentTicket.statu]?.bgColor
                }"
              >
                {{ statusConfig[currentTicket.statu]?.icon }} {{ currentTicket.statu }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ currentTicket.createTime || '-' }}</span>
            </div>
            <div class="info-item" v-if="currentTicket.updateTime">
              <span class="label">办结时间：</span>
              <span class="value">{{ currentTicket.updateTime }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentTicket.order">
          <h4 class="section-title">📝 关联订单</h4>
          <div class="info-grid">
            <div class="info-item full-width">
              <span class="label">订单编号：</span>
              <span class="value">{{ currentTicket.order.ordernum || '-' }}</span>
            </div>
            <div class="info-item" v-if="currentTicket.order.rate">
              <span class="label">评分：</span>
              <el-rate
                :model-value="currentTicket.order.rate"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
            <div class="info-item full-width" v-if="currentTicket.order.content">
              <span class="label">评价内容：</span>
              <span class="value">{{ currentTicket.order.content }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentTicket.vitae">
          <h4 class="section-title">👨‍⚕️ 陪诊师信息</h4>
          <div class="worker-detail-info">
            <div class="detail-avatar-wrapper">
              <img
                v-if="currentTicket.vitae?.img"
                :src="currentTicket.vitae?.img"
                :alt="currentTicket.vitae?.name"
                class="detail-avatar"
              />
              <div v-else class="detail-avatar-placeholder">
                {{ currentTicket.vitae?.name?.charAt(0) || '?' }}
              </div>
            </div>
            <div class="worker-info-text">
              <h3>{{ currentTicket.vitae?.name || '陪诊师' }}</h3>
              <p v-if="currentTicket.vitae?.phone">联系电话：{{ currentTicket.vitae?.phone }}</p>
              <p v-if="currentTicket.vitae?.idnumber">
                身份证号：{{ maskIdNumber(currentTicket.vitae?.idnumber) }}
              </p>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentTicket.ress && currentTicket.statu === '已经办结'">
          <h4 class="section-title">✅ 处理结果</h4>
          <div class="result-box">
            {{ currentTicket.ress }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.ticket-page {
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

.ticket-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.ticket-card {
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

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e5e7eb;

  .ticket-info-row {
    .ticket-label {
      font-size: 13px;
      color: #6b7280;
      margin-right: 0.5rem;
    }

    .ticket-value {
      font-size: 14px;
      font-weight: 600;
      color: #1f2937;
      font-family: 'Courier New', monospace;
    }
  }

  .ticket-status {
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

.ticket-body {
  padding: 1.25rem;

  .order-info {
    margin-bottom: 1rem;
    padding: 0.75rem 1rem;
    background: #f9fafb;
    border-radius: 0.5rem;
    border-left: 3px solid #3b82f6;
    display: flex;
    gap: 2rem;
    flex-wrap: wrap;

    .info-item {
      display: flex;
      gap: 0.75rem;
      align-items: center;

      .info-icon {
        font-size: 18px;
      }

      .info-content {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;

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

  .ticket-content {
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

  .ticket-result {
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

.ticket-footer {
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

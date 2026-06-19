<script setup>
  import { reactive, ref, computed } from 'vue'
  import { Search, Refresh } from '@element-plus/icons-vue'
  import request from '@/utils/request'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useRoute, useRouter } from "vue-router"
  import useSystemStore from '@/stores/system'

  const $router = useRouter()
  const $route = useRoute()
  const systemStore = useSystemStore()

  // 当前页数
  const pageNum = ref(1)
  // 每页展示量
  const pageSize = ref(8)
  // 分页数据总数
  const total = ref(0)
  // 订单列表
  const orderList = ref([])
  // 查询条件
  const searchForm = reactive({
    statu: '',
    ordernum: ''
  })
  
  // 订单详情弹窗
  const detailVisible = ref(false)
  const currentOrder = ref(null)
  
  // 评价弹窗
  const rateVisible = ref(false)
  const rateForm = reactive({
                  id: undefined,
                  userId: undefined,
                  resumeId: undefined,
                  address: undefined,
                  time: undefined,
                  workerId: undefined,
    statu: undefined,
                  des: undefined,
                  stime: undefined,
                  etime: undefined,
                  total: undefined,
                  ordernum: undefined,
                  alinum: undefined,
    rate: 0,
    content: '',
    createTime: undefined
  })
  const rateFormRef = ref()
  // 订单状态配置
  const statusConfig = {
    '未接单': { color: '#f59e0b', bgColor: '#fef3c7', icon: '⏳' },
    '已接单': { color: '#3b82f6', bgColor: '#dbeafe', icon: '✅' },
    '已支付': { color: '#059669', bgColor: '#d1fae5', icon: '💳' },
    '进行中': { color: '#8b5cf6', bgColor: '#ede9fe', icon: '🔄' },
    '已完成': { color: '#10b981', bgColor: '#d1fae5', icon: '✨' },
    '已评价': { color: '#06b6d4', bgColor: '#cffafe', icon: '⭐' },
    '已取消': { color: '#ef4444', bgColor: '#fee2e2', icon: '❌' },
    '拒绝接单': { color: '#dc2626', bgColor: '#fef2f2', icon: '🚫' }
  }







  // 加载订单列表
  const load = async () => {
    try {
    const res = await request.get('/order/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
          userId: systemStore.userInfo?.id,
          statu: searchForm.statu || undefined,
          ordernum: searchForm.ordernum || undefined
  }
  })
    pageNum.value = res.data.current
    pageSize.value = res.data.size
    total.value = res.data.total
      orderList.value = res.data.records
    } catch (error) {
      console.error('加载订单列表失败：', error)
      ElMessage.error('加载订单列表失败')
    }
  }

  // 页面加载
  load()

  // 查询订单
  const handleSearch = () => {
    pageNum.value = 1
    load()
  }

  // 重置查询
  const handleReset = () => {
    searchForm.statu = ''
    searchForm.ordernum = ''
    pageNum.value = 1
    load()
  }

  // 查看订单详情
  const handleViewDetail = (order) => {
    currentOrder.value = order
    detailVisible.value = true
  }

  // 取消订单
  const handleCancelOrder = async (id) => {
    try {
      await ElMessageBox.confirm('确定要取消此订单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await request.delete(`/order/${id}`, { statu: '已取消' })
      ElMessage.success('订单已取消')
      load()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('取消订单失败：', error)
        ElMessage.error('取消订单失败')
      }
    }
  }

  // 付款
  const handlePayment = async (id) => {
    try {
      await ElMessageBox.confirm('确认支付此订单？', '提示', {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'success'
      })


      window.open("http://localhost:9090/api/alipay/pay?id=" + id )
    } catch (error) {
      if (error !== 'cancel') {
        console.error('支付失败：', error)
        ElMessage.error('支付失败')
      }
    }
  }

  // 打开评价弹窗
  const handleOpenRate = (order) => {
    // 复制订单的所有字段到表单
    Object.assign(rateForm, {
      id: order.id,
      userId: order.userId,
      resumeId: order.resumeId,
      address: order.address,
      time: order.time,
      workerId: order.workerId,
      statu: order.statu,
      des: order.des,
      stime: order.stime,
      etime: order.etime,
      total: order.total,
      ordernum: order.ordernum,
      alinum: order.alinum,
      rate: order.rate || 0,
      content: order.content || '',
      createTime: order.createTime
    })
    rateVisible.value = true
  }

  // 提交评价
  const handleSubmitRate = async () => {
    if (rateForm.rate === 0) {
      ElMessage.warning('请选择评分')
      return
    }
    
    try {
      // 提交完整的表单数据，并将状态改为已评价
    const res=   await request.put(`/order/${rateForm.id}`, {
        ...rateForm,
        statu: '已评价'
      })
      if(res.data){
        ElMessage.success(`${res.data}`)
      }else{
        ElMessage.success(`评价成功`)
      }


      rateVisible.value = false
      load()
    } catch (error) {
      console.error('评价失败：', error)
      ElMessage.error('评价失败')
    }
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

  // 格式化地区
  const formatRegion = (addressStr) => {
    if (!addressStr) return '-'
    const codes = addressStr.split(',')
    // 这里简化处理，实际应该通过 areaData 转换
    return addressStr
  }

  // 投诉
  const handleComplaint = (order) => {
    ElMessageBox.prompt('请输入投诉内容', '订单投诉', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请详细描述您的问题...',
      inputValidator: (value) => {
        if (!value || value.trim().length < 10) {
          return '投诉内容至少需要10个字符'
        }
        return true
      }
    }).then(async ({ value }) => {
      try {
        // 调用投诉接口
        await request.post('/complain', {
          orderId: order.id,                    // 订单ID
          des: value,                           // 投诉内容
          userId: systemStore.userInfo?.id,     // 投诉人ID
          vaId: order.resumeId,                 // 陪诊师ID
          statu: '未处理'                       // 默认状态
        })
        ElMessage.success('投诉已提交，我们会尽快处理')
        load()
      } catch (error) {
        console.error('投诉提交失败：', error)
        ElMessage.error('投诉提交失败')
      }
    }).catch(() => {
      // 用户取消
    })
  }







</script>

<template>
  <div class="order-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <h1>📋 我的预约</h1>
          <p>查看和管理您的陪诊预约记录</p>
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
          placeholder="订单状态"
          clearable
          style="width: 180px"
          @change="handleSearch"
        >
          <el-option label="未接单" value="未接单" />
          <el-option label="已接单" value="已接单" />
          <el-option label="已支付" value="已支付" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已完成" value="已完成" />
          <el-option label="已评价" value="已评价" />
          <el-option label="已取消" value="已取消" />
          <el-option label="拒绝接单" value="拒绝接单" />
        </el-select>

        <el-input
          v-model="searchForm.ordernum"
          placeholder="输入订单编号搜索"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
      </div>

    <!-- 订单列表 -->
    <div class="order-list" v-if="orderList.length > 0">
      <div class="order-card" v-for="order in orderList" :key="order.id">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-info-row">
            <span class="order-label">订单编号：</span>
            <span class="order-value">{{ order.ordernum || '待生成' }}</span>
      </div>
          <div 
            class="order-status" 
            :style="{ 
              color: statusConfig[order.statu]?.color, 
              backgroundColor: statusConfig[order.statu]?.bgColor 
            }"
          >
            <span class="status-icon">{{ statusConfig[order.statu]?.icon }}</span>
            <span>{{ order.statu }}</span>
          </div>
        </div>

        <!-- 订单主体 -->
        <div class="order-body">
          <div class="worker-info">
            <div class="worker-avatar-wrapper">
              <img 
                v-if="order.vitae?.img" 
                :src="order.vitae?.img" 
                :alt="order.vitae?.name"
                class="worker-avatar"
              />
              <div v-else class="worker-avatar-placeholder">
                {{ order.vitae?.name?.charAt(0) || '?' }}
              </div>
            </div>
            <div class="worker-detail">
              <h4 class="worker-name">{{ order.vitae?.name || '陪诊师' }}</h4>
              <p class="worker-desc">{{ order.vitae?.des || '暂无简介' }}</p>
            </div>
          </div>

          <div class="order-details">
            <div class="detail-item">
              <span class="detail-icon">📍</span>
              <div class="detail-content">
                <span class="detail-label">预约地址</span>
                <span class="detail-value">{{ order.address }}</span>
              </div>
            </div>
            <div class="detail-item">
              <span class="detail-icon">🕐</span>
              <div class="detail-content">
                <span class="detail-label">预约时间</span>
                <span class="detail-value">{{ order.time }}</span>
              </div>
            </div>
            <div class="detail-item" v-if="order.stime && order.etime">
              <span class="detail-icon">⏰</span>
              <div class="detail-content">
                <span class="detail-label">开始时间</span>
                <span class="detail-value">{{ order.stime }}</span>
              </div>
            </div>
            <div class="detail-item" v-if="order.stime && order.etime">
              <span class="detail-icon">⏱️</span>
              <div class="detail-content">
                <span class="detail-label">结束时间</span>
                <span class="detail-value">{{ order.etime }}</span>
              </div>
            </div>
            <div class="detail-item" v-if="order.des">
              <span class="detail-icon">📝</span>
              <div class="detail-content">
                <span class="detail-label">其他需求</span>
                <span class="detail-value">{{ order.des }}</span>
              </div>
            </div>
            <div class="detail-item" v-if="order.total">
              <span class="detail-icon">💰</span>
              <div class="detail-content">
                <span class="detail-label">实际费用</span>
                <span class="detail-value price">¥{{ order.total }}</span>
              </div>
            </div>
            <div class="detail-item" v-if="order.statu === '已评价'">
              <span class="detail-icon">⭐</span>
              <div class="detail-content">
                <span class="detail-label">我的评价</span>
                <el-rate v-model="order.rate" disabled show-score />
              </div>
            </div>
          </div>
        </div>

        <!-- 订单底部操作栏 -->
        <div class="order-footer">
          <span class="create-time">创建时间：{{ order.createTime || '-' }}</span>
          <div class="action-buttons">
            <el-button size="small" @click="handleViewDetail(order)">
              查看详情
            </el-button>
            <el-button 
              v-if="order.statu === '未接单'" 
              type="danger" 
              size="small" 
              plain
              @click="handleCancelOrder(order.id)"
            >
              取消订单
            </el-button>
            <el-button 
              v-if="order.statu === '已完成' && !order.alinum" 
              type="success" 
              size="small"
              @click="handlePayment(order.id)"
            >
              立即支付
            </el-button>
            <el-button 
              v-if="[ '已支付'].includes(order.statu) && order.statu !== '已评价'"
              type="primary" 
              size="small"
              @click="handleOpenRate(order)"
            >
              评价服务
            </el-button>
            <el-button 
              v-if="!['已取消', '拒绝接单'].includes(order.statu)"
              type="warning" 
              size="small"
              plain
              @click="handleComplaint(order)"
            >
              投诉
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-else
      description="暂无预约记录"
      class="empty-state"
    >
      <el-button type="primary" @click="$router.push('/front/vitae')">
        立即预约陪诊服务
      </el-button>
    </el-empty>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="orderList.length > 0">
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

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="detail-dialog" v-if="currentOrder">
        <div class="detail-section">
          <h4 class="section-title">📋 订单信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">订单编号：</span>
              <span class="value">{{ currentOrder.ordernum || '待生成' }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态：</span>
              <span 
                class="value status-tag"
                :style="{ 
                  color: statusConfig[currentOrder.statu]?.color,
                  backgroundColor: statusConfig[currentOrder.statu]?.bgColor
                }"
              >
                {{ statusConfig[currentOrder.statu]?.icon }} {{ currentOrder.statu }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ currentOrder.createTime || '-' }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.alinum">
              <span class="label">支付流水号：</span>
              <span class="value">{{ currentOrder.alinum }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">👨‍⚕️ 陪诊师信息</h4>
          <div class="worker-detail-info">
            <div class="detail-avatar-wrapper">
              <img 
                v-if="currentOrder.vitae?.img" 
                :src="currentOrder.vitae?.img" 
                :alt="currentOrder.vitae?.name"
                class="detail-avatar"
              />
              <div v-else class="detail-avatar-placeholder">
                {{ currentOrder.vitae?.name?.charAt(0) || '?' }}
              </div>
            </div>
            <div class="worker-info-text">
              <h3>{{ currentOrder.vitae?.name || '陪诊师' }}</h3>
              <p>{{ currentOrder.vitae?.des || '暂无简介' }}</p>
              <p v-if="currentOrder.vitae?.phone">联系电话：{{ currentOrder.vitae?.phone }}</p>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">📝 预约详情</h4>
          <div class="info-grid">
            <div class="info-item full-width">
              <span class="label">预约地址：</span>
              <span class="value">{{ currentOrder.address }}</span>
            </div>
            <div class="info-item">
              <span class="label">预约时间：</span>
              <span class="value">{{ currentOrder.time }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.stime">
              <span class="label">开始时间：</span>
              <span class="value">{{ currentOrder.stime }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.etime">
              <span class="label">结束时间：</span>
              <span class="value">{{ currentOrder.etime }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.total">
              <span class="label">实际费用：</span>
              <span class="value price">¥{{ currentOrder.total }}</span>
            </div>
            <div class="info-item full-width" v-if="currentOrder.des">
              <span class="label">其他需求：</span>
              <span class="value">{{ currentOrder.des }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentOrder.statu === '已评价'">
          <h4 class="section-title">⭐ 我的评价</h4>
          <div class="rate-content">
            <el-rate v-model="currentOrder.rate" disabled show-score />
            <p v-if="currentOrder.content" class="rate-text">{{ currentOrder.content }}</p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="rateVisible"
      title="评价服务"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="rate-dialog">
        <el-form :model="rateForm" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="订单编号">
                <el-input v-model="rateForm.ordernum" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预约时间">
                <el-input v-model="rateForm.time" disabled />
              </el-form-item>
    </el-col>
  </el-row>

          <el-form-item label="预约地址">
            <el-input v-model="rateForm.address" disabled />
          </el-form-item>

          <el-divider />

          <el-form-item label="服务评分" required>
            <el-rate 
              v-model="rateForm.rate" 
              show-text 
              :texts="['极差', '失望', '一般', '满意', '非常满意']"
            />
          </el-form-item>

          <el-form-item label="评价内容">
            <el-input
              v-model="rateForm.content"
              type="textarea"
              :rows="5"
              placeholder="请描述您对本次服务的评价"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="rateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRate">提交评价</el-button>
      </template>
    </el-dialog>
    </div>
  </div>
</template>

<style scoped lang="scss">
.order-page {
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

.order-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.order-card {
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

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e5e7eb;
  
  .order-info-row {
    .order-label {
      font-size: 13px;
      color: #6b7280;
      margin-right: 0.5rem;
    }
    
    .order-value {
      font-size: 14px;
      font-weight: 600;
      color: #1f2937;
      font-family: 'Courier New', monospace;
    }
  }
  
  .order-status {
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

.order-body {
  padding: 1.25rem;
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
    
    .worker-desc {
      font-size: 14px;
      color: #6b7280;
      margin: 0;
      line-height: 1.6;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
}

.order-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
  
  .detail-item {
    display: flex;
    gap: 0.75rem;
    align-items: flex-start;
    
    .detail-icon {
      font-size: 18px;
      flex-shrink: 0;
      margin-top: 2px;
    }
    
    .detail-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      
      .detail-label {
        font-size: 12px;
        color: #9ca3af;
      }
      
      .detail-value {
        font-size: 14px;
        color: #1f2937;
        font-weight: 500;
        
        &.price {
          color: #ef4444;
          font-size: 18px;
          font-weight: 700;
        }
      }
    }
  }
}

.order-footer {
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
        
        &.price {
          color: #ef4444;
          font-size: 18px;
          font-weight: 700;
        }
        
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
  
  .rate-content {
    .rate-text {
      margin-top: 1rem;
      padding: 1rem;
      background: #f9fafb;
      border-radius: 0.5rem;
      font-size: 14px;
      color: #1f2937;
      line-height: 1.6;
    }
  }
}

// 评价对话框样式
.rate-dialog {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #374151;
  }
  
  :deep(.el-input.is-disabled .el-input__inner) {
    background-color: #f9fafb;
    color: #6b7280;
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
  
  .order-details {
    grid-template-columns: 1fr;
  }
  
  .detail-dialog .info-grid {
    grid-template-columns: 1fr;
    
    .info-item.full-width {
      grid-column: 1;
    }
  }
}
</style>

<script setup >
import { reactive, ref } from 'vue'
  import {
    Phone,
    Location,
    User,
    Calendar,
    Search,
    Refresh
  } from '@element-plus/icons-vue'
  import request from '@/utils/request'
  import {ElMessage} from 'element-plus'
  import {useRoute} from "vue-router";
  import router from '@/router'
  import { areaList } from '@vant/area-data'
  
  const $router = router
  const $route = useRoute()
  
  // 当前页数
  const pageNum = ref(1)
  // 每页展示量
  const pageSize = ref(9)
  // 分页数据总数
  const total = ref(0)
  // 表格数据
  const tableData = ref([])
  // 系统公告数据
  const announcements = ref([])
  const activeNames = ref(['1'])
  
  //定义查询值
  const  name=ref('')
  const  address=ref([])

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

  // 加载公告数据
  const loadAnnouncements = async () => {
    try {
      const res = await request.get('/notice')
      announcements.value = res.data.map(item => ({
        id: item.id,
        title: item.name || item.title,
        content: item.content || item.des,
        date: item.createTime ? item.createTime.substring(0, 10) : ''
      }))
      
      // 默认展开第一条公告
      if (announcements.value.length > 0) {
        activeNames.value = [announcements.value[0].id.toString()]
      }
    } catch (error) {
      console.log('加载公告失败', error)
    }
  }

  // 分页查询方法
  const load = async () => {
    const res = await request.get('/vitae/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        name: name.value,
        address: Array.isArray(address.value) && address.value.length > 0 ? address.value.toString() : ''
      }
    })
    pageNum.value = res.data.current
    pageSize.value = res.data.size
    total.value = res.data.total
    tableData.value = res.data.records
  }
  
  // 加载页面初始化调用load方法
  load()
  loadAnnouncements()
  
  // 查看简历详情
  const handleViewDetail = (id) => {
    $router.push(`/front/vitae/detail?id=${id}`)
  }

  // 清空查询数据重置方法
  const handleReset = () => {
    name.value = ''
    address.value = []
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

</script>
<template>
  <div class="vitae-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <h1>👨‍⚕️ 陪诊师团队</h1>
          <p>专业的医院陪护服务，让您安心就医</p>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <div class="content-wrapper">
        <!-- 左侧简历列表 -->
        <div class="left-section">
          <!-- 搜索筛选 -->
          <div class="search-box">
            <el-input 
              v-model="name" 
              placeholder="搜索陪诊师姓名"
              clearable
              @change="load"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <el-cascader
              v-model="address"
              :options="options"
              :props="{ checkStrictly: false }"
              placeholder="选择区域"
              clearable
              @change="load"
              class="area-select"
            />
            
            <el-button type="primary" @click="load" :icon="Search">搜索</el-button>
            <el-button @click="handleReset" :icon="Refresh">重置</el-button>
          </div>

          <!-- 简历列表 -->
          <div class="vitae-list">
            <div 
              v-for="vitae in tableData" 
              :key="vitae.id" 
              class="vitae-item"
              @click="handleViewDetail(vitae.id)"
            >
              <div class="vitae-avatar">
                <img :src="vitae.img" :alt="vitae.name" @error="$event.target.src='/logo.png'" />
              </div>
              
              <div class="vitae-content">
                <div class="vitae-header">
                  <h3 class="vitae-name">{{ vitae.name }}</h3>
                  <span class="vitae-sex">{{ vitae.sex }}</span>
                </div>
                
                <p class="vitae-desc">{{ vitae.des }}</p>
                
                <div class="vitae-info">
                  <div class="info-item">
                    <el-icon><Phone /></el-icon>
                    <span>{{ vitae.phone }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Location /></el-icon>
                    <span>{{ formatRegion(vitae.address) }}</span>
                  </div>
                </div>
              </div>
              
              <div class="vitae-action">
                <el-button type="primary" size="small">查看详情</el-button>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="tableData.length === 0" class="empty-state">
              <el-icon class="empty-icon"><User /></el-icon>
              <p>暂无陪诊师信息</p>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
              v-model:current-page="pageNum"
              :page-size="pageSize"
              :page-sizes="[9, 18, 36, 72]"
              layout="total, sizes, prev, pager, next"
              :total="Number(total)"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              background
            />
          </div>
        </div>

        <!-- 右侧系统公告 -->
        <div class="right-section">
          <div class="announcement-box">
            <div class="announcement-header">
              <h3>📢 系统公告</h3>
            </div>
            
            <el-collapse v-model="activeNames" class="announcement-collapse" v-if="announcements.length > 0">
              <el-collapse-item 
                v-for="announcement in announcements" 
                :key="announcement.id"
                :name="announcement.id.toString()"
              >
                <template #title>
                  <div class="collapse-title">
                    <span class="title-text">{{ announcement.title }}</span>
                    <span class="title-date">{{ announcement.date }}</span>
                  </div>
                </template>
                <div class="announcement-content">
                  {{ announcement.content }}
                </div>
              </el-collapse-item>
            </el-collapse>
            
            <div v-else class="no-announcement">
              <p>暂无公告</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.vitae-list-page {
  min-height: calc(100vh - 4rem);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
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
  padding: 2rem;
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

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 2rem;
}

/* 左侧简历列表 */
.left-section {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

/* 搜索框 */
.search-box {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.search-input {
  flex: 1;
}

.area-select {
  width: 240px;
}

/* 简历列表 */
.vitae-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.vitae-item {
  display: flex;
  flex-direction: column;
  padding: 1.5rem;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  transition: all 0.3s ease;
  cursor: pointer;
}

.vitae-item:hover {
  background: white;
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  transform: translateY(-4px);
}

.vitae-avatar {
  margin: 0 auto 1rem;
}

.vitae-avatar img {
  width: 120px;
  height: 160px;
  object-fit: cover;
  border-radius: 0.5rem;
  border: 2px solid #e5e7eb;
}

.vitae-content {
  flex: 1;
  text-align: center;
}

.vitae-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.vitae-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.vitae-sex {
  padding: 0.25rem 0.5rem;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border-radius: 1rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.vitae-desc {
  color: #6b7280;
  font-size: 0.875rem;
  line-height: 1.6;
  margin: 0 0 1rem 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-align: left;
}

.vitae-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.8rem;
}

.info-item .el-icon {
  color: #3b82f6;
}

.vitae-action {
  display: flex;
  justify-content: center;
}

.vitae-action .el-button {
  width: 100%;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 4rem 2rem;
}

.empty-icon {
  font-size: 4rem;
  color: #d1d5db;
  margin-bottom: 1rem;
}

.empty-state p {
  color: #9ca3af;
  font-size: 1rem;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

/* 右侧公告 */
.right-section {
  position: sticky;
  top: 2rem;
  height: fit-content;
}

.announcement-box {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.announcement-header {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f3f4f6;
}

.announcement-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.announcement-collapse {
  border: none;
}

:deep(.el-collapse-item) {
  margin-bottom: 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  overflow: hidden;
}

:deep(.el-collapse-item__header) {
  background: #f9fafb;
  padding: 1rem 1.25rem;
  border: none;
  font-weight: 500;
}

:deep(.el-collapse-item__header.is-active) {
  background: #eff6ff;
  color: #3b82f6;
}

:deep(.el-collapse-item__wrap) {
  border: none;
}

:deep(.el-collapse-item__content) {
  padding: 1rem 1.25rem;
}

.collapse-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 1rem;
}

.title-text {
  font-weight: 500;
  color: #374151;
  flex: 1;
}

.title-date {
  font-size: 0.875rem;
  color: #9ca3af;
  margin-left: 1rem;
}

.announcement-content {
  color: #6b7280;
  font-size: 0.9375rem;
  line-height: 1.7;
}

.no-announcement {
  text-align: center;
  padding: 2rem;
  color: #9ca3af;
}

.no-announcement p {
  margin: 0;
  font-size: 0.9375rem;
}

/* 响应式 */
@media (max-width: 1200px) {
  .vitae-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1024px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .right-section {
    position: static;
  }
}

@media (max-width: 768px) {
  .main-container {
    padding: 1rem;
  }

  .header-content {
    padding: 1.5rem 1rem;
  }

  .header-title h1 {
    font-size: 1.5rem;
  }

  .search-box {
    flex-direction: column;
  }

  .area-select {
    width: 100%;
  }

  .vitae-list {
    grid-template-columns: 1fr;
  }
}
</style>

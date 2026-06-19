<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import router from '@/router'

// 轮播图数据
const bannerList = ref([])
// 分类列表
const categoryList = ref([])
// 文章列表
const articleList = ref([])
// 当前页数
const pageNum = ref(1)
// 每页展示量
const pageSize = ref(9)
// 分页数据总数
const total = ref(0)
// 搜索关键词
const searchKeyword = ref('')
// 选中的分类
const selectedCategory = ref('')

// 加载数据
const load = async () => {
  // 加载轮播图
  const bannerRes = await request.get('/banner')
  bannerList.value = bannerRes.data

  // 加载分类
  const categoryRes = await request.get('/category')
  categoryList.value = categoryRes.data

  // 加载文章
  const articleRes = await request.get('/article/page', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: searchKeyword.value,
      categoryId: selectedCategory.value
    }
  })
  articleList.value = articleRes.data.records
  total.value = articleRes.data.total
  pageNum.value = articleRes.data.current
}

// 初始化加载
load()

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  load()
}

// 分类筛选
const handleCategoryChange = (categoryId) => {
  selectedCategory.value = categoryId
  pageNum.value = 1
  load()
}

// 翻页
const handleCurrentChange = (current) => {
  pageNum.value = current
  load()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 查看文章详情
const viewArticle = (id) => {
  router.push({
    path: '/front/detail',
    query: { id: id }
  })
}

// 点击轮播图
const handleBannerClick = (url) => {
  if (url) {
    window.open(url)
  }
}
</script>
<template>
  <div class="front-home">
    <!-- 轮播图区域 -->
    <div class="banner-section">
      <el-carousel height="600px" :interval="5000">
        <el-carousel-item v-for="banner in bannerList" :key="banner.id">
          <div class="banner-item" :style="{ backgroundImage: `url(${banner.img})` }" @click="handleBannerClick(banner.url)">
            <div class="banner-overlay"></div>
            <div class="banner-content">
              <h2 class="banner-title">{{ banner.name }}</h2>
              <p class="banner-desc">{{ banner.des }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <div class="container">
        <!-- 搜索和分类筛选区域 -->
        <div class="filter-section">
          <!-- 健康科普标题 -->
          <div class="section-title">
            <h2>📚 健康科普</h2>
            <p>了解健康知识，守护美好生活！</p>
          </div>
          
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索健康文章..."
              size="large"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button :icon="Search" @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>

          <!-- 分类标签 -->
          <div class="category-tabs">
            <div
              class="category-item"
              :class="{ active: selectedCategory === '' }"
              @click="handleCategoryChange('')"
            >
              <span class="category-icon">📚</span>
              <span class="category-name">全部</span>
            </div>
            <div
              v-for="category in categoryList"
              :key="category.id"
              class="category-item"
              :class="{ active: selectedCategory === category.id }"
              @click="handleCategoryChange(category.id)"
            >
              <span class="category-name">{{ category.name }}</span>
            </div>
          </div>
        </div>

        <!-- 文章列表 -->
        <div class="article-grid">
          <div
            v-for="article in articleList"
            :key="article.id"
            class="article-card"
            @click="viewArticle(article.id)"
          >
            <div class="article-image">
              <img :src="article.img" :alt="article.name" />
              <div class="article-category-badge" v-if="article.category">
                {{ article.category.name }}
              </div>
            </div>
            <div class="article-content">
              <h3 class="article-title">{{ article.name }}</h3>
              <p class="article-desc">{{ article.des }}</p>
              <div class="article-footer">
                <span class="read-more">阅读全文 →</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="articleList.length === 0" class="empty-state">
          <el-empty description="暂无文章">
            <el-button type="primary" @click="handleCategoryChange('')">查看全部</el-button>
          </el-empty>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handleCurrentChange"
            background
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.front-home {
  width: 100%;
  min-height: calc(100vh - 4rem);
  background: #f5f7fa;
}

/* 轮播图样式 */
.banner-section {
  width: 100%;
}

.banner-item {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.6));
}

.banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
  padding: 20px;
}

.banner-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.banner-desc {
  font-size: 1.2rem;
  max-width: 600px;
  margin: 0 auto;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
}

/* 主内容区 */
.main-content {
  padding: 3rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 2.5rem;
}

.section-title {
  text-align: center;
  margin-bottom: 2rem;
}

.section-title h2 {
  font-size: 2rem;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.section-title p {
  font-size: 1rem;
  color: #6b7280;
}

.search-box {
  margin-bottom: 2rem;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.category-item {
  padding: 0.75rem 1.5rem;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
}

.category-item:hover {
  border-color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.category-item.active {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-color: #3b82f6;
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.category-icon {
  font-size: 1.1rem;
}

/* 文章网格 */
.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.article-card {
  background: white;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.article-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.article-image {
  width: 100%;
  height: 220px;
  overflow: hidden;
  position: relative;
}

.article-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-card:hover .article-image img {
  transform: scale(1.1);
}

.article-category-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(59, 130, 246, 0.9);
  color: white;
  padding: 0.4rem 1rem;
  border-radius: 1rem;
  font-size: 0.85rem;
  font-weight: 500;
  backdrop-filter: blur(4px);
}

.article-content {
  padding: 1.5rem;
}

.article-title {
  font-size: 1.25rem;
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
  height: 4.8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

.read-more {
  color: #3b82f6;
  font-weight: 500;
  font-size: 0.95rem;
}

.article-card:hover .read-more {
  color: #2563eb;
}

/* 空状态 */
.empty-state {
  padding: 4rem 0;
  text-align: center;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 2rem 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .banner-title {
    font-size: 1.75rem;
  }

  .banner-desc {
    font-size: 1rem;
  }

  .article-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .category-tabs {
    gap: 0.5rem;
  }

  .category-item {
    padding: 0.6rem 1.2rem;
    font-size: 0.9rem;
  }
}
</style>

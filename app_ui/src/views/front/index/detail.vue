<script setup>
import {onBeforeMount, reactive, ref} from "vue";

const $route = router
const systemStore = useSystemStore()
const form = reactive({
  id: undefined,
  name: undefined,
  cid: undefined,
  img: undefined,
  file: undefined,
  des: undefined,
  content: undefined,
})
import {Delete, Download, PieChart, Star, View} from "@element-plus/icons-vue";
import router from "@/router";
import request from "@/utils/request";
import useSystemStore from "@/stores/system";
import {ElMessage} from "element-plus";

const aid = ref('')
onBeforeMount((async () => {
  if ($route.currentRoute.value.query.id) {
    aid.value = $route.currentRoute.value.query.id
    await load()
  }
}))
const handleCollet = async () => {
  const entity = {}
  entity.articleId = form.id
  const res = await request.post(`/collect`, entity)
  ElMessage({
    message: `${res.data}`,
    type: 'success',
    showClose: true,
  })
  await load()
}
const load = async () => {
  const res = await request.get(`/article/${$route.currentRoute.value.query.id}`)
  Object.assign(form, res.data)

  const rec= await request.get(`/comment/article/${$route.currentRoute.value.query.id}`)

  config.comments=rec.data
}
const currentIndex = ref(0)
const drawer=ref(false)
import emoji from '@/emoji/emoji'

import {UComment, UToast} from 'undraw-ui'

const config = reactive({
  user: systemStore.userInfo ,
  emoji: emoji,
  comments: [],
  showLevel: false,
  showHomeLink: false,
  showAddress: false,
  show:{
    // 是否显示评论表单

    level: false ,// 是否显示等级
    likes: false ,// 是否点赞
    address: false,  // 是否显示地址
    homeLink: false, // 是否跳转个人主页地址
    reply: true // 是否显示回复按钮
  }
})
// 提交评论事件
const submit =async ({ content, parentId, files, finish }) => {
  console.log('提交评论: ' + content, parentId, files)

  const comment = {

    parentId: parentId?parentId:0,
    uid: config.user.id,
    content: content,
    aid:$route.currentRoute.value.query.id,
    user: {
      username: config.user.username,
      avatar: config.user.avatar
    },
    reply: null
  }
  await request.post(`/comment`,comment)

  await load()
  finish()
  UToast({ message: '评论成功!', type: 'info' })
  // setTimeout(() => {
  //   finish(comment)
  //   UToast({ message: '评论成功!', type: 'info' })
  // }, 200)
}
const remove = async (id) => {
  console.log('删除评论: ' + id)
  await request.delete(`/comment/${id}`)
  ElMessage.success({
    message:'操作成功',
    type:'success',
    showClose:false
  })
  await load()
}
const dowload=async (url)=>{
  window.open(url)
}
</script>

<template>
  <div class="article-detail-page">
    <div class="detail-container">
      <!-- 顶部操作栏 -->
      <div class="top-bar">
        <div class="back-button" @click="$router.go(-1)">
          <span class="back-icon">←</span>
          <span>返回</span>
        </div>
        <div class="actions">
          <el-button 
            circle 
            :icon="Star" 
            size="large"
            :type="form.isCollect ? 'warning' : 'primary'" 
            @click="handleCollet"
          >
          </el-button>
        </div>
      </div>

      <!-- 文章内容卡片 -->
      <div class="article-card">
        <!-- 文章头部 -->
        <div class="article-header">
          <h1 class="article-title">{{ form.name }}</h1>
          <div class="article-meta" v-if="form.category">
            <span class="meta-item category">
              <span class="icon">📁</span>
              {{ form.category.name }}
            </span>
          </div>
        </div>

        <!-- 文章封面 -->
        <div class="article-cover" v-if="form.img">
          <img :src="form.img" :alt="form.name" />
        </div>

        <!-- 文章简介 -->
        <div class="article-description" v-if="form.des">
          <p>{{ form.des }}</p>
        </div>

        <!-- 文章正文 -->
        <div class="article-content" v-html="form.content"></div>
      </div>

      <!-- 评论区 -->
    <u-comment :config="config"  @submit="submit" >
      <!-- <template>导航栏卡槽</template> -->
      <!-- <template #info>用户信息卡槽</template> -->
      <!-- <template #card>用户信息卡片卡槽</template> -->
      <template #operate="scope">
        <el-button :icon="Delete" size="small"
                   v-if="scope.uid==systemStore.userInfo.id"
                   @click="remove(scope.id)"
        >

        </el-button>

      </template>
    </u-comment>
    </div>
  </div>
</template>

<style scoped>
.article-detail-page {
  min-height: calc(100vh - 4rem);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
  padding: 2rem 0;
}

.detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

/* 顶部操作栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
  padding: 1rem 1.5rem;
  background: white;
  border-radius: 1rem 1rem 0 0;
  border-bottom: 1px solid #e5e7eb;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  color: #3b82f6;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
}

.back-button:hover {
  background: #eff6ff;
  color: #2563eb;
}

.back-icon {
  font-size: 1.5rem;
  font-weight: bold;
}

/* 文章卡片 */
.article-card {
  background: white;
  border-radius: 0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  padding: 3rem;
  margin-bottom: 2rem;
}

/* 文章头部 */
.article-header {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #f3f4f6;
}

.article-title {
  font-size: 2.25rem;
  font-weight: bold;
  color: #1f2937;
  line-height: 1.4;
  margin-bottom: 1rem;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  color: #6b7280;
}

.meta-item.category {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  font-weight: 500;
}

.meta-item .icon {
  font-size: 1.1rem;
}

/* 文章封面 */
.article-cover {
  margin-bottom: 2rem;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.article-cover img {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
}

/* 文章简介 */
.article-description {
  background: #eff6ff;
  border-left: 4px solid #3b82f6;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border-radius: 0.5rem;
}

.article-description p {
  color: #374151;
  font-size: 1.05rem;
  line-height: 1.8;
  margin: 0;
}

/* 文章正文 */
.article-content {
  font-size: 1.05rem;
  line-height: 1.9;
  color: #374151;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  color: #1f2937;
  font-weight: 600;
  margin-top: 2rem;
  margin-bottom: 1rem;
}

.article-content :deep(h2) {
  font-size: 1.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #e5e7eb;
}

.article-content :deep(h3) {
  font-size: 1.4rem;
  color: #3b82f6;
}

.article-content :deep(p) {
  margin-bottom: 1.2rem;
  text-align: justify;
}

.article-content :deep(strong) {
  color: #1f2937;
  font-weight: 600;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  margin-left: 2rem;
  margin-bottom: 1.2rem;
}

.article-content :deep(li) {
  margin-bottom: 0.5rem;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 0.5rem;
  margin: 1.5rem 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.article-content :deep(blockquote) {
  border-left: 4px solid #3b82f6;
  padding-left: 1rem;
  margin: 1.5rem 0;
  color: #6b7280;
  font-style: italic;
}

.article-content :deep(code) {
  background: #f3f4f6;
  padding: 0.2rem 0.4rem;
  border-radius: 0.25rem;
  font-size: 0.9em;
  color: #ef4444;
}

/* 响应式 */
@media (max-width: 768px) {
  .article-card {
    padding: 1.5rem;
  }

  .article-title {
    font-size: 1.75rem;
  }

  .article-content {
    font-size: 1rem;
  }

  .detail-container {
    padding: 0 1rem;
  }
}
</style>
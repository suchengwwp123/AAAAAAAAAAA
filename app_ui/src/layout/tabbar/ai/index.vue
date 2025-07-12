<script setup>
import {onBeforeMount, onBeforeUnmount, onMounted, onUnmounted, ref} from 'vue';
import {SystemSumIcon} from 'tdesign-icons-vue-next';
import pinia from "@/stores/store";
import useSystemStore from "@/stores/system";
import {stopStream, streamRequest,} from "@/utils/useStreamRequest";
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";

const systemStore = useSystemStore(pinia)
const loading = ref(false);
const isStreamLoad = ref(false)
const allowToolTip = ref(false);
const chatSenderRef = ref(null);
const inputValue = ref('');
const selectOptions = [
  {
    label: 'Deepseek',
    value: 'DeepSeek',
  },
  {
    label: 'Ollama',
    value: 'Ollama',
  }


];
const props= defineProps(['aidrawer'])
const emit = defineEmits(['changeaidrawer'])
const selectValue = ref({
  label: 'Deepseek',
  value: 'Deepseek',
});
const isChecked = ref(false);
// 获取当前时间
const getCurrentTime = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0'); // 月份从0开始
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}
const messages = ref([])
// 开启/关闭深度思考
const checkClick = () => {
  isChecked.value = !isChecked.value;
};
// 模拟消息发送
const inputEnter = async () => {
// 先展示本地，再发送
  const message = {
    avatar: systemStore.userInfo.avatar,
    name: systemStore.userInfo.username,
    datetime: getCurrentTime(),
    content: inputValue.value,
    role: 'user',

  }
  messages.value.unshift(message)
  await saveMessages(message)
  loading.value = true
  isStreamLoad.value = true
  messages.value.unshift({

    avatar: '/logo.png',
    name: 'authority-ai',
    datetime: getCurrentTime(),
    content: ``,
    role: 'assistant',
  })
  const prompt = inputValue.value;
  inputValue.value = ''
  await streamRequest(
      selectValue.value.value,
      prompt,
      isChecked.value,
      (delta) => {
        loading.value = false;
        messages.value[0].content += delta
      },
      (finalText) => {
        console.log('✅ 全部内容接收完成')
        isStreamLoad.value = false;
        saveMessages(messages.value[0])
      }
  )
};
// 停止接收方法
const onStop = async () => {
  loading.value = false;
  isStreamLoad.value = false;
  stopStream()
  await saveMessages(messages.value[0])
};
// 保存会话记录
const saveMessages = async (chatMessage) => {
  await request.post('/history', chatMessage)
}
// 调取聊天记录
const load = async () => {
  const res = await request.get('/history')
  messages.value = res.data
  if (messages.value.length === 0) {
    const message = {
      avatar: '/logo.png',
      name: 'authority-ai',
      datetime: getCurrentTime(),
      content: '欢迎来到authority-ai小助手，请在此咨询您的问题',
      role: 'assistant',
    }
    messages.value.unshift(message)
    await saveMessages(message)
  }
}
//删除聊天记录
const deleteMessages = async () => {
  await request.delete(`/history/${systemStore.userInfo.id}`)
  ElMessage({
    showClose: true,
    message: '清空历史记录成功',
    type: 'success'
  })
  await load()
}
const handleBeforeClose = async () => {
  ElMessageBox.confirm(
      '确认退出ai助手?',
      '系统提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(async () => {

        if (isStreamLoad.value){
          stopStream()
          await saveMessages(messages.value[0])
          isStreamLoad.value=false
          loading.value=false
        }
        emit('changeaidrawer', false)


      })
      .catch(() => {

      })

}
defineExpose({
  load
})
const drawerSize = ref('900px')

const updateDrawerSize = () => {
  const width = window.innerWidth
  if (width <= 768) {
    drawerSize.value = '100%'
  } else if (width <= 1024) {
    drawerSize.value = '80%'
  } else {
    drawerSize.value = '900px'
  }
}

onMounted(() => {
  updateDrawerSize()
  window.addEventListener('resize', updateDrawerSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateDrawerSize)
})
</script>
<script>
export default {
  name: 'AiChat',
}
</script>
<template>
  <el-drawer
      title="AI助手"
      :model-value="aidrawer"
      append-to-body
      :size="drawerSize"
      :before-close="handleBeforeClose"

  >
    <div class="chat-container"
         v-if="props.aidrawer"
    >
      <div class="chat-body">
        <t-chat
            @clear="deleteMessages"
            :reverse="true"
            :text-loading="loading"
            :data="messages"
            class="chat-main"
            scrollToBottom
        >
          <!-- 显示用户名 -->
          <template #name="{ item }">
            {{ item.name }}
          </template>

          <!-- 显示头像 -->
          <template #avatar="{ item }">
            <t-avatar size="large" shape="circle" :image="item.avatar"/>
          </template>

          <!-- 显示时间 -->
          <template #datetime="{ item }">
            {{ item.datetime }}
          </template>

          <!-- 显示内容 -->
          <template #content="{ item }">
            <t-chat-content :content="item.content"/>
          </template>

          <!-- 输入框 -->
          <template #footer>
            <t-chat-sender
                ref="chatSenderRef"
                v-model="inputValue"
                class="chat-sender"
                :textarea-props="{ placeholder: '请输入消息...' }"
                :loading="loading"
                @send="inputEnter"
                @stop="onStop"
                :stop-disabled="isStreamLoad"

            >
              <template #suffix="{ renderPresets }">

                <component :is="renderPresets([])"/>
              </template>
              <template #prefix>
                <div class="model-select">
                  <t-tooltip v-model:visible="allowToolTip" content="切换模型" trigger="hover">
                    <t-select
                        v-model="selectValue"
                        :options="selectOptions"
                        value-type="object"
                        @focus="allowToolTip = false"
                    />
                  </t-tooltip>
                  <t-button class="check-box" :class="{ 'is-active': isChecked }" variant="text" @click="checkClick">
                    <SystemSumIcon/>
                    <span>深度思考</span>
                  </t-button>
                </div>
              </template>
            </t-chat-sender>
          </template>
        </t-chat>
      </div>
    </div>
  </el-drawer>
</template>


<style lang="less">
.chat-container {
  width: 90%;
  max-width: 900px;
  height: 80vh;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}


.chat-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;

  // TDesign 内部消息容器类，确保它能滚动
  .t-chat__messages {
    flex: 1;
    overflow-y: auto;
    padding: 16px;

  }
}

.chat-sender {
  .btn {
    color: var(--td-text-color-disabled);
    border: none;

    &:hover {
      color: var(--td-brand-color-hover);
      background: none;
    }
  }

  .btn.t-button {
    height: var(--td-comp-size-m);
    padding: 0;
  }

  .model-select {
    display: flex;
    align-items: center;

    .t-select {
      width: 112px;
      height: var(--td-comp-size-m);
      margin-right: var(--td-comp-margin-s);

      .t-input {
        border-radius: 32px;
        padding: 0 15px;
      }

      .t-input.t-is-focused {
        box-shadow: none;
      }
    }

    .check-box {
      width: 112px;
      height: var(--td-comp-size-m);
      border-radius: 32px;
      border: 0;
      background: var(--td-bg-color-component);
      color: var(--td-text-color-primary);
      box-sizing: border-box;

      .t-button__text {
        display: flex;
        align-items: center;
        justify-content: center;

        span {
          margin-left: var(--td-comp-margin-xs);
        }
      }

      &.is-active {
        border: 1px solid var(--td-brand-color-focus);
        background: var(--td-brand-color-light);
        color: var(--td-text-color-brand);
      }
    }
  }
}
@media screen and (max-width: 768px) {
  .chat-container {
    width: 100%;
    padding: 0 10px;
  }
}
</style>

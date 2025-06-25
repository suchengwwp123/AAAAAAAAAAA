<script setup>
import {onBeforeUnmount, onMounted, ref} from 'vue'
import {Delete, Download, Edit, InfoFilled, Plus, Refresh, Search} from '@element-plus/icons-vue'
import request from '@/utils/request'
import {ElMessage} from 'element-plus'
import UserForm from '@/views/system/user/form/index.vue'
import downloadExcel from '@/utils/downloads'
import {useWebSocket} from "@/utils/websocket";

// 名称
const name = ref('')
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
// 获取子组件对象
const childComp = ref(null)
const ws = useWebSocket('ONLINEUSER_TAKE') // ✅ 只传频道名

onMounted(() => {
  ws.connect()
  ws.onMessage( (msg) => {

    load()
  })
})

onBeforeUnmount(() => {
  ws.disconnect()
})


// 单个删除
const handleLogout = async (id) => {
  await request.put(`/auth/logout/${id}`)
  ElMessage({
    showClose: true,
    message: '下线成功',
    type: 'success'
  })
  await load()
}



// 分页查询
const load = async () => {
  const res = await request.get('/user/onlinepage', {
    params: {
      name: name.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  })
  pageNum.value = res.data.current
  pageSize.value = res.data.size
  total.value = res.data.total
  tableData.value = res.data.records
}
// 加载页面初始化调用load方法
load()
// 清空查询数据重置
const handleReset = () => {
  name.value = ''
  load()
}
// 修改每页展示的数据量
const handleSizeChange = (size) => {
  pageSize.value = size
  load()
}
// 翻页方法
const handleCurrentChange = (current) => {
  pageNum.value = current
  load()
}
// 多选按钮处理
const handleSelectionChange = (val) => {
  multipleSelection.value = val
  disabled.value = val.length === 0
}
//子组件传值给父组件,更改显示状态
const handlechangeDialog = (value) => {
  dialogVisible.value = value
  load()
}
</script>
<template>
  <!--  编辑弹框-->


  <el-row class="page">
    <!--  分页查询表单按钮-->
    <el-col
        v-permission="'sys:online:search'"
    >
      <el-form :inline="true">
        <el-form-item>
          <el-input v-model="name" placeholder="点击输入用户名称"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button type="warning" :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-col>

    <!--  分页页面-->
    <el-col>
      <el-table
          :data="tableData"
          class="page-table"

          :header-cell-style="{ background: '#eef1f6', color: '#606266' }"
          @selection-change="handleSelectionChange"
      >

        <el-table-column prop="id" label="会话标识" align="center" show-overflow-tooltip/>
        <el-table-column prop="username" label="账户名" align="center" show-overflow-tooltip/>
        <el-table-column prop="device" label="登录类型" align="center" show-overflow-tooltip/>
        <el-table-column prop="loginTime" label="登录时间" align="center" show-overflow-tooltip/>

        <el-table-column label="操作" align="center"
                         width="240"
        >
          <template #default="scope">
            <div class="page-table-editout--layout">

              <div v-permission="'sys:user:delete'">
                <el-popconfirm
                    confirm-button-text="确定"
                    cancel-button-text="取消"
                    :icon="InfoFilled"
                    icon-color="#626AEF"
                    title="确认要下线此用户吗？"
                    @confirm="handleLogout(scope.row.loginId)"
                    @cancel="load"
                >
                  <template #reference>
                    <el-button type="danger" :icon="Delete" size="small">下线</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="page-pagination">
        <el-pagination
            :current-page="pageNum"
            :page-size="pageSize"
            :page-sizes="[10, 20, 30, 50, 100, 500, 1000]"
            small="small"
            layout="total, sizes, prev, pager, next, jumper"
            :total="Number(total)"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-col>
  </el-row>
</template>

<style scoped lang="scss">


</style>
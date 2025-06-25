<script setup >
import { ref } from 'vue'
import { Delete, Download, Edit, InfoFilled, Plus, Refresh, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import UserForm from '@/views/system/user/form/index.vue'
import downloadExcel from '@/utils/downloads'
// 名称
const name = ref('')
// 手机号
const phone=ref('')
// 邮箱号
const email=ref('')
// 当前状态
const statu=ref('')
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
//新增
const handleAdd = async () => {
  dialogVisible.value = true
  await childComp.value.handleGetTrees()
}
// 修改
const handleUpdate = async (id) => {
  dialogVisible.value = true
  await childComp.value.handleGetForm(id)
}
// 单个删除
const handleDel = async (id) => {
  await request.delete(`/user/${id}`)
  ElMessage({
    showClose: true,
    message: '删除成功',
    type: 'success'
  })
  await load()
}
// 批量删除
const handleBatchDel = async () => {
  const ids = []
  multipleSelection.value.forEach((row) => {
    ids.push(row.id)
  })
  await request.delete(`/user/batch/${ids}`)
  ElMessage({
    showClose: true,
    message: '批量删除成功',
    type: 'success'
  })
  await load()
}
// 批量导出
const handleBatchExport = async () => {
  const ids = multipleSelection.value.map((row) => row.id)
  const res = await request(
    {
      url: `/user/batch/export/${ids}`,
      method: 'get',
      headers: {
        // application/msword 表示要处理为word格式
        // application/vnd.ms-excel 表示要处理为excel格式
        'Content-Type': 'vnd.ms-excel; charset=UTF-8'
      },

      responseType: 'blob'
    } //在请求中加上这一行，特别重要
  )
  downloadExcel(res, '用户')
}
// 分页查询
const load = async () => {
  const res = await request.get('/user/page', {
    params: {
      name: name.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      phone:phone.value,
      email:email.value,
      statu:statu.value
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
  phone.value=''
  email.value=''
  statu.value=''
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
  <UserForm
    :dialogVisible="dialogVisible"
    @changeDialog="handlechangeDialog"
    ref="childComp"
  ></UserForm>

  <el-row class="page"


  >
    <!--  分页查询表单按钮-->
    <el-col
        v-permission="'sys:user:search'"
    >
      <el-form :inline="true">
        <el-form-item>
          <el-input v-model="name" placeholder="点击输入用户名称"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="phone" placeholder="点击输入手机号"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="email" placeholder="点击输入邮箱号"></el-input>
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="statu" >
            <el-radio-button label="1" value="1">正常</el-radio-button>
            <el-radio-button label="0" value="0">封禁</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button type="warning" :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-col>
    <!-- 批量按钮 -->
    <el-col class="page-operate--layout">
      <div v-permission="'sys:user:add'">
        <el-button type="primary" :icon="Plus" @click="handleAdd" size="small" plain
          >新增
        </el-button>
      </div>
      <div v-permission="'sys:user:batch:delete'">
        <el-popconfirm
          confirm-button-text="确定"
          cancel-button-text="取消"
          :icon="InfoFilled"
          icon-color="#626AEF"
          title="确认要批量删除吗？"
          @confirm="handleBatchDel"
          @cancel="load"
        >
          <template #reference>
            <el-button type="danger" :disabled="disabled" :icon="Delete" size="small" plain
              >批量删除
            </el-button>
          </template>
        </el-popconfirm>
      </div>
      <div v-permission="'sys:user:batch:export'">
        <el-button
          type="warning"
          :icon="Download"
          :disabled="disabled"
          size="small"
          @click="handleBatchExport"
          plain
          >批量导出
        </el-button>
      </div>
    </el-col>
    <!--  分页页面-->
    <el-col>
      <el-table
        :data="tableData"
       class="page-table"

        :header-cell-style="{ background: '#eef1f6', color: '#606266' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"  />
        <el-table-column type="index"  label="序号" align="center"  width="70"  />
        <el-table-column prop="avatar" label="头像" align="center">
          <template #default="scope">
            <el-avatar :size="35" :src="scope.row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="账户名" align="center" show-overflow-tooltip/>
        <el-table-column prop="nickname" label="昵称" align="center"  show-overflow-tooltip/>
        <el-table-column prop="phone" label="电话号码" align="center" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱号" align="center"  show-overflow-tooltip/>
        <el-table-column prop="statu" label="当前状态" align="center">
          <template #default="scope">
            <el-tag class="ml-2" type="success" v-if="scope.row.statu === 1">正常</el-tag>
            <el-tag class="ml-2" type="danger" v-if="scope.row.statu === 0">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" align="center" show-overflow-tooltip
        width="200"
        />

        <el-table-column label="操作" align="center"
        width="240"
        >
          <template #default="scope">
            <div class="page-table-editout--layout">
              <div>
                <el-button
                  type="primary"
                  :icon="Edit"
                  size="small"
                  @click="handleUpdate(scope.row.id)"
                  v-permission="'sys:user:update'"
                  >编辑
                </el-button>
              </div>
              <div v-permission="'sys:user:delete'">
                <el-popconfirm
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  :icon="InfoFilled"
                  icon-color="#626AEF"
                  title="确认要删除吗？"
                  @confirm="handleDel(scope.row.id)"
                  @cancel="load"
                >
                  <template #reference>
                    <el-button type="danger" :icon="Delete" size="small">删除</el-button>
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

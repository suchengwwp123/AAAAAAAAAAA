<script setup>
import {nextTick, onBeforeMount, reactive, ref} from 'vue'
import {CopyDocument, FolderAdd, InfoFilled, Plus, Refresh, View} from '@element-plus/icons-vue'
import {ElLoading, ElMessage, ElMessageBox} from 'element-plus'
import request from '@/utils/request'
import router from "@/router";
import IconCommunity from "@/components/icons/IconCommunity.vue";
import IconDocumentation from "@/components/icons/IconDocumentation.vue";
import IconAI from "@/components/icons/IconAI.vue";
import {useRoute} from "vue-router";

const formRef = ref()
const sqlFormRef = ref()
const $route = useRoute()
const $router = router
// 定义联级选择器的规则策略
const props = {
  expandTrigger: 'hover',
}
onBeforeMount((async () => {
  if ($route.query.id) {

    await load($route.query.id)
  }
  await handleGetTables()
}))
const options = ref([])
// 初始化方法
const load = async (id) => {
  const res = await request.get(`/record/${id}`)
  Object.assign(dynamicValidateForm, JSON.parse(res.data.generator))
}
//获取数据表与字段 获取数据字典列表
const handleGetTables = async () => {
  const res = await request.get(`/generator/tables`)
  const tableOptions = res.data
  const red = await request.get(`/dict/options`)
  const dictOptions = red.data

  const sumarryOptions = [
    {
      label: '数据字典',
      value: '1',
      children: dictOptions
    },
    {
      label: '数据库表',
      value: '2',
      children: tableOptions
    }
  ]
  options.value = sumarryOptions


}


const dynamicValidateForm = reactive({
  domains: [
    {
      key: Date.now(),
      key1: Date.now(),
      key2: Date.now(),
      key3: Date.now(),
      key4: Date.now(),
      key5: Date.now(),
      key6: Date.now(),
      key7: Date.now(),
      key8: Date.now(),
      name: 'id',
      typeAndsize: 'bigint(20)',

      description: '序号',
      defaultValue: null,
      formComponent: 'input',
      isRequire: false,
      isShow: false,
      isSearch: false,
      relevance: []
    }
  ],
  description: '',
  tableName: '',
  sql: ''
})
// 是否展开右侧边栏
const dialog = ref(false)
// 校验规则
const rules = reactive({
  tableName: [
    {required: true, message: '数据库表名称不能为空', trigger: 'blur'},
    {min: 3, max: 20, message: '长度在3到20之间', trigger: 'blur'}
  ],
  description: [{required: true, message: '请输入数据库表注释', trigger: 'blur'}],
  sql: [{required: true, message: '请输入sql语句', trigger: 'blur'}]
})
// 是否必选选择框数组
const isRequires = [
  {
    label: '必选项',
    value: true
  },
  {
    label: '非必选项',
    value: false
  }
]
// 表单类型是否展示
const isChangeCascader = ref(true)

const removeDomain = (item) => {
  const index = dynamicValidateForm.domains.indexOf(item)
  if (index !== -1) {
    dynamicValidateForm.domains.splice(index, 1)
  }
}

const addDomain = () => {
  dynamicValidateForm.domains.push({
    key: Date.now() + 1,
    key1: Date.now() + 2,
    key2: Date.now() + 3,
    key3: Date.now() + 4,
    key4: Date.now() + 5,
    key5: Date.now() + 6,
    key6: Date.now() + 7,
    key7: Date.now() + 8,
    key8: Date.now() + 9,
    name: null,
    typeAndsize: 'varchar(255)',
    description: null,
    defaultValue: null,
    formComponent: 'input',
    isRequire: true,
    isShow: true,
    isSearch: false,
    relevance: []
  })
}
// 提交表单
const submitForm = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      // console.log(dynamicValidateForm)
      if (dynamicValidateForm.domains.length === 1) {
        ElMessage({
          showClose: true,
          message: '数据库字段长度不符合规范',
          type: 'error'
        })
        return false
      }
      console.log(dynamicValidateForm.domains)


      ElMessageBox.confirm(
          '确认已经仔细核对提交生成代码?',
          '重要提示',
          {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
          }
      )
          .then(async () => {
            const query = { ...$route.query }
            delete query.id
            $router.push(`/tools/generator`)
            await request.post('/generator', dynamicValidateForm)
            ElMessage({
              showClose: true,
              message: '构建成功，请立即重启项目！',
              type: 'success'
            })
          })
          .catch(() => {
            ElMessage({
              showClose: true,
              message: '取消构建！',
              type: 'warning'
            })
          })

    } else {
      console.log('error submit!')
      return false
    }
  })
}

const submitFormAi = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {


      ElMessageBox.confirm(
          '确认已经仔细数据库表的名称和注释?',
          '重要提示',
          {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
          }
      )
          .then(async () => {
            const loading = ElLoading.service({
              lock: true,
              text: '正在Ai构建中，请勿进行其他操作!',
              background: 'rgba(0, 0, 0, 0.7)',
            })
            const res = await request.post('/generator/ai', dynamicValidateForm)
            Object.assign(dynamicValidateForm, res.data)
            loading.close()
            dynamicValidateForm.domains.forEach(domain => {

                  if (domain.defaultValue === 'null' || domain.defaultValue === 'NULL')
                    domain.defaultValue = null
                }
            )
          })
          .catch(() => {
            ElMessage({
              showClose: true,
              message: '取消构建！',
              type: 'warning'
            })
          })

    } else {
      console.log('error submit!')
      return false
    }
  })
}
// 提交sq表单
const submitSqlForm = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      // console.log(dynamicValidateForm)

      const res = await request.post(`/generator/sqlgenerator`, dynamicValidateForm)
      Object.assign(dynamicValidateForm, res.data)
      dynamicValidateForm.domains.forEach(domain => {

            if (domain.defaultValue === 'null' || domain.defaultValue === 'NULL')
              domain.defaultValue = null
          }
      )
      dialog.value = false
    } else {
      console.log('error submit!')
      return false
    }
  })
}

// 重置表单
const resetForm = (formEl) => {
  if (!formEl) return
  formEl.resetFields()
  Object.assign(dynamicValidateForm, {
    domains: [
      {
        key: Date.now(),
        key1: Date.now(),
        key2: Date.now(),
        key3: Date.now(),
        key4: Date.now(),
        key5: Date.now(),
        name: 'id',
        typeAndsize: 'bigint(20)',
        isRequire: true,
        description: '序号',
        defaultValue: null,
        formComponent: 'input',
        isShow: 0
      }
    ],
    description: '',
    tableName: '',
    sql: ''
  })


  location.reload()
}
// 重置sql表单
const resetSqlForm = (formEl) => {

  if (!formEl) return
  formEl.resetFields()
  Object.assign(dynamicValidateForm, {
    domains: [
      {
        key: Date.now(),
        key1: Date.now(),
        key2: Date.now(),
        key3: Date.now(),
        key4: Date.now(),
        key5: Date.now(),
        name: 'id',
        typeAndsize: 'bigint(20)',
        isRequire: true,
        description: '序号',
        defaultValue: null,
        formComponent: 'input',
        isShow: 0
      }
    ],
    description: '',
    tableName: '',
    sql: ''
  })
  location.reload()
}
// 导入sql方法
const addSql = () => {
  dialog.value = true
}
// 关闭sql抽屉
const beforeCloseSqlForm = async (done) => {
  ElMessageBox.confirm('确定关闭窗口?')
      .then(() => {
        resetSqlForm(sqlFormRef.value)
      })
      .then(() => {
        done()
      })
      .catch(() => {
        // catch error
      })
}

</script>
<template>

  <el-form
      ref="formRef"
      :rules="rules"
      :model="dynamicValidateForm"
      label-width="10px"
      class="demo-dynamic"
  >
    <!--    1属性、生成按钮列-->
    <el-row :gutter="5">
      <el-col :span="4">
        <el-form-item prop="tableName" label-width="0">
          <el-input v-model="dynamicValidateForm.tableName" placeholder="输入数据库表名称"/>
        </el-form-item>
      </el-col>
      <el-col :span="4">
        <el-form-item prop="description" label-width="0">
          <el-input v-model="dynamicValidateForm.description" placeholder="输入表注释"/>
        </el-form-item>
      </el-col>
      <el-col :span="16">
        <el-button @click="addDomain" type="primary"
                   :icon="Plus"
        >新增字段
        </el-button>
        <el-button @click="submitFormAi(formRef)"
                   type="success"
                   :icon="IconAI"
        >AI构建
        </el-button>
        <el-button @click="addSql" type="info"
                   :icon="CopyDocument"
        >导入sql
        </el-button>
        <el-button @click="resetForm(formRef)" type="warning"
                   :icon="Refresh"
        >清空数据
        </el-button>

        <el-button type="danger"
                   :icon="FolderAdd"
                   @click="submitForm(formRef)">构建代码
        </el-button>
      </el-col>
    </el-row>
    <!--    2头部列区域-->
    <el-row class="form-header" :gutter="10">
      <el-col :span="3">
        <span>数据字段名</span>
      </el-col>
      <el-col :span="3">
        <span>数据类型</span>
      </el-col>
      <el-col :span="3">
        <span>默认值</span>
      </el-col>
      <el-col :span="3">
        <span>字段注释</span>
      </el-col>
      <el-col :span="3">
        <span>表单类型</span>
      </el-col>
      <el-col :span="3">
        <span>关联数据</span>
      </el-col>
      <el-col :span="1">
        <span>必选</span>
      </el-col>
      <el-col :span="1">
        <span>展示</span>
      </el-col>
      <el-col :span="1">
        <span>查询</span>
      </el-col>
      <el-col :span="1">
        <span>操作</span>
      </el-col>
    </el-row>

    <!--    3分条件进行判断渲染展示，例如固定的id为第0个-->
    <el-row v-for="(domain, index) in dynamicValidateForm.domains" :key="index" :gutter="10" class="form-content">
      <!--第二种条件进行判断是id的时候-->

      <!--      1数据字段名-->
      <el-col class="form-content-col" :span="3">
        <el-form-item
            :key="domain.key"

            :prop="'domains.' + index + '.name'"
            :rules="{
            required: true,
            message: '数据字段名不能为空',
            trigger: 'blur'
          }"
        >
          <el-input v-model="domain.name" placeholder="数据字段名" :disabled="index === 0"/>
        </el-form-item>
      </el-col>
      <!--      2数据类型和长度-->
      <el-col class="form-content-col" :span="3">
        <el-form-item
            :key="domain.key1"

            :prop="'domains.' + index + '.typeAndsize'"
            :rules="{
            required: true,
            message: '数据类型和长度不能为空',
            trigger: 'blur'
          }"
        >
          <el-select
              class="content-select"
              v-model="domain.typeAndsize"
              :disabled="index === 0"
              placeholder="数据类型和长度"
          >
            <el-option label="varchar(64)" value="varchar(64)"></el-option>
            <el-option label="varchar(255)" value="varchar(255)"></el-option>
            <el-option label="int(1)" value="int(1)"></el-option>
            <el-option label="int(5)" value="int(5)"></el-option>
            <el-option label="int(10)" value="int(10)"></el-option>
            <el-option label="bigint(20)" value="bigint(20)"></el-option>
            <el-option label="double(10,2)" value="double(10,2)"></el-option>
            <el-option label="datetime" value="datetime"></el-option>
            <el-option label="text" value="text"></el-option>
            <el-option label="longtext" value="longtext"></el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <!--      3默认值-->
      <el-col class="form-content-col" :span="3">
        <el-form-item :key="domain.key2" :prop="'domains.' + index + '.defaultValue'">
          <el-input
              class="content-input"
              v-model="domain.defaultValue"
              placeholder="请输入默认值"
              :disabled="index === 0"
          />
        </el-form-item>
      </el-col>
      <!--      4字段注释-->
      <el-col class="form-content-col" :span="3">
        <el-form-item

            :key="domain.key3"
            :prop="'domains.' + index + '.description'"
            :rules="{
            required: true,
            message: '字段注释不能为空',
            trigger: 'blur'
          }"
        >
          <el-input
              class="content-input"
              v-model="domain.description" placeholder="字段注释" :disabled="index === 0"/>
        </el-form-item>
      </el-col>
      <!--      5表单类型-->
      <el-col class="form-content-col" :span="3">
        <el-form-item
            :key="domain.key4"
            :prop="'domains.' + index + '.formComponent'"
            :rules="{
            required: true,
            message: '表单类型不能为空',
            trigger: 'change'
          }"
        >
          <el-select
              class="content-select"
              v-model="domain.formComponent"
              :disabled="index === 0||domain.relevance!=null&&domain.relevance.length>0"
              v-if="isChangeCascader"
              placeholder="选择表单类型"
          >
            <el-option label="普通表单" value="input"></el-option>
            <el-option label="滑块" value="slider"></el-option>
            <el-option label="评分" value="rate"></el-option>
            <el-option label="计数器" value="number"></el-option>
            <el-option label="电话" value="phone"></el-option>
            <el-option label="邮箱" value="email"></el-option>
            <el-option label="单图片" value="img"></el-option>
            <el-option label="多图片" value="mutileimg"></el-option>
            <el-option label="文件" value="file"></el-option>
            <el-option label="时间选择器" value="time"></el-option>
            <el-option label="日期选择器" value="date"></el-option>
            <el-option label="富文本" value="textarea"></el-option>

          </el-select>
        </el-form-item>
      </el-col>
      <!--      6关联表信息 -->
      <el-col class="form-content-col" :span="3">
        <el-form-item
            :label-width="10"
            :key="domain.key5"
            :prop="'domains.' + index + '.relevance'"
        >
          <el-cascader
              class="content-cascader"
              v-model="domain.relevance"
              :options="options"
              :props="props"
              placeholder="选择关联信息"
              :disabled="index === 0"
              clearable
          />
        </el-form-item>
      </el-col>
      <!--      7是否校验-->
      <el-col class="form-content-col" :span="1">
        <el-form-item

            :key="domain.key6"
            :prop="'domains.' + index + '.isRequire'"
            :rules="{
            required: true,
            message: '是否为必选不能为空',
            trigger: 'change'
          }"
        >

          <el-checkbox v-model="domain.isRequire" :disabled="index === 0"/>
        </el-form-item>
      </el-col>
      <!--      8是否展示表单-->
      <el-col class="form-content-col" :span="1">
        <el-form-item
            :key="domain.key7"
            :prop="'domains.' + index + '.isShow'"

        >
          <el-checkbox v-model="domain.isShow" :disabled="index === 0"/>
        </el-form-item>
      </el-col>
      <!--    9是否加入查询  -->
      <el-col class="form-content-col" :span="1">
        <el-form-item
            :key="domain.key7"
            :prop="'domains.' + index + '.isShow'"
        >
          <el-checkbox v-model="domain.isSearch" :disabled="index === 0"/>
        </el-form-item>
      </el-col>
      <!--     10       操作按钮-->
      <el-col class="form-content-col" :span="1">
        <el-popconfirm
            confirm-button-text="确认"
            cancel-button-text="取消"
            :icon="InfoFilled"
            icon-color="#626AEF"
            title="确认删除吗？"
            @confirm="removeDomain(domain)"
        >
          <template #reference>
            <el-button text type="danger" :disabled="index === 0">删除</el-button>
          </template>
        </el-popconfirm>
      </el-col>
    </el-row>
  </el-form>

  <!--  导入sql抽屉组件-->
  <el-drawer v-model="dialog" title="请将sql语句添加到下方输入框"
             :before-close="beforeCloseSqlForm"
             append-to-body size="40%">
    <el-form
        ref="sqlFormRef"
        :rules="rules"
        :model="dynamicValidateForm"
    >
      <el-form-item
          prop="sql"
      >
        <el-input
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 6 }"
            v-model="dynamicValidateForm.sql"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div
          class="el-drawer__footer-container--line"
      >
        <el-button @click="resetSqlForm(sqlFormRef)">取消</el-button>
        <el-button type="primary" @click="submitSqlForm(sqlFormRef)"> 提交</el-button>
      </div>
    </template>

  </el-drawer>
</template>
<style lang="scss">
.form-header {
  background-color: #eef1f6;
  color: #606266;
  height: 50px;
  margin-bottom: 10px;

  .el-form-item__content {
    display: flex;
    width: 100%;
    margin-left: 0px !important;
    justify-content: center;

  }

  .el-select,
  .el-cascader,
  .el-input {
    width: 100%;
  }

  span {
    font-size: 15px; /* 设置字体大小 */
    font-weight: bold; /* 设置字体加粗 */
    display: flex;
    width: 100%;
    justify-content: center; /* 水平居中 */
    align-items: center; /* 垂直居中 */
    height: 100%; /* 确保在表格中占满高度 */
    text-align: center; /* 文字居中 */
  }
}

//.checkBoxAlign {
//  width: 100%;
//  text-align: center;
//}

.form-content {
  .form-content-col {
    .el-form-item__content {
      display: flex;
      width: 100%;
      margin-left: 0px !important;
      justify-content: center;

    }

    .content-select,
    .content-cascader,
    .content-input {
      width: 100%;
    }


  }
}


</style>

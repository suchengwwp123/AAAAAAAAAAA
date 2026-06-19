<script setup>
import request from '@/utils/request'

import Cache from '@/views/monitor/cache/index.vue'
import * as echarts from 'echarts';
import {onBeforeMount, onBeforeUnmount, onMounted, reactive, ref, watch} from 'vue'
import pinia from "@/stores/store";
import useSystemStore from "@/stores/system";
import {Refresh, Search} from "@element-plus/icons-vue";


// 当前时间
const currentTime = ref('')
const systemStore = useSystemStore(pinia)
const currentDay = ref('7')
//公告数组
const notices = ref([])
const app_name = import.meta.env.VITE_LOGIN_NAME

// 统计数据
const statistics = reactive({
  vitaeCount: 0,
  orderCount: 0,
  complainCount: 0,
  ticketCount: 0
})

onMounted(async () => {
  await loadStatistics()
  await monitorload()
  await load()
  await viewload()
  await revenueload()
  const now = new Date()
  currentTime.value = formatTime(now)

})
// 获取统计数据
const loadStatistics = async () => {
  try {
    console.log('开始加载统计数据...')
    
    // 获取简历数
    const vitaeRes = await request.get('/vitae')
    statistics.vitaeCount = vitaeRes.data.length || 0
    console.log('简历数：', statistics.vitaeCount)
    
    // 获取订单数
    const orderRes = await request.get('/order')
    statistics.orderCount = orderRes.data.length || 0
    console.log('订单数：', statistics.orderCount)
    
    // 获取投诉数
    const complainRes = await request.get('/complain')
    statistics.complainCount = complainRes.data.length || 0
    console.log('投诉数：', statistics.complainCount)
    
    // 获取工单数
    const ticketRes = await request.get('/tickets')
    statistics.ticketCount = ticketRes.data.length || 0
    console.log('工单数：', statistics.ticketCount)
    
    console.log('统计数据加载完成：', statistics)
  } catch (error) {
    console.error('加载统计数据失败：', error)
  }
}

// 获取公告数据
const load = async () => {
  const res = await request.get('/notice')
  notices.value = res.data
}
let monitorChart = null
let viewChart = null
let revenueChart = null

// 获取营业额数据（从后端API获取）
const revenueload = async () => {
  var chartDom = document.getElementById('revenue');
  if (!revenueChart) {
    revenueChart = echarts.init(chartDom);
  }
  
  // 从后端API获取营业额数据
  const res = await request.get(`/order/revenue/${currentDay.value}`)
  
  // 后端返回的数据格式：{ dates: [...], revenue: [...] }
  const revenueData = res.data
  
  var option = {
    title: {
      text: `近${currentDay.value}天营业额`,
      left: 'center',
      textStyle: {
        fontSize: 14,
        color: '#666'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        return `${params[0].name}<br/>营业额：¥${params[0].value.toFixed(2)}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: revenueData.dates,
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '金额（元）',
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [
      {
        data: revenueData.revenue,
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: '¥{c}'
        }
      }
    ]
  };
  
  option && revenueChart.setOption(option);
}

// 获取系统监控数据
const monitorload = async () => {
  const res = await request.get('/system/resource')

  var chartDom = document.getElementById('monitor');
  monitorChart = echarts.init(chartDom);
  var option;

  const gaugeData = [
    {
      value: res.data.diskUsage,
      name: '磁盘',
      title: {
        offsetCenter: ['-40%', '80%']
      },
      detail: {
        offsetCenter: ['-40%', '95%']
      }
    },
    {
      value: res.data.memUsage,
      name: '内存',
      title: {
        offsetCenter: ['0%', '80%']
      },
      detail: {
        offsetCenter: ['0%', '95%']
      }
    },
    {
      value: res.data.cpuUsage,
      name: 'CPU',
      title: {
        offsetCenter: ['40%', '80%']
      },
      detail: {
        offsetCenter: ['40%', '95%']
      }
    }
  ];
  option = {
    series: [
      {
        type: 'gauge',
        anchor: {
          show: true,
          showAbove: true,
          size: 18,
          itemStyle: {
            color: '#FAC858'
          }
        },
        pointer: {
          icon: 'path://M2.9,0.7L2.9,0.7c1.4,0,2.6,1.2,2.6,2.6v115c0,1.4-1.2,2.6-2.6,2.6l0,0c-1.4,0-2.6-1.2-2.6-2.6V3.3C0.3,1.9,1.4,0.7,2.9,0.7z',
          width: 5,
          length: '80%',
          offsetCenter: [0, '8%']
        },
        progress: {
          show: true,
          overlap: true,
          roundCap: true
        },
        axisLine: {
          roundCap: true
        },
        data: gaugeData,
        title: {
          fontSize: 10
        },
        detail: {
          width: 10,
          height: 10,
          fontSize: 10,
          color: '#fff',
          backgroundColor: 'inherit',
          borderRadius: 3,
          formatter: '{value}%'
        }
      }
    ]
  };


  option && monitorChart.setOption(option);
}
// 获取访问数据请去
const viewload = async () => {

  const res = await request.get(`/log/echarts/${currentDay.value}`)

  var chartDom = document.getElementById('view');
  viewChart = echarts.init(chartDom);
  var option;


  option = {
    xAxis: {
      type: 'category',
      data: res.data.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: res.data.counts,
        type: 'line',
        smooth: true
      }
    ]
  };

  option && viewChart.setOption(option);
}
watch(() => systemStore.windowWidth,
    (newValue, oldValue)=>{
      monitorChart.resize()
      viewChart.resize()
      revenueChart.resize()
    }

)

// 格式化日期函数
const formatTime = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[date.getDay()]

  return `${year}年${month}月${day}日, ${weekDay} `
}
//查询方法
const handleSearch = async () => {
  await viewload()
  await revenueload()
}

//重置方法
const handleReset = async () => {
  currentDay.value = '7'
  await viewload()
  await revenueload()
}

</script>

<template>

   <el-row class="home" :gutter="5" >
     <!--    欢迎格栅-->
     <el-col>
       <el-card
         class="home-card-welcome"
       >
         <el-row>
           <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
             <div class="home-card-welcome__left">
               <el-image :src="systemStore.userInfo.avatar"></el-image>
               <div>
                 <div>
                   <el-text size="large" class="home-card-welcome__left-hello">Hello~{{
                       systemStore.userInfo.username
                     }},
                   </el-text>
                   <el-text size="large" class="home-card-welcome__left-system">欢迎使用 {{
                       app_name
                     }}
                   </el-text>
                 </div>
                 <div>
                   <el-text size="small" class="home-card-welcome__left-hello-now">陪诊师如需修改个人简历请联系管理员</el-text>
                 </div>
               </div>
             </div>

           </el-col>
           <el-col :xs="0" :sm="0" :md="0" :lg="12" :xl="12"

           >
             <div
               class="home-card-welcome__right"
             >
               <el-form :inline="true"

               >
                 <el-form-item>
                   <el-radio-group
                     v-model="currentDay"
                   >
                     <el-radio-button value="7" label="7">近7天</el-radio-button>
                     <el-radio-button value="15" label="15">近15天</el-radio-button>
                     <el-radio-button value="30" label="30">近30天</el-radio-button>
                   </el-radio-group>
                 </el-form-item>
                 <el-form-item>

                   <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
                   <el-button type="warning" :icon="Refresh" @click="handleReset">重置</el-button>
                 </el-form-item>
               </el-form>
             </div>
           </el-col>
         </el-row>
       </el-card>
     </el-col>

     <!--    统计数据卡片-->
     <el-col v-permission="'sys:user:list'" :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
       <el-card class="stat-card stat-card-vitae">
         <div class="stat-content">
           <div class="stat-icon">📋</div>
           <div class="stat-info">
             <div class="stat-value">{{ statistics.vitaeCount }}</div>
             <div class="stat-label">简历数量</div>
           </div>
         </div>
       </el-card>
     </el-col>

     <el-col v-permission="'sys:user:list'" :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
       <el-card class="stat-card stat-card-order">
         <div class="stat-content">
           <div class="stat-icon">📦</div>
           <div class="stat-info">
             <div class="stat-value">{{ statistics.orderCount }}</div>
             <div class="stat-label">订单数量</div>
           </div>
         </div>
       </el-card>
     </el-col>

     <el-col v-permission="'sys:user:list'" :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
       <el-card class="stat-card stat-card-complain">
         <div class="stat-content">
           <div class="stat-icon">💬</div>
           <div class="stat-info">
             <div class="stat-value">{{ statistics.complainCount }}</div>
             <div class="stat-label">投诉数量</div>
           </div>
         </div>
       </el-card>
     </el-col>

     <el-col  v-permission="'sys:user:list'" :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
       <el-card class="stat-card stat-card-ticket">
         <div class="stat-content">
           <div class="stat-icon">🎫</div>
           <div class="stat-info">
             <div class="stat-value">{{ statistics.ticketCount }}</div>
             <div class="stat-label">工单数量</div>
           </div>
         </div>
       </el-card>
     </el-col>

     <!--    性能监控格栅-->
     <el-col v-permission="'sys:user:list'" :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
       <el-card class="home-card-info">
         <template #header>
           <div class="card-header">
             <el-text>性能监控</el-text>
           </div>
         </template>
         <div id="monitor"
              style="width: 100%;height: calc(60vh - 90px );"
         >
         </div>
       </el-card>
     </el-col>
     <!--    访问记录格栅-->
     <el-col  v-permission="'sys:user:list'" :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
       <el-card class="home-card-analysis">
         <template #header>
           <div class="card-header">
             <el-text>访问数据</el-text>
           </div>
         </template>
         <div id="view"
              style="width: 100%;height: calc(60vh - 90px );"
         >
         </div>
       </el-card>
     </el-col>
     <!--    营业额统计格栅-->
     <el-col v-permission="'sys:user:list'">
       <el-card class="home-card-revenue">
         <template #header>
           <div class="card-header">
             <el-text>营业额统计</el-text>
           </div>
         </template>
         <div id="revenue"
              style="width: 100%;height: calc(60vh - 90px );"
         >
         </div>
       </el-card>
     </el-col>

     <!--    系统公告格栅-->
     <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12"
     >
       <el-card>
         <template #header>
           <div class="card-header">
             <el-text>系统公告</el-text>
           </div>
         </template>
         <el-timeline>
           <el-timeline-item
             v-for="(item,index) in notices"
             :key="index.toString()"
             :timestamp="item.createTime" placement="top">
             <el-card>
               <h4>{{ item.name }}</h4>
               <p>{{ item.content }}</p>
             </el-card>
           </el-timeline-item>

         </el-timeline>
       </el-card>
     </el-col>
     <!--    系统参数格栅-->
     <el-col  :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
       <el-card>
         <template #header>
           <div class="card-header">
             <el-text>系统参数</el-text>
           </div>

         </template>
         <Cache></Cache>
       </el-card>
     </el-col>


   </el-row>



</template>

<style scoped lang="scss">
.home {
  .home-card-welcome {

    margin-bottom: 10px;
    border-radius: 8px;

    .home-card-welcome__left {
      display: flex;
      align-items: center;
      gap: 16px;
      height: 50px;

      .el-image {
        width: 30px;
        height: 30px;
        border-radius: 50%;
      }


      .home-card-welcome__left-hello {
        font-weight: bold;
        cursor: pointer;
      }

      .home-card-welcome__left-system {
        color: #409EFF;
        cursor: pointer;
        margin-left: 8px;

      }

      .home-card-welcome__left-hello-now {
        color: #666
      }
    }

    .home-card-welcome__right {
      height: 50px;
      display: flex;
      justify-content: end;
      align-items: center;

      .el-form--inline .el-form-item {
        margin-right: 10px;
      }
    }
  }

  .home-card-info,
  .home-card-analysis,
  .home-card-revenue {
    height: 60vh;
    margin-bottom: 10px;
  }

  // 统计卡片样式
  .stat-card {
    margin-bottom: 10px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    overflow: hidden;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
    }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 0.5rem;
    }

    .stat-icon {
      font-size: 3rem;
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.1));
    }

    .stat-info {
      flex: 1;
    }

    .stat-value {
      font-size: 1.75rem;
      font-weight: 700;
      margin-bottom: 0.25rem;
      color: #fff;
    }

    .stat-label {
      font-size: 0.875rem;
      color: rgba(255, 255, 255, 0.9);
    }

    // 不同卡片的背景颜色
    &.stat-card-vitae {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    &.stat-card-order {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }

    &.stat-card-complain {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    }

    &.stat-card-ticket {
      background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
    }
  }
}
</style>

<script setup>
import request from '@/utils/request'
import settings from '@/utils/settings'
import Cache from '@/views/monitor/cache/index.vue'
import * as echarts from 'echarts';
import {onBeforeMount, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import pinia from "@/stores/store";
import useSystemStore from "@/stores/system";
import {Refresh, Search} from "@element-plus/icons-vue";


// 当前时间
const currentTime = ref('')
const systemStore = useSystemStore(pinia)
const currentDay = ref(7)
//公告数组
const notices = ref([])


onMounted(async () => {
  await monitorload()
  await load()
  await viewload()
  const now = new Date()
  currentTime.value = formatTime(now)

})
// 获取公告数据
const load = async () => {
  const res = await request.get('/notice')
  notices.value = res.data
}
// 获取系统监控数据
const monitorload = async () => {
  const res = await request.get('/system/resource')

  var chartDom = document.getElementById('monitor');
  var myChart = echarts.init(chartDom);
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


  option && myChart.setOption(option);
}
// 获取访问数据请去
const viewload = async () => {

  const res = await request.get(`/log/echarts/${currentDay.value}`)

  var chartDom = document.getElementById('view');
  var myChart = echarts.init(chartDom);
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

  option && myChart.setOption(option);
}


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
//重置方法
const handleReset = async () => {
  currentDay.value = 7
  await viewload()
}
</script>

<template>
  <el-row class="home" :gutter="5">
    <!--    欢迎格栅-->
    <el-col>
      <el-card
          class="home-card-welcome"
      >
        <el-row>
          <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="14">
            <div class="home-card-welcome__left">
              <el-avatar :src="systemStore.userInfo.avatar"></el-avatar>
              <div>
                <div>
                  <el-text size="large" class="home-card-welcome__left-hello">Hello~{{
                      systemStore.userInfo.username
                    }},
                  </el-text>
                  <el-text size="large" class="home-card-welcome__left-system">欢迎使用 {{
                      settings.layout_name
                    }}
                  </el-text>
                </div>
                <div>
                  <el-text size="small" class="home-card-welcome__left-hello-now">现在是 {{ currentTime }}</el-text>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :xs="0" :sm="0" :md="0" :lg="12" :xl="10"

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
                  <el-button type="primary" :icon="Search" @click="viewload">查询</el-button>
                  <el-button type="warning" :icon="Refresh" @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </el-col>
    <!--    性能监控格栅-->
    <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="10">
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
    <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="14">
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
    <!--    系统公告格栅-->
    <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="10"
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
    <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="14">
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
  .home-card-analysis {
    height: 60vh;
    margin-bottom: 10px;
  }
}
</style>

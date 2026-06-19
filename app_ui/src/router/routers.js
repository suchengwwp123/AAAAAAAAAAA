const modules = import.meta.glob('/src/views/**/**.vue')
// 常量路由
const constrouters = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/common/login/index.vue')
  },
  {
    path: '/forget',
    name: 'forger',
    component: () => import('@/views/common/login/forgetpassword.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/common/login/register.vue')
  },

  {
    path: '/front',
    name: 'front',
    component: () => import('@/views/front/front.vue'),
    redirect: { name: 'frontindex' },
    children:[
      {
        path: '/front/index',
        name: 'frontindex',
        component: () => import('@/views/front/index/index.vue'),
      },
      {
        path: '/front/contact',
        name: 'frontcontact',
        component: () => import('@/views/front/contact/index.vue'),
      }
      ,
      {
        path: '/front/info',
        name: 'frontinformation',
        component: () => import('@/views/front/information/index.vue'),
      }
      ,
      {
        path: '/front/detail',
        name: 'frontdetail',
        component: () => import('@/views/front/index/detail.vue'),
      }
      ,
      {
        path: '/front/collect',
        name: 'frontcollect',
        component: () => import('@/views/front/collect/index.vue'),
      }     ,
      {
        path: '/front/vitae',
        name: 'frontvitae',
        component: () => import('@/views/front/vitae/index.vue'),
      },
      {
        path: '/front/vitae/detail',
        name: 'frontvitaedetail',
        component: () => import('@/views/front/vitae/detail.vue'),
      }
      ,
      {
        path: '/front/order',
        name: 'frontorder',
        component: () => import('@/views/front/order/index.vue'),
      }
      ,
      {
        path: '/front/complain',
        name: 'frontordercomplain',
        component: () => import('@/views/front/complain/index.vue'),
      }
      ,
      {
        path: '/front/tickets',
        name: 'fronttickets',
        component: () => import('@/views/front/tickets/index.vue'),
      }
    ]
  },
  {
    //404
    path: '/404',
    component: () => import('@/views/common/404/index.vue'),
    name: '404'
  }
]

// 获取菜单信息
const getRouters = (data) => {
  const res = data
  const router = [
    {
      path: '/',
      name: '',
      component: () => import('@/layout/index.vue'),
      redirect: '/home',
      children: [
        {
          path: '/home',
          name: 'home',
          component: ()=>import('@/views/system/home/index.vue'),
          meta: {
            icon: 'HomeFilled',
            title: '系统首页',
            hidden: false
          }
        },
        {
          path: '/information',
          name: 'information',
          component:()=> import('@/views/common/information/index.vue'),
          meta: {
            icon: 'User',
            title: '个人信息',
            hidden: true
          }
        }
      ]
    }
  ]

  res.forEach((routeData) => {
    router[0].children.push(addRouter(routeData))
  })
  router[1] = {
    //登录成功展示数据的路由
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    name: 'Any',
    meta: {
      title: '404',
      hidden: true,
      icon: 'CircleCloseFilled'
    }
  }

  return router
}
// 菜单转成路由
const addRouter = (routeData) => {
  const route = {
    path: routeData.path,
    name: routeData.perms,
    meta: routeData.meta
  }
  route.component = modules[`/src/views${routeData.component}.vue`]
  if (routeData.children && routeData.children.length > 0) {
    route.redirect = routeData.children[0].path
    route.children = routeData.children.map((childRoute) => addRouter(childRoute))
  }
  return route
}

export default constrouters
export { getRouters }

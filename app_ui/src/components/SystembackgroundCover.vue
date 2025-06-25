<script setup>
import settings from "../utils/settings";
import {ref} from "vue";

const show = ref(false)

setTimeout(() => show.value = true, 200)
// todo 后续结合实际进行修改，暂时先使用此版本
</script>

<template>
  <transition name="el-zoom-in-center">
    <div className="bgcover"
         v-show="show"
         :style="{backgroundImage: `url(${settings.login_bg_src})`}"
    >
      <h1
          className="login-cover--loginname"
      >{{ settings.login_name }}</h1>
    </div>
  </transition>
</template>

<style scoped lang="scss">
.bgcover {
  background: no-repeat center center;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100%;

  .login-cover--loginname {
    font-size: 100px;
    color: white;
    font-weight: bolder;
    cursor: pointer;
  }
}
</style>
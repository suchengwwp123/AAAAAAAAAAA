<template>
  <main>
    <div
        v-bind="props"
        :class="
        cn(
          'relative flex flex-col h-[100vh] items-center justify-center bg-zinc-50 dark:bg-zinc-900 text-slate-950 transition-bg',
          props.class,
        )
      "
    >

      <div class="absolute inset-0 overflow-hidden">
        <div
            :class="
            cn(
              'filter blur-[10px] invert dark:invert-0 pointer-events-none absolute -inset-[10px] opacity-50 will-change-transform;',
              '[--white-gradient:repeating-linear-gradient(100deg,var(--white)_0%,var(--white)_7%,var(--transparent)_10%,var(--transparent)_12%,var(--white)_16%)]',
              '[--dark-gradient:repeating-linear-gradient(100deg,var(--black)_0%,var(--black)_7%,var(--transparent)_10%,var(--transparent)_12%,var(--black)_16%)]',
              '[--aurora:repeating-linear-gradient(100deg,var(--blue-500)_10%,var(--indigo-300)_15%,var(--blue-300)_20%,var(--violet-200)_25%,var(--blue-400)_30%)]',
              '[background-image:var(--white-gradient),var(--aurora)] dark:[background-image:var(--dark-gradient),var(--aurora)] [background-size:300%,_200%] [background-position:50%_50%,50%_50%]',
              'aurora-background-gradient-after',
              'aurora-gradient-animation',
              props.radialGradient &&
                `[mask-image:radial-gradient(ellipse_at_100%_0%,black_10%,var(--transparent)_70%)]`,
            )
          "
        ></div>
      </div>
      <AuroraBackground>
        <Motion
            as="div"
            :initial="{ opacity: 0, y: 40, filter: 'blur(10px)' }"
            :while-in-view="{
        opacity: 1,
        y: 0,
        filter: 'blur(0px)',
      }"
            :transition="{
        delay: 0.3,
        duration: 0.8,
        ease: 'easeInOut',
      }"
            class="relative flex flex-col items-center justify-center gap-4 px-4"
        >
          <div class="text-center text-3xl font-bold md:text-7xl dark:text-white">
           {{login_name}}
          </div>
          <div class="py-4 text-base font-extralight md:text-3xl dark:text-neutral-100">
           基于SpringBoot+Vue架构,一键生成前后端代码、数据库表、接口文档。
          </div>
          <button
              @click="handleDoc"
              class="w-fit rounded-full bg-black px-4 py-2 text-white dark:bg-white dark:text-black"
          >
            立即开始
          </button>
        </Motion>
      </AuroraBackground>
    </div>
  </main>
</template>

<script setup >
import { cn } from "@/lib/utils";

import { Motion } from "motion-v";
import {ref} from "vue";
const props = withDefaults(defineProps(), {
  radialGradient: true,
});
const handleDoc=()=>{
window.open(`${import.meta.env.VITE_USE_URL}`)
}
const login_name=import.meta.env.VITE_LOGIN_NAME
</script>

<style scoped>
.aurora-background-gradient-after {
  @apply after:content-[""]
  after:absolute
  after:inset-0
  after:[background-image:var(--white-gradient),var(--aurora)]
  after:dark:[background-image:var(--dark-gradient),var(--aurora)]
  after:[background-size:200%,_100%]
  after:[background-attachment:fixed]
  after:mix-blend-difference;
}

.aurora-gradient-animation::after {
  animation: animate-aurora 60s linear infinite;
}

@keyframes animate-aurora {
  0% {
    background-position:
        50% 50%,
        50% 50%;
  }
  100% {
    background-position:
        350% 50%,
        350% 50%;
  }
}
</style>
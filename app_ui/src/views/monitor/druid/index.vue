<script setup></script>

<template>
  <iframe
      id="druidframe"
      src="/druid/login.html"
      frameborder="0"
      width="100%"
      height="100%"
      allow="fullscreen"
      referrerpolicy="no-referrer"
      sandbox=" allow-scripts allow-forms allow-scripts allow-same-origin allow-top-navigation-by-user-activation"

  >
  </iframe>
</template>

<style scoped lang="scss">
#druidframe {
  width: 100%;
  height: calc(100vh - 30px);
  border: none;
}
</style>

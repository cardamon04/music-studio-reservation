<template>
    <section class="wrap">
      <header class="head">
        <slot name="header" />
      </header>
      <main class="main">
        <slot />
      </main>
    </section>
  </template>

  <script setup lang="ts">
  /* レイアウト専用。データ取得/API呼び出しは行わない */
  </script>

  <style scoped>
  .wrap { padding: 16px; }
  .head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
  .main { min-height: 300px; }
  </style>
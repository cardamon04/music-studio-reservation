<!-- src/components/atoms/StatusBadge.vue -->
<template>
    <span :class="klass">{{ text }}</span>
  </template>

  <script lang="ts">
  import { defineComponent, computed } from "vue";

  export default defineComponent({
    name: "StatusBadge",
    props: {
      text: { type: String, required: true }
    },
    setup(props) {
      // シンプルな色分け（デザインは後で調整）
      const klass = computed(() => {
        switch (props.text) {
          case "空": return "px-2 py-1 rounded bg-gray-200";
          case "予約済み": return "px-2 py-1 rounded bg-yellow-200";
          case "予約確定": return "px-2 py-1 rounded bg-blue-200";
          case "使用中": return "px-2 py-1 rounded bg-green-200";
          case "予約キャンセル": return "px-2 py-1 rounded bg-red-200";
          default: return "px-2 py-1 rounded bg-gray-200";
        }
      });
      return { klass };
    }
  });
  </script>

  <style scoped>
  /* 必要なら細かい調整 */
  </style>

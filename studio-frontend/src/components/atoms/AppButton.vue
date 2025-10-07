<template>
    <button :type="type" :disabled="disabled" @click="$emit('click')" class="btn">
      <slot />
    </button>
  </template>
  <script setup lang="ts">
  defineProps<{ type?: 'button' | 'submit'; disabled?: boolean }>();
  defineEmits<{ (e: 'click'): void }>();
  </script>
  <style scoped>
  .btn {
    padding: 8px 14px;
    border-radius: 8px;
    border: 1px solid #555;
    background: #1f6feb;
    color: #fff;
    font-weight: 700;
    cursor: pointer;
  }
  .btn:disabled { opacity: 0.5; cursor: not-allowed; }
  </style>
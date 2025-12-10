<template>
  <div class="flex items-start justify-between gap-3 text-sm">
    <span class="min-w-20 shrink-0 text-gray-500">{{ label }}</span>
    <span class="font-medium text-gray-900"><slot /></span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string;
}>();
</script>


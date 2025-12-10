<template>
  <span
    :class="[
      'inline-flex items-center justify-center rounded-md px-2 py-1 text-xs font-medium leading-none select-none',
      colorClass,
    ]"
    role="status"
    :aria-label="ariaLabel"
  >
    <slot />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  status: 'available' | 'booked' | 'in_use' | 'canceled';
}>();

const colorClass = computed(() => {
  switch (props.status) {
    case 'available':
      return 'bg-green-500 text-white';
    case 'booked':
      return 'bg-amber-400 text-white';
    case 'in_use':
      return 'bg-blue-500 text-white';
    case 'canceled':
      return 'bg-gray-400 text-white';
  }
});

const ariaLabel = computed(() => {
  switch (props.status) {
    case 'available': return '空き';
    case 'booked': return '予約済み';
    case 'in_use': return '使用中';
    case 'canceled': return 'キャンセル';
  }
});
</script>

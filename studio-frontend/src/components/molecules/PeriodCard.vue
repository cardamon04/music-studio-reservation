<template>
  <article 
    class="period" 
    :class="{ 'is-clickable': isClickable }"
    role="listitem" 
    :aria-label="`${slotData.label} ${slotData.timeRange} ${statusLabel}`"
    @click="handleClick"
  >
    <div class="p-head">
      <span>{{ slotData.label }}</span>
      <span class="badge" :class="badgeClass">{{ statusLabel }}</span>
    </div>
    <div class="p-time">{{ slotData.timeRange }}</div>
    <div v-if="slotData.status !== 'available'" class="p-reservation-info">
      <span v-if="slotData.reservationType" class="reservation-type">{{ getReservationTypeLabel(slotData.reservationType) }}</span>
      <span v-if="slotData.eventName" class="event-name">{{ slotData.eventName }}</span>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { PeriodSlot } from '@/lib/types';

const props = defineProps<{
  slotData: PeriodSlot;
  topRight?: string; // 右上に小さく出す補助テキスト（任意）
}>();

const emit = defineEmits<{
  click: [slot: PeriodSlot];
}>();

const statusLabel = computed(() => {
  switch (props.slotData.status) {
    case 'available': return '空き';
    case 'booked': return '予約済み';
    case 'in_use': return '使用中';
    case 'canceled': return 'キャンセル';
  }
});

const badgeClass = computed(() => {
  switch (props.slotData.status) {
    case 'available': return 'is-free';
    case 'booked': return 'is-reserved';
    case 'in_use': return 'is-busy';
    case 'canceled': return 'is-canceled';
  }
});

const isClickable = computed(() => {
  return props.slotData.status === 'available';
});

function getReservationTypeLabel(reservationType: string): string {
  switch (reservationType) {
    case '学生レンタル': return '学生';
    case '授業レンタル': return '授業';
    case 'イベント予約': return 'イベント';
    default: return reservationType;
  }
}

function handleClick() {
  if (isClickable.value) {
    emit('click', props.slotData);
  }
}
</script>

<style scoped>
/* ===== Period Card ===== */
.period {
  scroll-snap-align: start;
  background: #FFFFFF;
  border: 1px solid var(--border, #E5E7EB);
  border-radius: var(--radius-md, 12px);
  padding: var(--space-3, 12px);
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: var(--space-2, 8px);
  min-height: 92px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.period.is-clickable {
  cursor: pointer;
}

.period.is-clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.period.is-clickable:active {
  transform: translateY(0);
}

.p-head {
  display: flex; 
  align-items: center; 
  justify-content: space-between;
  font-weight: 700;
}

.p-time { 
  color: var(--muted, #6B7280); 
  font-size: 12px; 
}

.p-reservation-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.reservation-type {
  font-size: 10px;
  color: #374151;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  width: fit-content;
}

.event-name {
  font-size: 10px;
  color: #6b7280;
  font-style: italic;
}

.badge {
  display: inline-flex; 
  align-items: center; 
  justify-content: center;
  font-size: 12px; 
  font-weight: 700; 
  color: #fff;
  border-radius: 999px; 
  padding: 4px 8px;
}

.is-free { 
  background: var(--ok, #10B981); 
}

.is-busy { 
  background: var(--busy, #3B82F6); 
}

.is-reserved { 
  background: var(--warn, #FBBF24); 
  color: #111827; /* 黄は黒字のほうが読みやすい */
}

.is-canceled { 
  background: var(--cancel, #9CA3AF); 
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .is-reserved { 
    color: #0b0e13; /* 黄は暗色背景でも黒字で可読 */
  }
}
</style>

<template>
  <section class="studio" :aria-labelledby="`studio-${studio.id.toLowerCase()}`">
    <h2 :id="`studio-${studio.id.toLowerCase()}`">{{ studio.name }}</h2>
    <div class="periods" role="list" aria-label="時間枠">
      <PeriodCard
        v-for="p in studio.periods"
        :key="p.id"
        :slotData="p"
        @click="handleSlotClick"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import PeriodCard from '@/components/molecules/PeriodCard.vue';
import type { Studio, PeriodSlot } from '@/lib/types';

const props = defineProps<{
  studio: Studio;
}>();

const emit = defineEmits<{
  slotClick: [studioId: string, slot: PeriodSlot];
}>();

function handleSlotClick(slot: PeriodSlot) {
  emit('slotClick', props.studio.id, slot);
}
</script>

<style scoped>
/* ===== Studio Card ===== */
.studio {
  background: var(--panel, #FFFFFF);
  border: 1px solid var(--border, #E5E7EB);
  border-radius: var(--radius-lg, 16px);
  box-shadow: var(--shadow, 0 6px 16px rgba(0,0,0,.06));
  padding: var(--space-4, 16px);
  margin-top: var(--space-4, 16px);
}

.studio h2 {
  margin: 0 0 var(--space-3, 12px);
  font-size: 18px;
}

/* ===== Horizontal Period List ===== */
.periods {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 120px; /* 1カードの基本幅 */
  gap: var(--space-3, 12px);
  overflow-x: auto;
  padding-bottom: var(--space-2, 8px);
  scroll-snap-type: x mandatory;
  scrollbar-width: thin; /* Firefox */
}

.periods::-webkit-scrollbar { 
  height: 8px; 
}

.periods::-webkit-scrollbar-thumb {
  background: #D1D5DB; 
  border-radius: 999px;
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .periods::-webkit-scrollbar-thumb { 
    background: #2a3441; 
  }
}
</style>

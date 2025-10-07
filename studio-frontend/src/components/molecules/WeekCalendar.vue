<template>
  <div class="calendar">
    <div class="week-header">
      <div v-for="(d, i) in days" :key="i" class="header-cell">
        {{ d.weekday }}
      </div>
    </div>
    <div class="week-body">
      <DateCell
        v-for="d in days"
        :key="d.date"
        :date="d.date"
        :day="d.day"
        :today="d.today"
        :active="d.date === selected"
        :disabled="d.disabled"
        @select="$emit('update:selected', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import DateCell from "@/components/atoms/DateCell.vue";

const props = defineProps<{ selected: string }>();
const emit = defineEmits<{ (e: "update:selected", date: string): void }>();

// 今日を基準に週を作る（例: 日曜〜土曜）
function getWeekDays(base: Date): { date: string; day: number; weekday: string; today: boolean; disabled: boolean }[] {
  const start = new Date(base);
  start.setDate(base.getDate() - base.getDay()); // 週の最初の日 (日曜日)
  
  const today = new Date();
  const maxDate = new Date(today);
  maxDate.setDate(today.getDate() + 7); // 1週間後

  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(start);
    d.setDate(start.getDate() + i);

    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    const iso = `${yyyy}-${mm}-${dd}`;
    
    // 過去の日付または1週間後以降の日付は無効
    const isDisabled = d < today || d > maxDate;

    return {
      date: iso,
      day: d.getDate(),
      weekday: ["日","月","火","水","木","金","土"][d.getDay()],
      today: iso === new Date().toISOString().slice(0,10),
      disabled: isDisabled,
    };
  });
}

const days = computed(() => getWeekDays(new Date(props.selected)));
</script>

<style scoped>
.calendar { 
  margin-bottom: 1rem; 
  width: 100%;
  max-width: 400px;
}

.week-header, .week-body {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0.375rem;
}

.header-cell {
  text-align: center;
  font-weight: 600;
  padding: 0.375rem 0.25rem;
  font-size: 0.875rem;
  color: #374151;
  background: #f9fafb;
  border-radius: 4px;
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* タブレット以上 */
@media (min-width: 768px) {
  .calendar {
    max-width: 500px;
  }
  
  .week-header, .week-body {
    gap: 0.5rem;
  }
  
  .header-cell {
    padding: 0.5rem 0.375rem;
    font-size: 1rem;
    min-height: 36px;
  }
}

/* デスクトップ以上 */
@media (min-width: 1024px) {
  .calendar {
    max-width: 600px;
  }
}
</style>

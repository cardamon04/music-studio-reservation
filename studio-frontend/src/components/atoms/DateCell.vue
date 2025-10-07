<template>
    <div
      class="date-cell"
      :class="{ active, today, disabled }"
      :title="getTooltipText()"
      @click="!disabled && $emit('select', date)"
    >
      {{ day }}
    </div>
  </template>

  <script setup lang="ts">
  import { computed } from "vue";
  
  // props: 受け取る値
  const props = defineProps<{
    date: string;   // YYYY-MM-DD
    day: number;    // 日にち (1〜31)
    active: boolean; // 選択中かどうか
    today: boolean;  // 今日かどうか
    disabled?: boolean; // 無効かどうか
  }>();

  // emits: 親に通知するイベント
  defineEmits<{ (e: "select", date: string): void }>();
  
  // ツールチップのテキストを生成
  function getTooltipText(): string {
    if (props.disabled) {
      const cellDate = new Date(props.date);
      const today = new Date();
      const maxDate = new Date(today);
      maxDate.setDate(today.getDate() + 7);
      
      if (cellDate < today) {
        return "過去の日付は予約できません";
      } else if (cellDate > maxDate) {
        return "1週間後以降の日付は予約できません";
      }
      return "この日付は予約できません";
    }
    
    if (props.today) {
      return "今日 - 予約可能";
    }
    
    return `${props.date} - 予約可能`;
  }
  </script>

<style scoped>
.date-cell {
  padding: 0.5rem 0.25rem;
  text-align: center;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
  font-size: 0.875rem;
  font-weight: 500;
  min-height: 36px;
  min-width: 36px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid #e5e7eb;
  aspect-ratio: 1;
}

.date-cell:hover:not(.disabled) { 
  background: #f3f4f6; 
  border-color: #d1d5db;
  transform: translateY(-1px);
}

.date-cell.active { 
  background: #3b82f6 !important; 
  color: #fff !important; 
  font-weight: 700 !important;
  border-color: #2563eb !important;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3) !important;
}

.date-cell.today { 
  border: 2px solid #10b981; 
  background: #ecfdf5;
  color: #047857;
  font-weight: 700;
}

.date-cell.disabled { 
  background: #f9fafb; 
  color: #9ca3af; 
  cursor: not-allowed; 
  opacity: 0.6;
  border-color: #e5e7eb;
}

/* 今日の日付が選択されている場合 */
.date-cell.today.active { 
  background: #3b82f6 !important; 
  color: #fff !important; 
  font-weight: 700 !important;
  border: 2px solid #2563eb !important;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3) !important;
}

/* タブレット以上 */
@media (min-width: 768px) {
  .date-cell {
    padding: 0.75rem 0.5rem;
    font-size: 1rem;
    min-height: 44px;
    min-width: 44px;
  }
}

/* デスクトップ以上 */
@media (min-width: 1024px) {
  .date-cell {
    padding: 0.875rem 0.625rem;
    min-height: 48px;
    min-width: 48px;
  }
}
</style>
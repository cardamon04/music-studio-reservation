<template>
  <div class="date-picker">
    <input
      ref="dateInput"
      type="date"
      :value="formattedDate"
      :min="minDate"
      :max="maxDate"
      @change="handleDateChange"
      @click="handleInputClick"
      class="date-input"
      aria-label="日付を選択"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

const props = defineProps<{
  modelValue: Date;
  minDate?: string; // yyyy-MM-dd形式
  maxDate?: string; // yyyy-MM-dd形式
}>();

const emit = defineEmits<{
  'update:modelValue': [date: Date];
}>();

const dateInput = ref<HTMLInputElement>();

// 日付をyyyy-MM-dd形式に変換
const formattedDate = computed(() => {
  return props.modelValue.toISOString().split('T')[0];
});

// デフォルトの最小日付（今日）
const minDate = computed(() => props.minDate || new Date().toISOString().split('T')[0]);

// デフォルトの最大日付（1週間後）
const maxDate = computed(() => {
  if (props.maxDate) return props.maxDate;
  const max = new Date();
  max.setDate(max.getDate() + 7);
  return max.toISOString().split('T')[0];
});

function handleDateChange(event: Event) {
  const target = event.target as HTMLInputElement;
  const newDate = new Date(target.value);
  
  // 有効な日付かチェック
  if (!isNaN(newDate.getTime())) {
    emit('update:modelValue', newDate);
  }
}

function handleInputClick() {
  // モバイルデバイスでカレンダーを確実に表示
  if (dateInput.value) {
    dateInput.value.showPicker?.();
  }
}

// 外部からカレンダーを開くためのメソッド
function openCalendar() {
  if (dateInput.value) {
    dateInput.value.showPicker?.();
  }
}

// 親コンポーネントから呼び出せるように公開
defineExpose({
  openCalendar
});
</script>

<style scoped>
.date-picker {
  position: relative;
}

.date-input {
  width: 100%;
  padding: 0;
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
  font-weight: 600;
  font-size: 14px;
  color: #000000 !important;
  cursor: pointer;
  outline: none;
  transition: none;
}

.date-input:focus {
  outline: 2px solid var(--primary, #3B82F6);
  outline-offset: 2px;
}

.date-input:hover {
  opacity: 0.8;
}

/* カレンダーアイコンのスタイリング */
.date-input::-webkit-calendar-picker-indicator {
  background: transparent;
  bottom: 0;
  color: transparent;
  cursor: pointer;
  height: auto;
  left: 0;
  position: absolute;
  right: 0;
  top: 0;
  width: auto;
}

/* カレンダーのクリアボタン（×ボタン）を非表示 */
.date-input::-webkit-clear-button {
  display: none;
}

.date-input::-webkit-inner-spin-button {
  display: none;
}

/* Firefox用のクリアボタンを非表示 */
.date-input::-moz-clear-button {
  display: none;
}

/* カレンダーポップアップ内のクリアボタンを非表示 */
.date-input::-webkit-datetime-edit-ampm-field {
  display: none;
}

/* スピンボタンも非表示 */
.date-input::-webkit-outer-spin-button {
  display: none;
}

/* カレンダーポップアップのスタイル調整 */
.date-input::-webkit-datetime-edit-year-field:focus,
.date-input::-webkit-datetime-edit-month-field:focus,
.date-input::-webkit-datetime-edit-day-field:focus {
  background: transparent;
  color: #000000 !important;
}

/* 選択された日付の背景色を透過に */
.date-input::-webkit-datetime-edit {
  background: transparent;
  color: #000000 !important;
}

.date-input::-webkit-datetime-edit-fields-wrapper {
  background: transparent;
  color: #000000 !important;
}

.date-input::-webkit-datetime-edit-text {
  background: transparent;
  color: #000000 !important;
}

.date-input::-webkit-datetime-edit-month-field {
  background: transparent;
  color: #000000 !important;
}

.date-input::-webkit-datetime-edit-day-field {
  background: transparent;
  color: #000000 !important;
}

.date-input::-webkit-datetime-edit-year-field {
  background: transparent;
  color: #000000 !important;
}

/* フォーカス時の背景色も透過に */
.date-input::-webkit-datetime-edit:focus {
  background: transparent;
  color: #000000 !important;
}

.date-input::-webkit-datetime-edit-fields-wrapper:focus {
  background: transparent;
  color: #000000 !important;
}

/* Firefox用の背景色透過 */
.date-input::-moz-datetime-edit {
  background: transparent;
  color: #000000 !important;
}

.date-input::-moz-datetime-edit-fields-wrapper {
  background: transparent;
  color: #000000 !important;
}

/* 全ブラウザ対応の背景色透過 */
.date-input:focus {
  background: transparent !important;
  color: #000000 !important;
}

.date-input:active {
  background: transparent !important;
  color: #000000 !important;
}

/* ダークモードでも黒い文字色を維持 */
@media (prefers-color-scheme: dark) {
  .date-input {
    background: transparent;
    border-color: var(--border, #1f2a37);
    color: #000000 !important;
  }
  
  /* ダークモードでも黒い文字色を維持 */
  .date-input::-webkit-datetime-edit {
    background: transparent;
    color: #000000 !important;
  }
  
  .date-input::-webkit-datetime-edit-fields-wrapper {
    background: transparent;
    color: #000000 !important;
  }
  
  .date-input::-webkit-datetime-edit-text {
    background: transparent;
    color: #000000 !important;
  }
  
  .date-input::-webkit-datetime-edit-month-field {
    background: transparent;
    color: #000000 !important;
  }
  
  .date-input::-webkit-datetime-edit-day-field {
    background: transparent;
    color: #000000 !important;
  }
  
  .date-input::-webkit-datetime-edit-year-field {
    background: transparent;
    color: #000000 !important;
  }
  
  .date-input:focus {
    background: transparent !important;
    color: #000000 !important;
  }
  
  .date-input:active {
    background: transparent !important;
    color: #000000 !important;
  }
}
</style>

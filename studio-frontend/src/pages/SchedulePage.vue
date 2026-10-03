<template>
  <main class="schedule-page" role="main" aria-labelledby="page-title">
    <!-- Header -->
    <header class="header">
      <h1 id="page-title" class="title">予約状況</h1>
      <p class="selection-help">日付を選び、希望する時間の「空き枠を選ぶ」を押してください。</p>
      <div class="datebar" role="group" aria-label="日付選択">
        <span class="icon" aria-hidden="true">📅</span>
        <DatePicker
          v-model="selectedDate"
          :min-date="minSelectableDate"
          :max-date="maxSelectableDate"
          @update:model-value="onDateChange"
        />
      </div>
    </header>

    <!-- Content -->
    <div class="container">
      <!-- ローディング状態 -->
      <div v-if="loading" class="loading-state" role="status">
        <div class="loading-spinner" aria-hidden="true"></div>
        <p>予約状況を読み込んでいます…</p>
      </div>

      <!-- エラー状態 -->
      <div v-else-if="error" class="error-state" role="alert">
        <p>予約状況を読み込めませんでした。</p>
        <p>接続を確認して、もう一度読み込んでください。</p>
        <button class="btn-retry" type="button" @click="fetchStudiosData">
          もう一度読み込む
        </button>
      </div>

      <!-- データ表示 -->
      <div v-else>
        <!-- スタジオごとのカード -->
        <StudioCard
          v-for="s in studios"
          :key="s.id"
          :studio="s"
          @slot-click="handleSlotClick"
        />

        <!-- データが空の場合 -->
        <div v-if="studios.length === 0" class="empty-state">
          <p>スタジオデータが見つかりませんでした</p>
        </div>
      </div>

    </div>

    <!-- 予約ダイアログ -->
    <BookingDialog
      :is-visible="showReservationDialog"
      :selected-studio="reservationData.studioName"
      :selected-period="reservationData.periodLabel"
      :usage-date="reservationData.date"
      @close="handleBookingDialogClose"
      @submit="handleBookingSubmit"
    />

    <!-- メッセージダイアログ -->
    <MessageDialog
      :is-visible="showMessageDialog"
      :title="messageDialog.title"
      :message="messageDialog.message"
      :type="messageDialog.type"
      @close="handleMessageDialogClose"
    />
  </main>
</template>

<script setup lang="ts">
/**
 * スマホ優先の予約状況ページ
 * - Atomic DesignのOrganisms（StudioCard）を使用
 * - 日付ごとの空き枠から予約入力を開く
 */

import { onMounted, ref, computed } from 'vue';
import StudioCard from '@/components/organisms/StudioCard.vue';
import DatePicker from '@/components/molecules/DatePicker.vue';
import BookingDialog from '@/components/organisms/BookingDialog.vue';
import MessageDialog, { type MessageButton } from '@/components/atoms/MessageDialog.vue';
import type { Studio, PeriodSlot } from '@/lib/types';
import { fetchBookingCalendar } from '@/api/bookingCalendarApi';
import { transformBookingCalendarToStudios, formatDateForApi } from '@/lib/apiTransformers';
import { createBooking, type CreateBookingRequest } from '@/api/bookingApi';

// ▼ 選択中の日付（現在の日付を使用）
const selectedDate = ref(new Date());

// ▼ 日付選択の制限（今日から1週間後まで）
const minSelectableDate = computed(() => {
  return new Date().toISOString().split('T')[0];
});

const maxSelectableDate = computed(() => {
  const maxDate = new Date();
  maxDate.setDate(maxDate.getDate() + 7);
  return maxDate.toISOString().split('T')[0];
});

// ▼ データ（APIから取得）
const studios = ref<Studio[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
let calendarRequest = 0;

// ▼ 予約ダイアログの状態
const showReservationDialog = ref(false);
const reservationData = ref({
  date: '',
  dateLabel: '',
  studioId: '',
  studioName: '',
  periodLabel: '',
  timeRange: '',
});

// ▼ メッセージダイアログの状態
const showMessageDialog = ref(false);
const messageDialog = ref({
  title: '',
  message: '',
  type: 'info' as 'success' | 'error' | 'warning' | 'info',
});

// ▼ データ取得関数
async function fetchStudiosData() {
  const request = ++calendarRequest;
  loading.value = true;
  error.value = null;
  
  try {
    const dateString = formatDateForApi(selectedDate.value);
    console.log('=== データ取得開始 ===');
    console.log('対象日付:', dateString);
    
    const calendarData = await fetchBookingCalendar(dateString);
    if (request !== calendarRequest) return;
    console.log('APIレスポンス:', calendarData);
    
    studios.value = transformBookingCalendarToStudios(calendarData);
    console.log('変換後のスタジオデータ:', studios.value);
    console.log('スタジオ数:', studios.value.length);
    
    // 各スタジオの最初の期間のstatusをログ出力
    studios.value.forEach(studio => {
      console.log(`${studio.name}:`, studio.periods.map(p => `${p.id}=${p.status}`).join(', '));
    });
  } catch (err) {
    if (request !== calendarRequest) return;
    console.error('Failed to fetch studios data:', err);
    error.value = err instanceof Error ? err.message : 'データの取得に失敗しました';
    // エラー時は空の配列を設定
    studios.value = [];
  } finally {
    if (request === calendarRequest) loading.value = false;
  }
}

// ▼ 日付変更時のハンドラー
function onDateChange(newDate: Date) {
  selectedDate.value = newDate;
  fetchStudiosData();
}

// ▼ スロットクリック時のハンドラー
function handleSlotClick(studioId: string, slot: PeriodSlot) {
  console.log('=== handleSlotClick 呼び出し ===');
  console.log('studioId:', studioId);
  console.log('slot:', slot);
  console.log('slot.status:', slot.status);
  
  // 空きスロットの場合のみ予約ダイアログを開く
  if (slot.status === 'available') {
    console.log('空きスロット検出！');
    const studio = studios.value.find(s => s.id === studioId);
    console.log('スタジオ情報:', studio);
    
    if (studio) {
      console.log('予約ダイアログを開きます');
      openReservationDialog(studioId, studio.name, slot.id, slot.timeRange);
    } else {
      console.warn('スタジオが見つかりません:', studioId);
    }
  } else {
    console.log('空きスロットではありません。status:', slot.status);
  }
}

// ▼ 初期ロード
onMounted(() => {
  fetchStudiosData();
});



// ▼ 予約ダイアログを開く
function openReservationDialog(studioId: string, studioName: string, periodLabel: string, timeRange: string) {
  console.log('=== openReservationDialog 呼び出し ===');
  console.log('引数:', { studioId, studioName, periodLabel, timeRange });
  
  const date = formatDateForApi(selectedDate.value);
  const dateLabel = formatDateForDisplay(selectedDate.value);
  
  reservationData.value = {
    date,
    dateLabel,
    studioId,
    studioName,
    periodLabel,
    timeRange,
  };
  
  console.log('reservationData設定完了:', reservationData.value);
  console.log('ダイアログ表示フラグをtrueに設定');
  showReservationDialog.value = true;
  console.log('showReservationDialog.value:', showReservationDialog.value);
}

// ▼ BookingDialog用のイベントハンドラー
function handleBookingDialogClose() {
  showReservationDialog.value = false;
}

async function handleBookingSubmit(bookingRequest: CreateBookingRequest) {
  console.log('予約確定:', bookingRequest);
  
  try {
    // 予約APIを呼び出し
    const response = await createBooking(bookingRequest);
    
    console.log('予約成功:', response);
    
    // 成功メッセージを表示
    const formattedDate = bookingRequest.usageDate.replace(/-/g, '/');
    showMessage('予約完了', 
      `予約が確定しました。\nスタジオ: ${getStudioNameFromId(bookingRequest.studioId)}\n日付: ${formattedDate}\nコマ: ${bookingRequest.period}`, 
      'success');
    
    // 予約成功後、データを再取得
    await fetchStudiosData();
    showReservationDialog.value = false;
  } catch (err) {
    console.error('予約エラー:', err);
    const errorMessage = err instanceof Error ? err.message : '予約に失敗しました';
    
    // エラーメッセージを表示
    showMessage('予約エラー', 
      `${errorMessage}\n\nもう一度お試しください。`, 
      'error');
  }
}

// ▼ スタジオIDからスタジオ名を取得する関数
function getStudioNameFromId(studioId: string): string {
  const studioMapping: Record<string, string> = {
    'A': 'Aスタ',
    'B': 'Bスタ',
    'C': 'Cスタ'
  };
  return studioMapping[studioId] || studioId;
}

// ▼ メッセージダイアログを表示する関数
function showMessage(title: string, message: string, type: 'success' | 'error' | 'warning' | 'info') {
  messageDialog.value = {
    title,
    message,
    type,
  };
  showMessageDialog.value = true;
}

function handleMessageDialogClose() {
  showMessageDialog.value = false;
}

// ▼ 日付を表示用にフォーマット
function formatDateForDisplay(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const weekdays = ['日', '月', '火', '水', '木', '金', '土'];
  const weekday = weekdays[date.getDay()];
  return `${year}/${month}/${day} (${weekday})`;
}
</script>

<style scoped>
/* ===== Design Tokens ===== */
.schedule-page {
  --bg: #F3F4F6;        /* Gray-100 */
  --panel: #FFFFFF;
  --text: #111827;      /* Gray-900 */
  --muted: #6B7280;     /* Gray-500 */
  --border: #E5E7EB;    /* Gray-200 */
  --shadow: 0 6px 16px rgba(0,0,0,.06);

  --primary: #1D4ED8;   /* Blue-500 */
  --ok: #047857;        /* Green-500 */
  --busy: #1D4ED8;      /* Blue-500 */
  --warn: #FBBF24;      /* Amber-400 */
  --cancel: #4B5563;    /* Gray-400 */

  --radius-lg: 16px;
  --radius-md: 12px;
  --radius-sm: 8px;

  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
}

/* ===== Base ===== */
.schedule-page {
  color-scheme: light;
  margin: 0;
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Noto Sans JP", "Hiragino Kaku Gothic ProN", Meiryo, sans-serif;
  color: var(--text);
  background: var(--bg);
  line-height: 1.4;
  min-height: 100vh;
}

.container {
  max-width: 480px; /* スマホ中心、タブレットでもセンターに */
  margin: 0 auto;
  padding: var(--space-5) var(--space-4) var(--space-6);
}

/* ===== Header ===== */
.selection-help { margin: 8px auto 16px; max-width: 32em; padding: 0 16px; color: #4b5563; font-size: 14px; line-height: 1.6; }
.header {
  padding-top: 20px;
  text-align: center;
  margin-bottom: var(--space-4);
}

.title {
  font-weight: 700;
  font-size: 20px;
  margin: 0 0 var(--space-3);
}

.datebar {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 10px 14px;
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow);
  font-weight: 600;
}

.datebar .date-picker {
  flex: 1;
}

.datebar .icon {
  font-size: 18px;
  color: var(--muted);
}

/* ===== Loading, Error, Empty States ===== */
.loading-state, .error-state, .empty-state {
  text-align: center;
  padding: var(--space-6, 24px);
  color: var(--muted, #6B7280);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border, #E5E7EB);
  border-top: 3px solid var(--primary, #3B82F6);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto var(--space-3, 12px);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon {
  font-size: 32px;
  margin-bottom: var(--space-3, 12px);
}

.btn-retry {
  min-height: 44px;
  margin-top: var(--space-3, 12px);
  padding: var(--space-2, 8px) var(--space-4, 16px);
  background: var(--primary, #3B82F6);
  color: white;
  border: none;
  border-radius: var(--radius-sm, 8px);
  font-weight: 600;
  cursor: pointer;
}

.btn-retry:focus-visible { outline: 3px solid #111827; outline-offset: 3px; }
.btn-retry:hover {
  background: #2563eb;
}

@media (prefers-reduced-motion: reduce) {
  .loading-spinner { animation: none; }
}
</style>

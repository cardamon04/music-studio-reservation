<template>
  <ScheduleTemplate>
    <template #header>
      <div class="mobile-header">
        <h1>予約状況</h1>
        <div class="mobile-controls">
          <div class="calendar-container">
            <WeekCalendar v-model:selected="date" />
          </div>
          <AppButton @click="reload" :disabled="loading" class="reload-button">
            <span class="reload-icon">↻</span>
            <span class="reload-text">再読込</span>
          </AppButton>
        </div>
      </div>
    </template>

    <div class="mobile-content">
      <p v-if="error" class="error">ERROR: {{ error }}</p>
      <p v-else-if="loading" class="loading">読み込み中...</p>

      <StudioGrid
        v-else
        ref="studioGridRef"
        :studios="studios"
        :periods="periods"
        :get-cell="(st, p) => store.getCell(st, p as any)"
        @cell-click="handleCellClick"
      />
    </div>

    <!-- 予約ダイアログ -->
    <BookingDialog
      :is-visible="showBookingDialog"
      :selected-studio="selectedStudio"
      :selected-period="selectedPeriod"
      :usage-date="date"
      @close="closeBookingDialog"
      @submit="handleBookingSubmit"
    />

    <!-- メッセージダイアログ -->
    <MessageDialog
      :is-visible="showMessageDialog"
      :title="messageDialog.title"
      :message="messageDialog.message"
      :details="messageDialog.details"
      :type="messageDialog.type"
      :buttons="messageDialog.buttons"
      @close="closeMessageDialog"
    />
  </ScheduleTemplate>
</template>

<script setup lang="ts">
import { storeToRefs } from "pinia";
import { watch, ref } from "vue";
import ScheduleTemplate from "@/components/templates/ScheduleTemplate.vue";
import AppButton from "@/components/atoms/AppButton.vue";
import StudioGrid from "@/components/organisms/StudioGrid.vue";
import WeekCalendar from "@/components/molecules/WeekCalendar.vue";
import BookingDialog from "@/components/organisms/BookingDialog.vue";
import MessageDialog, { type MessageButton } from "@/components/atoms/MessageDialog.vue";
import { useScheduleStore } from "@/stores/schedule";
import { createBooking, type CreateBookingRequest } from "@/api/bookingApi";
import { getPeriodTimes, type PeriodTime } from "@/api/periodTimeApi";

const store = useScheduleStore();
const { usageDate: date, studios, periods, loading, error } = storeToRefs(store);

// 予約ダイアログの状態管理
const showBookingDialog = ref(false);
const selectedStudio = ref("");
const selectedPeriod = ref("");

// StudioGridの参照
const studioGridRef = ref<InstanceType<typeof StudioGrid> | null>(null);

// メッセージダイアログの状態管理
const showMessageDialog = ref(false);
const messageDialog = ref({
  title: "",
  message: "",
  details: "",
  type: "info" as "success" | "error" | "warning" | "info",
  buttons: [] as MessageButton[]
});

function reload() {
  void store.load();
}

/**
 * 期間時刻を読み込む
 */
async function loadPeriodTimes() {
  try {
    console.log("=== 期間時刻読み込み処理開始 ===");
    console.log("API呼び出し: /period-times");
    const periodTimes = await getPeriodTimes();
    console.log("取得した期間時刻データ:", periodTimes);
    console.log("期間時刻データ件数:", periodTimes.length);
    
    if (studioGridRef.value) {
      console.log("StudioGridに期間時刻データを設定中...");
      studioGridRef.value.setPeriodTimes(periodTimes);
      console.log("StudioGridに期間時刻データを設定完了");
    } else {
      console.warn("studioGridRefがnullです - StudioGridコンポーネントがマウントされていません");
    }
    console.log("=== 期間時刻読み込み処理終了 ===");
  } catch (error) {
    console.error("=== 期間時刻読み込み処理エラー ===");
    console.error("期間時刻の読み込みに失敗しました:", error);
    console.error("エラーの詳細:", error);
  }
}

/**
 * メッセージダイアログを表示する
 */
function showMessage(title: string, message: string, details?: string, type: "success" | "error" | "warning" | "info" = "info") {
  messageDialog.value = {
    title,
    message,
    details: details ?? "",
    type,
    buttons: [
      { text: "OK", class: "primary", action: () => closeMessageDialog() }
    ] as MessageButton[]
  };
  showMessageDialog.value = true;
}

/**
 * メッセージダイアログを閉じる
 */
function closeMessageDialog() {
  showMessageDialog.value = false;
}

/**
 * セルクリック時の処理
 * 空きセルがクリックされた時に予約ダイアログを表示する
 */
function handleCellClick(studioId: string, period: string) {
  selectedStudio.value = studioId;
  selectedPeriod.value = period;
  showBookingDialog.value = true;
}

/**
 * 予約ダイアログを閉じる
 */
function closeBookingDialog() {
  showBookingDialog.value = false;
  selectedStudio.value = "";
  selectedPeriod.value = "";
}

/**
 * 予約確定時の処理
 * バックエンドAPIに予約データを送信する
 */
async function handleBookingSubmit(booking: CreateBookingRequest) {
  try {
    console.log("予約データを送信中:", booking);

    // APIに予約データを送信
    const response = await createBooking(booking);

    console.log("予約作成成功:", response);

    // 成功メッセージを表示
    showMessage(
      "予約完了",
      "予約が正常に作成されました。",
      `予約番号: ${response.bookingId}`,
      "success"
    );

    // 予約作成後、カレンダーを再読み込み
    await reload();
    closeBookingDialog();

  } catch (error) {
    console.error("予約作成エラー:", error);

    // エラーメッセージを表示
    const errorMessage = error instanceof Error ? error.message : '不明なエラー';
    showMessage(
      "予約エラー",
      "予約の作成に失敗しました。",
      errorMessage,
      "error"
    );
  }
}

// 初回ロード
void store.load();
void loadPeriodTimes();

// 日付が変わったら再読込
watch(date, () => { 
  void store.load(); 
  void loadPeriodTimes();
});
</script>

<style scoped>
.error { 
  color: #dc2626; 
  padding: 1rem;
  background: #fef2f2;
  border-radius: 8px;
  margin: 1rem 0;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
}

.mobile-header {
  padding: 1rem;
  background: white;
  border-bottom: 1px solid #e5e7eb;
}

.mobile-header h1 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #111827;
  margin: 0 0 1rem 0;
}

.mobile-controls {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

.calendar-container {
  display: flex;
  justify-content: center;
}

.reload-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  transition: background-color 0.2s;
  min-width: 120px;
}

.reload-button:hover:not(:disabled) {
  background: #2563eb;
}

.reload-button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.reload-icon {
  font-size: 1.2rem;
}

.reload-text {
  font-size: 0.9rem;
}

.mobile-content {
  padding: 1rem;
  min-height: calc(100vh - 200px);
}

/* タブレット以上 */
@media (min-width: 768px) {
  .mobile-controls {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    gap: 2rem;
  }
  
  .calendar-container {
    flex: 1;
    max-width: 500px;
  }
  
  .reload-button {
    flex-shrink: 0;
    min-width: 140px;
  }
}

/* デスクトップ以上 */
@media (min-width: 1024px) {
  .mobile-header {
    padding: 1.5rem 2rem;
  }
  
  .mobile-content {
    padding: 2rem;
  }
  
  .mobile-controls {
    flex-direction: row;
    align-items: flex-start;
    justify-content: flex-start;
    gap: 3rem;
  }
  
  .calendar-container {
    flex: 0 0 auto;
    max-width: none;
  }
  
  .reload-button {
    flex-shrink: 0;
    min-width: 160px;
    padding: 1rem 2rem;
    font-size: 1rem;
  }
}

/* 大画面以上 */
@media (min-width: 1200px) {
  .mobile-controls {
    gap: 4rem;
  }
  
  .reload-button {
    min-width: 180px;
    padding: 1.25rem 2.5rem;
    font-size: 1.1rem;
  }
}
</style>

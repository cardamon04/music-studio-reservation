<template>
  <div class="mobile-studio-grid">
    <!-- モバイル表示: カード形式 -->
    <div class="mobile-view">
      <div v-for="studio in studios" :key="studio" class="studio-card">
        <div class="studio-header" @click="toggleStudio(studio)">
          <h3 class="studio-name">{{ studio }}</h3>
          <div class="toggle-icon" :class="{ 'expanded': expandedStudios.has(studio) }">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6,9 12,15 18,9"></polyline>
            </svg>
          </div>
        </div>
        <div v-show="expandedStudios.has(studio)" class="periods-grid">
          <div
            v-for="period in periods"
            :key="studio + period"
            class="period-card"
            :class="{ 'clickable-period': cell(studio, period)?.status === '空' }"
            @click="handleCellClick(studio, period)"
          >
            <div class="period-time">
              <div class="period-name">{{ period }}</div>
              <div v-if="getPeriodTimeDisplay(period)" class="period-time-range">
                {{ getPeriodTimeDisplay(period) }}
              </div>
            </div>
            <template v-if="cell(studio, period)">
              <span :class="['status-badge', `status-${getBadgeClass(cell(studio, period)!.status)}`]">
                {{ cell(studio, period)!.status }}
              </span>
              <div
                v-if="cell(studio, period)!.graceExpired"
                class="grace-expired"
              >
                10分経過
              </div>
              <div
                v-if="cell(studio, period)!.reservationType"
                class="reservation-info"
              >
                {{ cell(studio, period)!.reservationType }}
                <span v-if="cell(studio, period)!.eventName" class="event-name">
                  {{ cell(studio, period)!.eventName }}
                </span>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- デスクトップ表示: テーブル形式 -->
    <div class="desktop-view">
      <div class="overflow-auto">
        <table class="min-w-full border border-gray-400 border-collapse">
          <thead>
            <tr>
              <th class="p-2 border border-gray-400 w-32 text-left">スタジオ</th>
              <th
                v-for="p in periods"
                :key="p"
                class="p-2 border border-gray-400 min-w-24 text-left"
              >
                <div class="period-header">
                  <div class="period-name">{{ p }}</div>
                  <div v-if="getPeriodTimeDisplay(p)" class="period-time-range">
                    {{ getPeriodTimeDisplay(p) }}
                  </div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="st in studios" :key="st">
              <td class="p-2 border border-gray-400 font-medium">{{ st }}</td>
              <td
                v-for="p in periods"
                :key="st + p"
                class="p-2 border border-gray-400 align-top"
                :class="{ 'clickable-cell': cell(st, p)?.status === '空' }"
                @click="handleCellClick(st, p)"
              >
                <template v-if="cell(st, p)">
                  <span :class="['badge', `badge-${getBadgeClass(cell(st, p)!.status)}`]">
                    {{ cell(st, p)!.status }}
                  </span>
                  <span
                    v-if="cell(st, p)!.graceExpired"
                    class="ml-2 text-xs px-2 py-0.5 rounded border border-red-400 text-red-600"
                  >
                    10分経過
                  </span>
                  <div
                    v-if="cell(st, p)!.reservationType"
                    class="text-xs text-gray-500 mt-1"
                  >
                    {{ cell(st, p)!.reservationType }}
                    <span v-if="cell(st, p)!.eventName" class="block text-blue-600 font-medium">
                      {{ cell(st, p)!.eventName }}
                    </span>
                  </div>
                </template>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

    <script setup lang="ts">
    import { ref, reactive, computed } from 'vue';
    import type { PeriodTime } from '@/api/periodTimeApi';
    
    type UiStatus = "空" | "予約済" | "使用中" | "キャンセル";
    type Cell = {
      status: UiStatus;
      bookingId?: string;
      reservationType?: string;
      eventName?: string; // イベント予約の場合のイベント名
      graceExpired: boolean;
    };

    const props = defineProps<{
      studios: string[];
      periods: string[];
      getCell?: (studioId: string, period: string) => Cell | undefined;
    }>();

    // 予約ダイアログの表示状態を管理
    const emit = defineEmits<{
      cellClick: [studioId: string, period: string];
    }>();

    // 折りたたみ状態を管理
    const expandedStudios = ref(new Set<string>());

    // 期間時刻情報を管理
    const periodTimes = ref<PeriodTime[]>([]);

    // 初期状態で最初のスタジオを展開
    if (props.studios.length > 0) {
      expandedStudios.value.add(props.studios[0]);
    }

    const cell = (st: string, p: string) => props.getCell?.(st, p);

    /**
     * 期間の時刻情報を取得
     */
    function getPeriodTime(periodId: string): PeriodTime | undefined {
      return periodTimes.value.find(pt => pt.periodId === periodId);
    }

    /**
     * 期間の時刻表示文字列を生成
     */
    function getPeriodTimeDisplay(periodId: string): string {
      console.log(`=== getPeriodTimeDisplay(${periodId}) 処理開始 ===`);
      const periodTime = getPeriodTime(periodId);
      console.log(`期間時刻検索結果:`, periodTime);
      console.log(`現在のperiodTimes.value:`, periodTimes.value);
      
      if (periodTime) {
        const display = `${periodTime.startTime} - ${periodTime.endTime}`;
        console.log(`表示文字列生成: ${display}`);
        console.log(`=== getPeriodTimeDisplay(${periodId}) 処理終了 ===`);
        return display;
      }
      console.log(`期間時刻が見つかりません: ${periodId}`);
      console.log(`=== getPeriodTimeDisplay(${periodId}) 処理終了 ===`);
      return '';
    }

    /**
     * 期間時刻情報を設定
     */
    function setPeriodTimes(times: PeriodTime[]) {
      console.log("=== StudioGrid: 期間時刻データ受信 ===");
      console.log("受信した期間時刻データ:", times);
      console.log("受信データ件数:", times.length);
      periodTimes.value = times;
      console.log("periodTimes.valueに設定完了:", periodTimes.value);
      console.log("=== StudioGrid: 期間時刻データ設定完了 ===");
    }

    // 外部からアクセス可能にする
    defineExpose({
      setPeriodTimes
    });

    /**
     * スタジオの折りたたみ/展開を切り替える
     */
    function toggleStudio(studio: string) {
      if (expandedStudios.value.has(studio)) {
        expandedStudios.value.delete(studio);
      } else {
        expandedStudios.value.add(studio);
      }
    }

    /**
     * セルクリック時の処理
     * 空きセルの場合のみ予約ダイアログを表示する
     */
    function handleCellClick(studioId: string, period: string) {
      const cellData = cell(studioId, period);
      if (cellData?.status === "空") {
        emit("cellClick", studioId, period);
      }
    }

    function getBadgeClass(s: UiStatus): string {
      switch (s) {
        case "空":
          return "empty";
        case "予約済":
          return "reserved";
        case "使用中":
          return "in-use";
        case "キャンセル":
          return "cancelled";
        default:
          return "empty";
      }
    }
    </script>

<style scoped>
/* モバイル表示 */
.mobile-view {
  display: block;
}

.desktop-view {
  display: none;
}

.mobile-studio-grid {
  width: 100%;
}

.studio-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 1rem;
  overflow: hidden;
}

.studio-header {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background-color 0.2s ease;
}

.studio-header:hover {
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
}

.toggle-icon {
  transition: transform 0.3s ease;
  color: #6b7280;
}

.toggle-icon.expanded {
  transform: rotate(180deg);
}

.studio-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.periods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 0.5rem;
  padding: 1rem;
  transition: all 0.3s ease;
  overflow: hidden;
}

.period-card {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0.75rem;
  text-align: center;
  transition: all 0.2s ease;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.period-time {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
  text-align: center;
}

.period-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.25rem;
}

.period-time-range {
  font-size: 0.75rem;
  font-weight: 400;
  color: #6b7280;
  line-height: 1.2;
}

.period-header {
  text-align: center;
}

.period-header .period-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.25rem;
}

.period-header .period-time-range {
  font-size: 0.75rem;
  font-weight: 400;
  color: #6b7280;
  line-height: 1.2;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.status-empty {
  background-color: #f3f4f6;
  color: #6b7280;
  border: 1px solid #d1d5db;
}

.status-reserved {
  background-color: #fef3c7;
  color: #92400e;
  border: 1px solid #f59e0b;
}

.status-in-use {
  background-color: #d1fae5;
  color: #065f46;
  border: 1px solid #10b981;
}

.status-cancelled {
  background-color: #fee2e2;
  color: #991b1b;
  border: 1px solid #ef4444;
}

.grace-expired {
  font-size: 0.625rem;
  color: #dc2626;
  background: #fef2f2;
  padding: 0.125rem 0.25rem;
  border-radius: 3px;
  margin-top: 0.25rem;
}

.reservation-info {
  font-size: 0.625rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

.event-name {
  display: block;
  color: #2563eb;
  font-weight: 500;
  margin-top: 0.125rem;
}

/* クリック可能な期間カード */
.clickable-period {
  cursor: pointer;
  background: #f0f9ff;
  border-color: #0ea5e9;
}

.clickable-period:hover {
  background: #e0f2fe;
  border-color: #0284c7;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.15);
}

.clickable-period:hover::after {
  content: 'タップして予約';
  position: absolute;
  top: -30px;
  left: 50%;
  transform: translateX(-50%);
  background: #374151;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  z-index: 10;
  pointer-events: none;
}

.clickable-period {
  position: relative;
}

/* タブレット以上でデスクトップ表示に切り替え */
@media (min-width: 768px) {
  .mobile-view {
    display: none;
  }
  
  .desktop-view {
    display: block;
  }
}

/* デスクトップ表示のスタイル */
table {
  border-collapse: collapse;
  border-spacing: 0;
  width: 100%;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
}

th, td {
  border: 1px solid #e5e7eb;
  padding: 12px 16px;
  text-align: left;
}

th {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  font-weight: 600;
  color: #374151;
  border-bottom: 2px solid #d1d5db;
  position: sticky;
  top: 0;
  z-index: 10;
}

td {
  background: white;
  transition: background-color 0.2s ease;
}

tr:hover td {
  background-color: #f9fafb;
}

tr:nth-child(even) td {
  background-color: #fafafa;
}

tr:nth-child(even):hover td {
  background-color: #f3f4f6;
}

.clickable-cell {
  cursor: pointer;
  position: relative;
}

.clickable-cell:hover {
  background-color: #e0f2fe !important;
}

.clickable-cell:hover::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2px solid #0ea5e9;
  border-radius: 4px;
  pointer-events: none;
  z-index: 1;
}

.clickable-cell:hover::before {
  content: 'クリックして予約';
  position: absolute;
  top: -30px;
  left: 50%;
  transform: translateX(-50%);
  background: #374151;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  z-index: 10;
  pointer-events: none;
}

.badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  text-align: center;
  min-width: 60px;
}

.badge-empty {
  background-color: #f3f4f6;
  color: #6b7280;
  border: 1px solid #d1d5db;
}

.badge-reserved {
  background-color: #fef3c7;
  color: #92400e;
  border: 1px solid #f59e0b;
}

.badge-in-use {
  background-color: #d1fae5;
  color: #065f46;
  border: 1px solid #10b981;
}

.badge-cancelled {
  background-color: #fee2e2;
  color: #991b1b;
  border: 1px solid #ef4444;
}
</style>

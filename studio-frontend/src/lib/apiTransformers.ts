/**
 * APIレスポンスからフロントエンド用の型に変換するユーティリティ関数
 */

import type {
  BookingCalendarView,
  StudioRow,
  SlotView,
  PeriodId
} from './types';
import type {
  Studio,
  PeriodSlot,
  PeriodStatus
} from './types';

/**
 * バックエンドのステータス文字列をフロントエンドのPeriodStatusに変換
 */
function mapStatusToFrontend(backendStatus: string): PeriodStatus {
  switch (backendStatus) {
    case '空':
      return 'available';
    case '予約済み':
    case '予約確定':
      return 'booked';
    case '使用中':
      return 'in_use';
    case '予約キャンセル':
      return 'canceled';
    default:
      return 'available'; // デフォルトは空き
  }
}

/**
 * 時刻を"HH:mm-HH:mm"形式に変換
 */
function formatTimeRange(startTime: string, endTime: string): string {
  // ISO形式の場合は時刻部分のみ抽出
  const start = startTime.includes('T') ? startTime.split('T')[1].substring(0, 5) : startTime;
  const end = endTime.includes('T') ? endTime.split('T')[1].substring(0, 5) : endTime;
  return `${start}–${end}`;
}

/**
 * 期間IDから表示ラベルを生成
 */
function getPeriodLabel(periodId: PeriodId): string {
  return periodId; // P1, P2, etc.
}

/**
 * APIのSlotViewからフロントエンドのPeriodSlotに変換
 */
function transformSlotView(slot: SlotView): PeriodSlot {
  return {
    id: slot.periodId,
    label: getPeriodLabel(slot.periodId),
    timeRange: formatTimeRange(slot.startTime, slot.endTime),
    status: mapStatusToFrontend(slot.status),
    reservationType: slot.reservationType,
    eventName: slot.eventName
  };
}

/**
 * APIのStudioRowからフロントエンドのStudioに変換
 */
function transformStudioRow(studioRow: StudioRow): Studio {
  return {
    id: studioRow.studioId,
    name: studioRow.studioName,
    periods: studioRow.slots.map(transformSlotView)
  };
}

/**
 * APIのBookingCalendarViewからフロントエンドのStudio配列に変換
 */
export function transformBookingCalendarToStudios(calendarData: BookingCalendarView): Studio[] {
  return calendarData.rows.map(transformStudioRow);
}

/**
 * 日付をAPI用の形式（yyyy-MM-dd）に変換
 */
export function formatDateForApi(date: Date): string {
  return date.toISOString().split('T')[0];
}

/**
 * 日付を表示用の形式（yyyy/MM/dd (曜日)）に変換
 */
export function formatDateForDisplay(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  
  const weekdays = ['日', '月', '火', '水', '木', '金', '土'];
  const weekday = weekdays[date.getDay()];
  
  return `${year}/${month}/${day}（${weekday}）`;
}

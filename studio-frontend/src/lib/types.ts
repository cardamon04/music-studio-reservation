/**
 * 予約システムで使う共通型定義
 */

export type PeriodId = "P1" | "P2" | "P3" | "P4" | "P5";

// 「空き枠」1件（ストアが使う最小情報）
export interface AvailableSlot {
  studioId: string;
  usageDate: string; // yyyy-MM-dd
  period: PeriodId;
}

// バックエンドの返却DTOに合わせた型
export interface SlotView {
  periodId: PeriodId;
  status: "空" | "予約済み" | "予約確定" | "使用中" | "予約キャンセル";
  bookingId?: string;
  reservationType?: "学生レンタル" | "授業レンタル" | "イベント予約";
  eventName?: string; // イベント予約の場合のイベント名
  graceExpired: boolean;
  startTime: string; // "HH:mm"
  endTime: string;   // "HH:mm"
}
export interface StudioRow {
  studioId: string;
  studioName: string;
  slots: SlotView[];
}
export interface BookingCalendarView {
  usageDate: string;     // "yyyy-MM-dd"
  periodOrder: PeriodId[];
  rows: StudioRow[];
}
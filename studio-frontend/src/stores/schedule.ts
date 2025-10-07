import { defineStore } from "pinia";
import { fetchBookingCalendar } from "@/lib/apiCalendar";
import type { PeriodId, BookingCalendarView } from "@/lib/types";

type UiStatus = "空" | "予約済" | "使用中" | "キャンセル";
type Cell = { 
  status: UiStatus; 
  bookingId?: string; 
  reservationType?: string; 
  eventName?: string; // イベント予約の場合のイベント名
  graceExpired: boolean 
};

export const useScheduleStore = defineStore("schedule", {
  state: () => ({
    usageDate: todayIso(),
    studios: [] as string[],
    periods: ["P1","P2","P3","P4","P5"] as PeriodId[],
    cells: {} as Record<string, Cell>, // key: `${studioId}:${periodId}`
    loading: false,
    error: "",
  }),

  actions: {
    setDate(d: string) { this.usageDate = d; },

    async load() {
      this.loading = true; this.error = "";
      try {
        const view: BookingCalendarView = await fetchBookingCalendar(this.usageDate);

        // studios はAPIの rows に合わせて同期（Cなどの幽霊行をなくす）
        this.studios = view.rows.map(r => r.studioId);
        this.periods = view.periodOrder as PeriodId[];

        // ルックアップ作成
        const map: Record<string, Cell> = {};
        for (const row of view.rows) {
          for (const s of row.slots) {
            const ui: UiStatus = mapStatusToUi(s.status);
            map[`${row.studioId}:${s.periodId}`] = {
              status: ui,
              bookingId: s.bookingId,
              reservationType: s.reservationType,
              eventName: s.eventName, // イベント名を保存
              graceExpired: s.graceExpired
            };
          }
        }
        this.cells = map;
      } catch (e: any) {
        this.error = e?.message ?? String(e);
      } finally {
        this.loading = false;
      }
    },

    /** セル情報取得（存在しなければ undefined を返す） */
    getCell(studioId: string, period: PeriodId): Cell | undefined {
      return this.cells[`${studioId}:${period}`];
    },
  },
});

function mapStatusToUi(status: string): UiStatus {
  switch (status) {
    case "空": return "空";
    case "予約済み":
    case "予約確定": return "予約済";
    case "使用中": return "使用中";
    case "予約キャンセル": return "キャンセル";
    default: return "予約済"; // 想定外は安全側で予約済扱い
  }
}

function todayIso(): string {
  const d = new Date();
  const mm = String(d.getMonth()+1).padStart(2,"0");
  const dd = String(d.getDate()).padStart(2,"0");
  return `${d.getFullYear()}-${mm}-${dd}`;
}

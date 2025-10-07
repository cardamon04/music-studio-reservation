// src/types/bookingCalendar.ts
export type SlotView = {
    periodId: string;
    status: "空" | "予約済み" | "予約確定" | "使用中" | "予約キャンセル";
    bookingId?: string;
    reservationType?: "学生レンタル" | "授業レンタル" | "イベント予約";
    eventName?: string; // イベント予約の場合のイベント名
    graceExpired: boolean;
    startTime: string; // "HH:mm"
    endTime: string;   // "HH:mm"
  };

  export type StudioRow = {
    studioId: string;
    studioName: string;
    slots: SlotView[];
  };

  export type BookingCalendarView = {
    usageDate: string;      // "yyyy-MM-dd"
    periodOrder: string[];  // ["P1","P2",...]
    rows: StudioRow[];
  };

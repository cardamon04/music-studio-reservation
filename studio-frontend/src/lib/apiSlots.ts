// src/lib/apiSlots.ts
import type { AvailableSlot, BookingCalendarView, PeriodId } from "@/lib/types";

export async function fetchAvailableSlots(usageDate: string, studioId?: string): Promise<AvailableSlot[]> {
  const q = new URLSearchParams({ date: usageDate });
  if (studioId && studioId.length > 0) q.set("studioId", studioId);

  const res = await fetch(`/api/booking-calendar?${q.toString()}`, {
    method: "GET",
    credentials: "include",
    headers: { Accept: "application/json" },
  });
  if (!res.ok) throw new Error(`GET /api/booking-calendar failed: ${res.status}`);

  const view = (await res.json()) as BookingCalendarView;

  const slots: AvailableSlot[] = [];
  for (const row of view.rows) {
    for (const s of row.slots) {
      const isEmpty = (!("bookingId" in s) || !s.bookingId)  // 予約IDが無い＝空
                   || (("status" in s) && (s as any).status === "空"); // もちろん status==="空" もOK
      if (isEmpty) {
        slots.push({
          studioId: row.studioId,
          usageDate: view.usageDate,
          period: s.periodId as PeriodId,
        });
      }
    }
  }
  return slots;
}

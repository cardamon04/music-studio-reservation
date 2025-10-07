import type { BookingCalendarView } from "@/lib/types";

export async function fetchBookingCalendar(
  usageDate: string,
  studioId?: string
): Promise<BookingCalendarView> {
  const q = new URLSearchParams({ date: usageDate });
  if (studioId && studioId.length > 0) q.set("studioId", studioId);

  const res = await fetch(`/api/booking-calendar?${q.toString()}`, {
    method: "GET",
    credentials: "include",
    headers: { Accept: "application/json" },
  });
  if (!res.ok) throw new Error(`GET /api/booking-calendar failed: ${res.status}`);
  return (await res.json()) as BookingCalendarView;
}
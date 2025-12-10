export async function fetchBookingCalendar(date: string, studioId?: string) {
  const q = new URLSearchParams({ date });
  if (studioId) q.set("studioId", studioId);

  const res = await fetch(`/api/booking-calendar?${q.toString()}`, {
    method: "GET",
    credentials: "include",
    headers: { Accept: "application/json" },
  });
  if (!res.ok) throw new Error(`Failed to fetch calendar: ${res.status}`);
  return await res.json();
}

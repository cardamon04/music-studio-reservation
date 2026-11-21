// 予約作成API
export interface CreateBookingRequest {
  studioId: string;
  period: string;
  usageDate: string;
  reservationType: string;
  members: string[];
  equipmentItems: {
    equipmentId: string;
    quantity: number;
  }[];
  eventName?: string; // イベント予約の場合のイベント名
}

export interface CreateBookingResponse {
  bookingId: string;
  studioId: string;
  period: string;
  usageDate: string;
  reservationType: string;
  status: string;
  message: string;
}

/**
 * 予約を作成する
 * 
 * @param booking 予約データ
 * @returns 作成された予約の情報
 */
export async function createBooking(booking: CreateBookingRequest): Promise<CreateBookingResponse> {
  const response = await fetch('/api/bookings', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
    credentials: 'include',
    body: JSON.stringify(booking),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ error: 'Unknown error' }));
    throw new Error(`予約作成に失敗しました: ${errorData.error || response.statusText}`);
  }

  // The current controller returns 200 only after saving. 202 is not completion.
  if (response.status !== 200) throw new Error('Unrecognised booking outcome');
  const result: unknown = await response.json();
  if (typeof result !== 'object' || result === null) throw new Error('Unrecognised booking response');
  const saved = result as Record<string, unknown>;
  if (typeof saved.bookingId !== 'string' || !saved.bookingId.trim()
    || saved.studioId !== booking.studioId || saved.period !== booking.period
    || saved.usageDate !== booking.usageDate || saved.reservationType !== booking.reservationType
    || typeof saved.status !== 'string' || !saved.status
    || typeof saved.message !== 'string') {
    throw new Error('Unrecognised booking response');
  }
  return saved as unknown as CreateBookingResponse;
}

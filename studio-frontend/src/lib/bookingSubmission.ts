import { computed, reactive, ref } from 'vue';
import { createBooking, type CreateBookingRequest } from '@/api/bookingApi';
import type { Student } from '@/api/studentApi';
import type { Equipment } from '@/api/equipmentApi';

// Presentation state for this tab, retained across client-side route changes only.
// These are transport outcomes, not the domain's BookingStatus.
export const bookingSaveState = ref<'idle' | 'pending' | 'unknown' | 'saved'>('idle');
// Keep the displayed names together with quantities while a submitted form is retained.
export const bookingEquipment = ref<Equipment[]>([]);
export const bookingDraft = reactive({
  reservationType: 'StudentRental', selectedStudents: [] as Student[],
  newStudentNumber: '', equipmentQuantities: {} as Record<string, number>, eventName: '',
});
export const bookingSlot = ref({ date: '', dateLabel: '', studioId: '', studioName: '', periodLabel: '', timeRange: '' });
export const bookingSaveMessage = computed(() => ({
  idle: '', pending: '予約内容を保存しています…',
  unknown: '保存できたか確認できません。再送しないでください。',
  saved: '予約内容を保存しました。',
})[bookingSaveState.value]);

export function resetBookingDraft() {
  if (bookingSaveState.value !== 'idle') return;
  Object.assign(bookingDraft, { reservationType: 'StudentRental', selectedStudents: [], newStudentNumber: '', equipmentQuantities: {}, eventName: '' });
}

export function acknowledgeSavedBooking() {
  if (bookingSaveState.value !== 'saved') return;
  bookingSaveState.value = 'idle';
  resetBookingDraft();
}

export async function submitBookingOnce(request: CreateBookingRequest) {
  if (bookingSaveState.value !== 'idle') return;
  bookingSaveState.value = 'pending';
  try {
    await createBooking(request);
    bookingSaveState.value = 'saved';
  } catch {
    // The current API has no machine-readable guarantee of a pre-write rejection.
    // Neither an HTTP error nor an empty calendar proves the write did not happen.
    bookingSaveState.value = 'unknown';
  }
}

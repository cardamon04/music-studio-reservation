export type PeriodStatus = 'available' | 'booked' | 'in_use' | 'canceled';

export interface PeriodSlot {
  id: string;          // e.g. "P1"
  label: string;       // 表示用 "P1"
  timeRange: string;   // "09:00–10:30"
  status: PeriodStatus;
}

export interface Studio {
  id: string;
  name: string;        // e.g. "Studio A"
  periods: PeriodSlot[];
}

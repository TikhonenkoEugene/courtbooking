export type Schedule = { dayOfWeek: number; openTime: string; closeTime: string };

export type Court = {
  id: number;
  name: string;
  sport: 'TENNIS' | 'PADEL';
  pricePerHour: number;
  facilityId: number;
  facilityName: string;
  city: string;
  address: string;
  schedule: Schedule[];
};

export type Booking = {
  id: number;
  courtId: number;
  startTime: string;
  endTime: string;
  price: number;
  status: 'PENDING_PAYMENT' | 'ACTIVE' | 'CANCELLED';
};

export const sportNames = { TENNIS: 'Теннис', PADEL: 'Падел' };

export const dayNames = ['', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

export function scheduleText(schedule: Schedule[]) {
  if (schedule.length === 0) {
    return 'Не работает';
  }
  return schedule.map((s) => `${dayNames[s.dayOfWeek]} ${s.openTime.slice(0, 5)}–${s.closeTime.slice(0, 5)}`).join(', ');
}

export type Slot = { start: string; end: string };

export type CourtSlots = {
  courtId: number;
  courtName: string;
  sport: 'TENNIS' | 'PADEL';
  pricePerHour: number;
  facilityName: string;
  city: string;
  address: string;
  slots: Slot[];
};

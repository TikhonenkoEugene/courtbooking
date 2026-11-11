export type Facility = { id: number; name: string; city: string; address: string };

export type Schedule = { dayOfWeek: number; openTime: string; closeTime: string };

export type Court = {
  id: number;
  name: string;
  sport: 'TENNIS' | 'PADEL';
  pricePerHour: number;
  schedule: Schedule[];
};

export const sportNames = { TENNIS: 'Теннис', PADEL: 'Падел' };

export const dayNames = ['', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

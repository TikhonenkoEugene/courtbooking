import { FormEvent, useEffect, useState } from 'react';
import { api, getToken } from './api';
import { Booking, Court, CourtSlots, Slot, sportNames } from './types';

type Props = { onBooked: () => void; onUnauthorized: () => void };

function today() {
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  return now.toISOString().slice(0, 10);
}

function time(value: string) {
  return value.slice(11, 16);
}

function isPast(slot: Slot) {
  return new Date(slot.start) < new Date();
}

export default function SearchPage({ onBooked, onUnauthorized }: Props) {
  const [sport, setSport] = useState('');
  const [city, setCity] = useState('');
  const [date, setDate] = useState(today());
  const [from, setFrom] = useState('08:00');
  const [to, setTo] = useState('22:00');
  const [cities, setCities] = useState<string[]>([]);
  const [results, setResults] = useState<CourtSlots[] | null>(null);
  const [selected, setSelected] = useState<{ court: CourtSlots; slot: Slot } | null>(null);
  const [booking, setBooking] = useState<Booking | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api<Court[]>('GET', '/catalog/v1/courts')
      .then((courts) => setCities([...new Set(courts.map((court) => court.city))].sort()))
      .catch((err) => setError(err.message));
  }, []);

  async function search(e?: FormEvent) {
    e?.preventDefault();
    setError('');
    const params = new URLSearchParams({ date, from, to });
    if (sport) params.append('sport', sport);
    if (city) params.append('city', city);
    try {
      const found = await api<CourtSlots[]>('GET', '/booking/v1/slots?' + params);
      setResults(
        found
          .map((court) => ({ ...court, slots: court.slots.filter((slot) => !isPast(slot)) }))
          .filter((court) => court.slots.length > 0),
      );
    } catch (err) {
      setError((err as Error).message);
    }
  }

  function selectSlot(court: CourtSlots, slot: Slot) {
    setSelected({ court, slot });
    setBooking(null);
    setError('');
  }

  async function book() {
    setError('');
    try {
      const created = await api<Booking>('POST', '/booking/v1/bookings', {
        courtId: selected!.court.courtId,
        startTime: selected!.slot.start,
        hours: 1,
      });
      setBooking(created);
      search();
    } catch (err) {
      if (getToken() === null) onUnauthorized();
      setError((err as Error).message);
      search();
    }
  }

  async function pay(success: boolean) {
    setError('');
    try {
      await api('POST', `/payment/v1/payments/${booking!.id}/pay?success=${success}`);
      setSelected(null);
      setBooking(null);
      onBooked();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <>
      <form className="card card-body shadow-sm mb-4" onSubmit={search}>
        <div className="row g-3 align-items-end">
          <div className="col-md-2">
            <label className="form-label">Вид спорта</label>
            <select className="form-select" value={sport} onChange={(e) => setSport(e.target.value)}>
              <option value="">Все</option>
              <option value="TENNIS">Теннис</option>
              <option value="PADEL">Падел</option>
            </select>
          </div>
          <div className="col-md-3">
            <label className="form-label">Город</label>
            <select className="form-select" value={city} onChange={(e) => setCity(e.target.value)}>
              <option value="">Все</option>
              {cities.map((name) => (
                <option key={name}>{name}</option>
              ))}
            </select>
          </div>
          <div className="col-md-2">
            <label className="form-label">Дата</label>
            <input
              type="date"
              className="form-control"
              min={today()}
              value={date}
              onChange={(e) => setDate(e.target.value)}
              required
            />
          </div>
          <div className="col-md-3">
            <label className="form-label">Время</label>
            <div className="input-group">
              <span className="input-group-text">с</span>
              <input type="time" step="3600" className="form-control" value={from} onChange={(e) => setFrom(e.target.value)} required />
              <span className="input-group-text">до</span>
              <input type="time" step="3600" className="form-control" value={to} onChange={(e) => setTo(e.target.value)} required />
            </div>
          </div>
          <div className="col-md-2">
            <button className="btn btn-success w-100">Найти слоты</button>
          </div>
        </div>
      </form>

      {error && <div className="alert alert-danger">{error}</div>}

      {selected && (
        <div className="card mb-4 border-success">
          <div className="card-body">
            {!booking ? (
              <>
                <h5 className="card-title">Подтвердите бронь</h5>
                <p className="mb-3">
                  {selected.court.courtName}, {selected.court.facilityName}
                  <br />
                  {new Date(selected.slot.start).toLocaleDateString('ru-RU')}, {time(selected.slot.start)}–{time(selected.slot.end)}
                  <br />
                  Стоимость: <b>{selected.court.pricePerHour} ₽</b>
                </p>
                <button className="btn btn-success me-2" onClick={book}>
                  Забронировать
                </button>
                <button className="btn btn-outline-secondary" onClick={() => setSelected(null)}>
                  Отмена
                </button>
              </>
            ) : (
              <>
                <h5 className="card-title">Бронь #{booking.id} ждёт оплаты</h5>
                <p>
                  Сумма: <b>{booking.price} ₽</b>
                </p>
                <button className="btn btn-success me-2" onClick={() => pay(true)}>
                  Оплатить
                </button>
                <button className="btn btn-outline-danger" onClick={() => pay(false)}>
                  Оплата не прошла
                </button>
              </>
            )}
          </div>
        </div>
      )}

      {results !== null && results.length === 0 && (
        <p className="text-muted">Свободных слотов нет. Попробуйте другую дату или время.</p>
      )}

      <div className="row g-3">
        {results?.map((court) => (
          <div className="col-md-6" key={court.courtId}>
            <div className="card h-100 shadow-sm">
              <div className="card-body">
                <div className="d-flex justify-content-between">
                  <h5 className="card-title mb-1">{court.courtName}</h5>
                  <span className="badge bg-secondary align-self-start">{sportNames[court.sport]}</span>
                </div>
                <p className="mb-1">{court.facilityName}</p>
                <p className="text-muted small mb-2">
                  {court.city}, {court.address} · {court.pricePerHour} ₽ / час
                </p>
                <div className="d-flex flex-wrap gap-2">
                  {court.slots.map((slot) => {
                    const active = selected?.court.courtId === court.courtId && selected.slot.start === slot.start;
                    return (
                      <button
                        key={slot.start}
                        className={'btn btn-sm ' + (active ? 'btn-success' : 'btn-outline-success')}
                        onClick={() => selectSlot(court, slot)}
                      >
                        {time(slot.start)}–{time(slot.end)}
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </>
  );
}

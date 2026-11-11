import { useEffect, useState } from 'react';
import { api, getToken } from './api';
import { Booking, Court } from './types';

const statuses = {
  PENDING_PAYMENT: { text: 'Ждёт оплаты', color: 'warning' },
  ACTIVE: { text: 'Активна', color: 'success' },
  CANCELLED: { text: 'Отменена', color: 'secondary' },
};

const CANCEL_HOURS_BEFORE_START = 2;

function canCancel(booking: Booking) {
  const hoursLeft = (new Date(booking.startTime).getTime() - Date.now()) / 3_600_000;
  return booking.status !== 'CANCELLED' && hoursLeft >= CANCEL_HOURS_BEFORE_START;
}

export default function BookingsPage({ onUnauthorized }: { onUnauthorized: () => void }) {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [courts, setCourts] = useState<Record<number, Court>>({});
  const [error, setError] = useState('');
  const [info, setInfo] = useState('');

  async function load() {
    try {
      setBookings(await api<Booking[]>('GET', '/booking/v1/bookings'));
      const list = await api<Court[]>('GET', '/catalog/v1/courts');
      setCourts(Object.fromEntries(list.map((court) => [court.id, court])));
    } catch (err) {
      if (getToken() === null) onUnauthorized();
      setError((err as Error).message);
    }
  }

  useEffect(() => {
    load();
    const timer = setInterval(load, 2000);
    return () => clearInterval(timer);
  }, []);

  async function pay(booking: Booking) {
    try {
      await api('POST', `/payment/v1/payments/${booking.id}/pay?success=true`);
      load();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  async function cancel(booking: Booking) {
    const paid = booking.status === 'ACTIVE';
    const question = paid
      ? `Отменить бронь #${booking.id}? Деньги (${booking.price} ₽) вернутся на карту.`
      : `Отменить бронь #${booking.id}?`;
    if (!confirm(question)) {
      return;
    }
    setError('');
    setInfo('');
    try {
      await api('POST', `/booking/v1/bookings/${booking.id}/cancel`);
      setInfo(paid ? `Бронь #${booking.id} отменена, ${booking.price} ₽ вернутся на карту` : `Бронь #${booking.id} отменена`);
      load();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  function formatTime(value: string) {
    return new Date(value).toLocaleString('ru-RU', { dateStyle: 'short', timeStyle: 'short' });
  }

  return (
    <>
      <h4 className="mb-3">Мои брони</h4>
      <p className="text-muted small">Отменить бронь можно не позднее чем за {CANCEL_HOURS_BEFORE_START} часа до начала игры.</p>
      {info && <div className="alert alert-success">{info}</div>}
      {error && <div className="alert alert-danger">{error}</div>}
      {bookings.length === 0 ? (
        <p className="text-muted">Броней пока нет</p>
      ) : (
        <table className="table table-hover bg-white">
          <thead>
            <tr>
              <th>#</th>
              <th>Корт</th>
              <th>Начало</th>
              <th>Конец</th>
              <th>Сумма</th>
              <th>Статус</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {bookings.map((booking) => (
              <tr key={booking.id}>
                <td>{booking.id}</td>
                <td>{courts[booking.courtId]?.name ?? booking.courtId}</td>
                <td>{formatTime(booking.startTime)}</td>
                <td>{formatTime(booking.endTime)}</td>
                <td>{booking.price} ₽</td>
                <td>
                  <span className={'badge bg-' + statuses[booking.status].color}>{statuses[booking.status].text}</span>
                </td>
                <td>
                  {booking.status === 'PENDING_PAYMENT' && (
                    <button className="btn btn-sm btn-success me-2" onClick={() => pay(booking)}>
                      Оплатить
                    </button>
                  )}
                  {booking.status !== 'CANCELLED' && (
                    <button
                      className="btn btn-sm btn-outline-danger"
                      disabled={!canCancel(booking)}
                      title={canCancel(booking) ? '' : `До начала меньше ${CANCEL_HOURS_BEFORE_START} часов, отмена невозможна`}
                      onClick={() => cancel(booking)}
                    >
                      Отменить
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}

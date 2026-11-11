import { FormEvent, useEffect, useState } from 'react';
import { api, getToken } from './api';
import { Court, Facility, dayNames, sportNames } from './types';

type Props = { facilityId: number; onBack: () => void; onUnauthorized: () => void };

type DayForm = { works: boolean; openTime: string; closeTime: string };

const days = [1, 2, 3, 4, 5, 6, 7];

function defaultSchedule(): DayForm[] {
  return days.map(() => ({ works: true, openTime: '08:00', closeTime: '22:00' }));
}

function scheduleFromCourt(court: Court): DayForm[] {
  return days.map((day) => {
    const item = court.schedule.find((s) => s.dayOfWeek === day);
    return item
      ? { works: true, openTime: item.openTime.slice(0, 5), closeTime: item.closeTime.slice(0, 5) }
      : { works: false, openTime: '08:00', closeTime: '22:00' };
  });
}

function scheduleText(court: Court) {
  if (court.schedule.length === 0) {
    return 'Не работает';
  }
  return court.schedule
    .map((s) => `${dayNames[s.dayOfWeek]} ${s.openTime.slice(0, 5)}–${s.closeTime.slice(0, 5)}`)
    .join(', ');
}

const emptyForm = { name: '', sport: 'TENNIS', pricePerHour: '' };

export default function FacilityPage({ facilityId, onBack, onUnauthorized }: Props) {
  const [facility, setFacility] = useState<Facility | null>(null);
  const [facilityForm, setFacilityForm] = useState({ name: '', city: '', address: '' });
  const [courts, setCourts] = useState<Court[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [schedule, setSchedule] = useState<DayForm[]>(defaultSchedule());
  const [editId, setEditId] = useState<number | null>(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  function handleError(err: unknown) {
    if (getToken() === null) {
      onUnauthorized();
    } else {
      setError((err as Error).message);
    }
  }

  async function load() {
    try {
      const loaded = await api<Facility>('GET', `/catalog/v1/owner/facilities/${facilityId}`);
      setFacility(loaded);
      setFacilityForm({ name: loaded.name, city: loaded.city, address: loaded.address });
      setCourts(await api<Court[]>('GET', `/catalog/v1/owner/facilities/${facilityId}/courts`));
    } catch (err) {
      handleError(err);
    }
  }

  useEffect(() => {
    load();
  }, [facilityId]);

  async function saveFacility(e: FormEvent) {
    e.preventDefault();
    setError('');
    setMessage('');
    try {
      setFacility(await api<Facility>('PUT', `/catalog/v1/owner/facilities/${facilityId}`, facilityForm));
      setMessage('Спорткомплекс сохранён');
    } catch (err) {
      handleError(err);
    }
  }

  async function saveCourt(e: FormEvent) {
    e.preventDefault();
    setError('');
    setMessage('');
    const body = {
      name: form.name,
      sport: form.sport,
      pricePerHour: Number(form.pricePerHour),
      schedule: days
        .filter((day) => schedule[day - 1].works)
        .map((day) => ({
          dayOfWeek: day,
          openTime: schedule[day - 1].openTime,
          closeTime: schedule[day - 1].closeTime,
        })),
    };
    try {
      if (editId === null) {
        await api('POST', `/catalog/v1/owner/facilities/${facilityId}/courts`, body);
      } else {
        await api('PUT', `/catalog/v1/owner/courts/${editId}`, body);
      }
      cancelEdit();
      load();
    } catch (err) {
      handleError(err);
    }
  }

  function startEdit(court: Court) {
    setEditId(court.id);
    setForm({ name: court.name, sport: court.sport, pricePerHour: String(court.pricePerHour) });
    setSchedule(scheduleFromCourt(court));
  }

  function cancelEdit() {
    setEditId(null);
    setForm(emptyForm);
    setSchedule(defaultSchedule());
  }

  function changeDay(index: number, changes: Partial<DayForm>) {
    setSchedule(schedule.map((day, i) => (i === index ? { ...day, ...changes } : day)));
  }

  async function removeCourt(court: Court) {
    if (!confirm(`Удалить корт «${court.name}»?`)) {
      return;
    }
    try {
      await api('DELETE', `/catalog/v1/owner/courts/${court.id}`);
      load();
    } catch (err) {
      handleError(err);
    }
  }

  return (
    <>
      <button className="secondary" onClick={onBack}>
        ← Все спорткомплексы
      </button>
      <h2>{facility?.name}</h2>

      <form className="card" onSubmit={saveFacility}>
        <h3>Спорткомплекс</h3>
        <div className="row">
          <label>
            Название
            <input value={facilityForm.name} onChange={(e) => setFacilityForm({ ...facilityForm, name: e.target.value })} required />
          </label>
          <label>
            Город
            <input value={facilityForm.city} onChange={(e) => setFacilityForm({ ...facilityForm, city: e.target.value })} required />
          </label>
          <label>
            Адрес
            <input
              value={facilityForm.address}
              onChange={(e) => setFacilityForm({ ...facilityForm, address: e.target.value })}
              required
            />
          </label>
        </div>
        <button type="submit">Сохранить</button>
        {message && <span className="success">{message}</span>}
      </form>

      <form className="card" onSubmit={saveCourt}>
        <h3>{editId === null ? 'Новый корт' : `Редактирование корта #${editId}`}</h3>
        <div className="row">
          <label>
            Название
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          </label>
          <label>
            Спорт
            <select value={form.sport} onChange={(e) => setForm({ ...form, sport: e.target.value })}>
              <option value="TENNIS">Теннис</option>
              <option value="PADEL">Падел</option>
            </select>
          </label>
          <label>
            Цена за час
            <input
              type="number"
              min="1"
              value={form.pricePerHour}
              onChange={(e) => setForm({ ...form, pricePerHour: e.target.value })}
              required
            />
          </label>
        </div>

        <h4>График работы</h4>
        <table className="schedule">
          <tbody>
            {schedule.map((day, index) => (
              <tr key={index}>
                <td>
                  <label className="inline">
                    <input type="checkbox" checked={day.works} onChange={(e) => changeDay(index, { works: e.target.checked })} />
                    {dayNames[index + 1]}
                  </label>
                </td>
                <td>
                  <input
                    type="time"
                    value={day.openTime}
                    disabled={!day.works}
                    onChange={(e) => changeDay(index, { openTime: e.target.value })}
                  />
                </td>
                <td>
                  <input
                    type="time"
                    value={day.closeTime}
                    disabled={!day.works}
                    onChange={(e) => changeDay(index, { closeTime: e.target.value })}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {error && <p className="error">{error}</p>}
        <button type="submit">{editId === null ? 'Создать корт' : 'Сохранить'}</button>
        {editId !== null && (
          <button type="button" className="secondary" onClick={cancelEdit}>
            Отмена
          </button>
        )}
      </form>

      <h3>Корты</h3>
      {courts.length === 0 ? (
        <p>Кортов пока нет</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Название</th>
              <th>Спорт</th>
              <th>Цена за час</th>
              <th>График</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {courts.map((court) => (
              <tr key={court.id}>
                <td>{court.id}</td>
                <td>{court.name}</td>
                <td>{sportNames[court.sport]}</td>
                <td>{court.pricePerHour}</td>
                <td>{scheduleText(court)}</td>
                <td>
                  <button className="secondary" onClick={() => startEdit(court)}>
                    Изменить
                  </button>
                  <button className="danger" onClick={() => removeCourt(court)}>
                    Удалить
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}

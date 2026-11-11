import { FormEvent, useEffect, useState } from 'react';
import { api, getToken } from './api';
import { Facility } from './types';

type Props = { onOpen: (id: number) => void; onUnauthorized: () => void };

const emptyForm = { name: '', city: '', address: '' };

export default function FacilitiesPage({ onOpen, onUnauthorized }: Props) {
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');

  function handleError(err: unknown) {
    if (getToken() === null) {
      onUnauthorized();
    } else {
      setError((err as Error).message);
    }
  }

  async function load() {
    try {
      setFacilities(await api<Facility[]>('GET', '/catalog/v1/owner/facilities'));
    } catch (err) {
      handleError(err);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function create(e: FormEvent) {
    e.preventDefault();
    setError('');
    try {
      await api('POST', '/catalog/v1/owner/facilities', form);
      setForm(emptyForm);
      load();
    } catch (err) {
      handleError(err);
    }
  }

  async function remove(facility: Facility) {
    if (!confirm(`Удалить спорткомплекс «${facility.name}» вместе со всеми кортами?`)) {
      return;
    }
    try {
      await api('DELETE', `/catalog/v1/owner/facilities/${facility.id}`);
      load();
    } catch (err) {
      handleError(err);
    }
  }

  return (
    <>
      <form className="card" onSubmit={create}>
        <h2>Новый спорткомплекс</h2>
        <div className="row">
          <label>
            Название
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          </label>
          <label>
            Город
            <input value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} required />
          </label>
          <label>
            Адрес
            <input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} required />
          </label>
        </div>
        {error && <p className="error">{error}</p>}
        <button type="submit">Создать</button>
      </form>

      <h2>Мои спорткомплексы</h2>
      {facilities.length === 0 ? (
        <p>Спорткомплексов пока нет</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Название</th>
              <th>Город</th>
              <th>Адрес</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {facilities.map((facility) => (
              <tr key={facility.id}>
                <td>{facility.id}</td>
                <td>
                  <button className="link" onClick={() => onOpen(facility.id)}>
                    {facility.name}
                  </button>
                </td>
                <td>{facility.city}</td>
                <td>{facility.address}</td>
                <td>
                  <button onClick={() => onOpen(facility.id)}>Корты</button>
                  <button className="danger" onClick={() => remove(facility)}>
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

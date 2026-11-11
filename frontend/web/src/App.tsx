import { useState } from 'react';
import { getToken, setToken } from './api';
import LoginPage from './LoginPage';
import SearchPage from './SearchPage';
import BookingsPage from './BookingsPage';

type Tab = 'search' | 'bookings';

export default function App() {
  const [loggedIn, setLoggedIn] = useState(getToken() !== null);
  const [tab, setTab] = useState<Tab>('search');

  function logout() {
    setToken(null);
    setLoggedIn(false);
  }

  return (
    <>
      <nav className="navbar navbar-expand navbar-dark bg-success mb-4">
        <div className="container">
          <span className="navbar-brand">CourtBook</span>
          {loggedIn && (
            <>
              <ul className="navbar-nav me-auto">
                <li className="nav-item">
                  <button className={'nav-link btn btn-link' + (tab === 'search' ? ' active' : '')} onClick={() => setTab('search')}>
                    Поиск кортов
                  </button>
                </li>
                <li className="nav-item">
                  <button className={'nav-link btn btn-link' + (tab === 'bookings' ? ' active' : '')} onClick={() => setTab('bookings')}>
                    Мои брони
                  </button>
                </li>
              </ul>
              <button className="btn btn-outline-light btn-sm" onClick={logout}>
                Выйти
              </button>
            </>
          )}
        </div>
      </nav>

      <div className="container">
        {!loggedIn && <LoginPage onLogin={() => setLoggedIn(true)} />}
        {loggedIn && tab === 'search' && <SearchPage onBooked={() => setTab('bookings')} onUnauthorized={logout} />}
        {loggedIn && tab === 'bookings' && <BookingsPage onUnauthorized={logout} />}
      </div>
    </>
  );
}

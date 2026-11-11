import { useState } from 'react';
import { getToken, setToken } from './api';
import AuthPage from './AuthPage';
import FacilitiesPage from './FacilitiesPage';
import FacilityPage from './FacilityPage';
import './styles.css';

export default function App() {
  const [loggedIn, setLoggedIn] = useState(getToken() !== null);
  const [facilityId, setFacilityId] = useState<number | null>(null);

  function logout() {
    setToken(null);
    setLoggedIn(false);
    setFacilityId(null);
  }

  let page;
  if (!loggedIn) {
    page = <AuthPage onLogin={() => setLoggedIn(true)} />;
  } else if (facilityId === null) {
    page = <FacilitiesPage onOpen={setFacilityId} onUnauthorized={logout} />;
  } else {
    page = <FacilityPage facilityId={facilityId} onBack={() => setFacilityId(null)} onUnauthorized={logout} />;
  }

  return (
    <div className="container">
      <div className="header">
        <h1>CourtBook CRM</h1>
        {loggedIn && <button onClick={logout}>Выйти</button>}
      </div>
      {page}
    </div>
  );
}

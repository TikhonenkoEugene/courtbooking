import { FormEvent, useState } from 'react';
import { api, setToken } from './api';

type TokenResponse = { userId: number; role: string; token: string };

export default function AuthPage({ onLogin }: { onLogin: () => void }) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError('');
    try {
      const response = await api<TokenResponse>('POST', `/identity/v1/auth/owner/${mode}`, { email, password });
      setToken(response.token);
      onLogin();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <form className="card" onSubmit={submit}>
      <h2>{mode === 'login' ? 'Вход владельца' : 'Регистрация владельца'}</h2>
      <label>
        Почта
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
      </label>
      <label>
        Пароль
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} minLength={6} required />
      </label>
      {error && <p className="error">{error}</p>}
      <button type="submit">{mode === 'login' ? 'Войти' : 'Зарегистрироваться'}</button>
      <button type="button" className="link" onClick={() => setMode(mode === 'login' ? 'register' : 'login')}>
        {mode === 'login' ? 'Нет аккаунта? Регистрация' : 'Уже есть аккаунт? Вход'}
      </button>
    </form>
  );
}

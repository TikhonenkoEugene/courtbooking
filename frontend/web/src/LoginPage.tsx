import { FormEvent, useEffect, useState } from 'react';
import { api, setToken } from './api';

type TokenResponse = { userId: number; role: string; token: string };

const RESEND_SECONDS = 30;

export default function LoginPage({ onLogin }: { onLogin: () => void }) {
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [codeSent, setCodeSent] = useState(false);
  const [error, setError] = useState('');
  const [info, setInfo] = useState('');
  const [secondsLeft, setSecondsLeft] = useState(0);

  useEffect(() => {
    if (secondsLeft === 0) {
      return;
    }
    const timer = setTimeout(() => setSecondsLeft(secondsLeft - 1), 1000);
    return () => clearTimeout(timer);
  }, [secondsLeft]);

  async function sendCode() {
    setError('');
    setInfo('');
    try {
      await api('POST', '/identity/v1/auth/otp/request', { phone });
      setCodeSent(true);
      setCode('');
      setSecondsLeft(RESEND_SECONDS);
      return true;
    } catch (err) {
      setError((err as Error).message);
      return false;
    }
  }

  async function requestCode(e: FormEvent) {
    e.preventDefault();
    await sendCode();
  }

  async function resendCode() {
    if (await sendCode()) {
      setInfo('Новый код отправлен, предыдущий больше не действует');
    }
  }

  async function verifyCode(e: FormEvent) {
    e.preventDefault();
    setError('');
    try {
      const response = await api<TokenResponse>('POST', '/identity/v1/auth/otp/verify', { phone, code });
      setToken(response.token);
      onLogin();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <div className="row justify-content-center">
      <div className="col-md-5">
        <div className="card shadow-sm">
          <div className="card-body">
            <h4 className="card-title mb-3">Вход по номеру телефона</h4>

            {!codeSent ? (
              <form onSubmit={requestCode}>
                <div className="mb-3">
                  <label className="form-label">Телефон</label>
                  <input
                    className="form-control"
                    placeholder="+79990001122"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    required
                  />
                </div>
                <button className="btn btn-success w-100">Получить код</button>
              </form>
            ) : (
              <form onSubmit={verifyCode}>
                <p className="text-muted">Код отправлен на {phone}</p>
                <div className="mb-3">
                  <label className="form-label">Код из сообщения</label>
                  <input
                    className="form-control"
                    maxLength={6}
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    required
                  />
                </div>
                <button className="btn btn-success w-100 mb-2">Войти</button>
                <button
                  type="button"
                  className="btn btn-outline-secondary w-100 mb-2"
                  disabled={secondsLeft > 0}
                  onClick={resendCode}
                >
                  {secondsLeft > 0 ? `Запросить код повторно через ${secondsLeft} с` : 'Запросить код повторно'}
                </button>
                <button type="button" className="btn btn-link w-100" onClick={() => setCodeSent(false)}>
                  Изменить номер
                </button>
              </form>
            )}

            {info && <div className="alert alert-info mt-3 mb-0">{info}</div>}
            {error && <div className="alert alert-danger mt-3 mb-0">{error}</div>}
          </div>
        </div>
      </div>
    </div>
  );
}

import { sm3 } from 'sm-crypto';

export function hashPasswordForTransport(password: string) {
  const raw = password.trim();
  if (raw.length < 6 || raw.length > 64) {
    throw new Error('密码长度需为 6-64 位。');
  }
  return sm3(raw);
}

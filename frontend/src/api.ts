export type ApiEnvelope<T> = { success?: boolean; code?: string; message?: string; data?: T };
export type AuthSession<TUser> = { token: string; user: TUser };
type MaybeUser = { nickname?: string; username?: string };

const USER_STORAGE_KEY = 'petshop_user';
const TOKEN_STORAGE_KEY = 'petshop_token';

function looksBrokenText(value: string) {
  if (!value) return false;
  const trimmed = value.trim();
  return /^[?]+$/.test(trimmed) || /[�锟]/.test(trimmed);
}

function repairStoredUser<T>(user: T) {
  if (!user || typeof user !== 'object') return user;
  const candidate = user as MaybeUser;
  if (candidate.username && looksBrokenText(candidate.nickname || '')) {
    return { ...candidate, nickname: candidate.username } as T;
  }
  return user;
}

export function getAuthToken() {
  return localStorage.getItem(TOKEN_STORAGE_KEY) || '';
}

export function getStoredUser<T>() {
  const raw = localStorage.getItem(USER_STORAGE_KEY);
  return raw ? repairStoredUser(JSON.parse(raw) as T) : null;
}

export function saveStoredUser<T>(user: T) {
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(repairStoredUser(user)));
}

export function saveAuthSession<TUser>(session: AuthSession<TUser>) {
  localStorage.setItem(TOKEN_STORAGE_KEY, session.token);
  saveStoredUser(session.user);
}

export function clearAuthSession() {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
  localStorage.removeItem(USER_STORAGE_KEY);
}

export async function apiFetch(input: RequestInfo | URL, init: RequestInit = {}) {
  const headers = new Headers(init.headers);
  const token = getAuthToken();
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  return await fetch(input, { ...init, headers });
}

export async function readError(res: Response) {
  try {
    const data = await readApiEnvelope<unknown>(res);
    if (data.message && data.message !== res.statusText) return data.message;
    if ((data as Record<string, unknown>).error && (data as Record<string, unknown>).error !== res.statusText) return String((data as Record<string, unknown>).error);
    if (res.status === 400) return '请求内容不符合要求，请检查填写项。';
    if (res.status === 401) return '请先登录后再继续操作。';
    if (res.status === 403) return '没有权限执行这个操作。';
    if (res.status === 404) return '没有找到对应的数据。';
    return '请求失败，请检查填写内容。';
  } catch {
    try {
      const text = await res.text();
      if (text.trim()) return text.trim();
    } catch {
      return '请求失败，请检查后端服务是否正常运行。';
    }
    return '请求失败，请检查后端服务是否正常运行。';
  }
}

export async function readApiEnvelope<T>(res: Response): Promise<ApiEnvelope<T> & Record<string, unknown>> {
  return await res.json() as ApiEnvelope<T> & Record<string, unknown>;
}

export async function readApiData<T>(res: Response): Promise<T> {
  const body = await readApiEnvelope<T>(res);
  if (body && Object.prototype.hasOwnProperty.call(body, 'data')) {
    return body.data as T;
  }
  return body as unknown as T;
}

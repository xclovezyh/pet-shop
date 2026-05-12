export type ApiEnvelope<T> = { success?: boolean; code?: string; message?: string; data?: T };

export async function readError(res: Response) {
  try {
    const data = await readApiEnvelope<unknown>(res);
    if (data.message && data.message !== res.statusText) return data.message;
    if ((data as Record<string, unknown>).error && (data as Record<string, unknown>).error !== res.statusText) return String((data as Record<string, unknown>).error);
    if (res.status === 400) return '请求内容不符合要求，请检查填写项。';
    if (res.status === 401) return '账号或密码不正确。';
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

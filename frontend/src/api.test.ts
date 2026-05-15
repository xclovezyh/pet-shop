import { readApiData, readError } from './api';
import fs from 'fs';
import path from 'path';

describe('api helpers', () => {
  it('unwraps unified api response envelopes', async () => {
    const response = mockResponse({
      success: true,
      code: 'SUCCESS',
      message: 'ok',
      data: { id: 1, name: 'alice' }
    }, 200);

    const data = await readApiData<{ id: number; name: string }>(response);

    expect(data).toEqual({ id: 1, name: 'alice' });
  });

  it('falls back to raw body for legacy responses', async () => {
    const response = mockResponse([{ id: 1 }, { id: 2 }], 200);

    const data = await readApiData<Array<{ id: number }>>(response);

    expect(data).toHaveLength(2);
    expect(data[1].id).toBe(2);
  });

  it('prefers backend message when reading errors', async () => {
    const response = mockResponse({
      success: false,
      code: 'USER_400_001',
      message: '用户名已被注册'
    }, 400, 'Bad Request');

    await expect(readError(response)).resolves.toBe('用户名已被注册');
  });

  it('keeps legacy admin APIs out of the user-facing entry', () => {
    const mainSource = fs.readFileSync(path.join(__dirname, 'main.tsx'), 'utf8');

    expect(mainSource).not.toContain('AdminPage');
    expect(mainSource).not.toContain('admin=');
    expect(mainSource).not.toContain('/users/${user.id}/role');
    expect(mainSource).not.toContain("role === 'SUPER_ADMIN'");
  });

  it('keeps admin display name editing wired in the standalone admin entry', () => {
    const adminSource = fs.readFileSync(path.join(__dirname, 'admin.tsx'), 'utf8');

    expect(adminSource).toContain('/display-name');
    expect(adminSource).toContain('updateAdminDisplayName');
  });
});

function mockResponse(body: unknown, status: number, statusText = '') {
  return {
    status,
    statusText,
    json: async () => body,
    text: async () => typeof body === 'string' ? body : JSON.stringify(body)
  } as Response;
}

import { expect, test } from '@playwright/test';

test('home page should load and navigate between core sections', async ({ page }) => {
  await page.goto('/');
  const nav = page.getByRole('navigation');

  await expect(page.locator('text=萌宠集市')).toBeVisible();
  await expect(page.locator('text=登录 / 注册')).toBeVisible();

  await nav.getByRole('button', { name: '市场', exact: true }).click();
  await expect(page.locator('text=售卖 / 互换 / 领养帖子')).toBeVisible();
  await expect(page.locator('text=正在加载交易帖子')).toBeVisible();

  await nav.getByRole('button', { name: '日常', exact: true }).click();
  await expect(page.locator('text=用户日常分享')).toBeVisible();
  await expect(page.locator('text=正在加载用户日常')).toBeVisible();

  await page.getByRole('button', { name: '登录 / 注册', exact: true }).click();
  await expect(page.getByRole('button', { name: '密码登录', exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: '验证码登录', exact: true })).toBeVisible();
});

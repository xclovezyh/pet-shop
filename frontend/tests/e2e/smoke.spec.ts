import { expect, test } from '@playwright/test';

test('home page should load and navigate between core sections', async ({ page }) => {
  await page.goto('/');

  await expect(page.getByTestId('nav-home')).toBeVisible();
  await expect(page.getByTestId('login-entry')).toBeVisible();

  await page.getByTestId('nav-market').click();
  await expect(page.getByTestId('nav-market')).toHaveClass(/active/);

  await page.getByTestId('nav-moments').click();
  await expect(page.getByTestId('nav-moments')).toHaveClass(/active/);

  await page.getByTestId('login-entry').click();
  await expect(page.getByTestId('account-page')).toBeVisible();
  await expect(page.getByTestId('account-mode-password')).toBeVisible();
  await expect(page.getByTestId('account-mode-sms')).toBeVisible();
  await expect(page.getByTestId('account-mode-register')).toBeVisible();
});

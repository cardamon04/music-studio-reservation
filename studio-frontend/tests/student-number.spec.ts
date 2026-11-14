import { test, expect, type Page } from '@playwright/test';

async function openBooking(page: Page) {
  await page.route('**/api/booking-calendar?*', route => route.fulfill({ json: {
    rows: [{ studioId: 'A', studioName: 'Aスタ', slots: [{
      periodId: 'P1', status: '空', startTime: '09:00', endTime: '10:30', graceExpired: false,
    }] }],
  } }));
  await page.route('**/api/equipment', route => route.fulfill({ json: [] }));
  await page.goto('/schedule');
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
}

test('学生番号を入力中も項目名と入力例を確認でき、ラベルから入力欄へ移れる', async ({ page }) => {
  await openBooking(page);
  const input = page.locator('.student-input-group input');
  await input.fill('AB12');
  await input.scrollIntoViewIfNeeded();
  if (process.env.NUMBER_EVIDENCE) await page.screenshot({ path: `../docs/ui-evidence/student-number-${process.env.NUMBER_EVIDENCE}-375.png` });
  await expect(page.getByLabel('学生番号で追加', { exact: true })).toHaveValue('AB12');
  await expect(page.getByText('例: AB12345678', { exact: true })).toBeVisible();
  await expect(input).toHaveAccessibleDescription('例: AB12345678');
  await page.getByText('学生番号で追加', { exact: true }).click();
  await expect(input).toBeFocused();
});

for (const width of [320, 375, 1280]) {
  test(`${width}px幅で学生番号の案内と入力・追加ボタンが収まる`, async ({ page }) => {
    await page.setViewportSize({ width, height: 812 });
    await openBooking(page);
    const input = page.getByLabel('学生番号で追加', { exact: true });
    await input.fill('AB12345678');
    for (const locator of [input, page.getByText('例: AB12345678', { exact: true }), page.getByRole('button', { name: '追加', exact: true })]) {
      await locator.scrollIntoViewIfNeeded();
      const box = await locator.boundingBox();
      expect(box!.x).toBeGreaterThanOrEqual(0);
      expect(box!.x + box!.width).toBeLessThanOrEqual(width);
      expect(await locator.evaluate(el => el.scrollWidth <= el.clientWidth)).toBe(true);
    }
  });
}

test('空白は追加せず、番号を繰り返し追加しても重複せず、キャンセルで消える', async ({ page }) => {
  await openBooking(page);
  const input = page.getByLabel('学生番号で追加', { exact: true });
  const add = page.getByRole('button', { name: '追加', exact: true });
  await input.fill('   ');
  await expect(add).toBeDisabled();
  await input.press('Enter');
  await expect(page.locator('.student-item')).toHaveCount(0);
  await input.fill(' ab12345678 ');
  await input.press('Enter');
  await expect(input).toHaveValue('');
  await expect(page.locator('.student-item')).toContainText('AB12345678');
  for (let i = 0; i < 3; i++) {
    await input.fill('AB12345678');
    await add.click();
    await expect(page.locator('.student-item')).toHaveCount(1);
  }
  await page.getByRole('button', { name: 'キャンセル', exact: true }).click();
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
  await expect(input).toHaveValue('');
  await expect(page.locator('.student-item')).toHaveCount(0);
  await expect(page.getByText('例: AB12345678', { exact: true })).toBeVisible();
});

test('学生検索の通信失敗時にも番号で追加でき、戻る・進むで入力が残らない', async ({ page }) => {
  await page.route('**/api/health', route => route.fulfill({ body: 'ok' }));
  await page.goto('/ping');
  await openBooking(page);
  await page.route('**/api/students/search?*', route => route.fulfill({ status: 503, body: 'Unavailable' }));
  await page.getByLabel('学生名で検索', { exact: true }).fill('田中');
  await expect(page.getByRole('alert')).toBeVisible();
  await page.getByLabel('学生番号で追加', { exact: true }).fill('AB12345678');
  await page.getByRole('button', { name: '追加', exact: true }).click();
  await expect(page.locator('.student-item')).toHaveCount(1);
  await page.getByRole('button', { name: 'キャンセル', exact: true }).click();
  await page.goBack();
  await expect(page).toHaveURL(/\/ping$/);
  await page.goForward();
  await expect(page).toHaveURL(/\/schedule$/);
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
  await expect(page.getByLabel('学生番号で追加', { exact: true })).toHaveValue('');
  await expect(page.locator('.student-item')).toHaveCount(0);
});

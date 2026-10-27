import { test, expect, type Page } from '@playwright/test';

const student = { studentNumber: 'AB12345678', name: '田中 テスト' };

async function openBooking(page: Page) {
  await page.route('**/api/booking-calendar?*', route => route.fulfill({ json: {
    rows: [{ studioId: 'A', studioName: 'Aスタ', slots: [{
      periodId: 'P1', status: '空', startTime: '09:00', endTime: '10:30', graceExpired: false,
    }] }],
  } }));
  await page.route('**/api/equipment', route => route.fulfill({ json: [] }));
  await page.goto('/schedule');
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
  return page.getByPlaceholder('学生名で検索 (例: 田中)');
}

test('学生検索の候補をTabとEnterで選び、入力へ戻って次の学生を探せる', async ({ page }) => {
  const input = await openBooking(page);
  await page.route('**/api/students/search?*', route => route.fulfill({ json: [student] }));
  await input.fill('田中');
  await expect(page.locator('.search-result-item')).toBeVisible();
  await input.scrollIntoViewIfNeeded();
  if (process.env.UI_EVIDENCE) await page.screenshot({ path: `../docs/ui-evidence/student-search-${process.env.UI_EVIDENCE}-375.png` });
  await input.press('Tab');
  const candidate = page.getByRole('button', { name: '田中 テスト AB12345678 を追加' });
  await expect(candidate).toBeFocused();
  await candidate.press('Enter');
  await expect(page.locator('.student-list')).toContainText('田中 テスト');
  await expect(input).toBeFocused();
  await expect(input).toHaveValue('');
  await expect(candidate).toBeHidden();
});

for (const action of ['clear', 'cancel', 'type'] as const) {
  test(`検索中に${action}しても遅い結果を次の入力に表示しない`, async ({ page }) => {
    const input = await openBooking(page);
    let release!: () => void;
    const gate = new Promise<void>(resolve => { release = resolve; });
    await page.route('**/api/students/search?*', async route => {
      await gate;
      await route.fulfill({ json: [student] });
    });
    const started = page.waitForRequest(r => r.url().includes('/students/search?'));
    await input.fill('田中');
    await started;
    if (action === 'clear') await input.fill('');
    if (action === 'cancel') {
      await page.getByRole('button', { name: 'キャンセル', exact: true }).click();
      await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
    }
    if (action === 'type') {
      await page.locator('.form-select').selectOption('ClassRental');
      await page.locator('.form-select').selectOption('StudentRental');
    }
    const finished = page.waitForResponse(r => r.url().includes('/students/search?'));
    release();
    await finished;
    await input.focus();
    await expect(input).toHaveValue('');
    await expect(page.locator('.search-result-item')).toHaveCount(0);
    await expect(page.getByRole('status')).toBeEmpty();
  });
}

test('旧検索の失敗で新しい候補を消さない', async ({ page }) => {
  const input = await openBooking(page);
  let release!: () => void;
  const gate = new Promise<void>(resolve => { release = resolve; });
  await page.route('**/api/students/search?*', async route => {
    if (new URL(route.request().url()).searchParams.get('name') === '旧') {
      await gate;
      await route.fulfill({ status: 503, body: 'Unavailable' });
    } else await route.fulfill({ json: [student] });
  });
  const started = page.waitForRequest(r => r.url().includes('/students/search?'));
  await input.fill('旧');
  await started;
  await input.fill('田中');
  const candidate = page.getByRole('button', { name: '田中 テスト AB12345678 を追加' });
  await expect(candidate).toBeVisible();
  const finished = page.waitForResponse(r => new URL(r.url()).searchParams.get('name') === '旧');
  release();
  await finished;
  await expect(candidate).toBeVisible();
  await expect(page.getByRole('alert')).toBeHidden();
});

test('320pxで候補をSpaceで追加し、重複を除外してキャンセルできる', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 640 });
  const input = await openBooking(page);
  let writes = 0;
  await page.route('**/api/bookings', route => { writes++; return route.abort(); });
  await page.route('**/api/students/search?*', route => route.fulfill({ json: [student] }));
  await input.fill('田中');
  const candidate = page.getByRole('button', { name: '田中 テスト AB12345678 を追加' });
  await expect(candidate).toBeVisible();
  await input.press('Tab');
  const bounds = (await candidate.boundingBox())!;
  expect(bounds.width).toBeGreaterThanOrEqual(44);
  expect(bounds.height).toBeGreaterThanOrEqual(44);
  expect(bounds.x).toBeGreaterThanOrEqual(0);
  expect(bounds.x + bounds.width).toBeLessThanOrEqual(320);
  await candidate.press('Space');
  await expect(input).toBeFocused();
  await input.fill('田中');
  await expect(page.getByRole('status')).toHaveText('追加できる学生が見つかりませんでした。');
  await expect(page.locator('.student-item')).toHaveCount(1);
  await page.getByRole('button', { name: 'キャンセル', exact: true }).click();
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).press('Enter');
  await expect(input).toHaveValue('');
  await expect(page.locator('.student-item')).toHaveCount(0);
  await page.keyboard.press('Escape');
  expect(writes).toBe(0);
});

test('入力を変えた後に届いた古い検索結果で候補が戻らない', async ({ page }) => {
  const input = await openBooking(page);
  let release!: () => void;
  const gate = new Promise<void>(resolve => { release = resolve; });
  await page.route('**/api/students/search?*', async route => {
    const old = new URL(route.request().url()).searchParams.get('name') === '田';
    if (old) await gate;
    await route.fulfill({ json: old ? [student] : [{ studentNumber: 'CD12345678', name: '山田 テスト' }] });
  });
  const started = page.waitForRequest(r => r.url().includes('/students/search?'));
  await input.fill('田');
  await started;
  await input.fill('山田');
  await expect(page.getByRole('button', { name: '山田 テスト CD12345678 を追加' })).toBeVisible();
  const oldResponse = page.waitForResponse(r => new URL(r.url()).searchParams.get('name') === '田');
  release();
  await oldResponse;
  await expect(page.getByRole('button', { name: '田中 テスト AB12345678 を追加' })).toBeHidden();
  await expect(page.getByRole('button', { name: '山田 テスト CD12345678 を追加' })).toBeVisible();
});

test('検索中・0件・失敗を区別し、入力を保ったまま再試行できる', async ({ page }) => {
  const input = await openBooking(page);
  let release!: () => void;
  const gate = new Promise<void>(resolve => { release = resolve; });
  await page.route('**/api/students/search?*', async route => {
    await gate;
    await route.fulfill({ status: 503, body: 'Unavailable' });
  });
  await input.fill('田中');
  await expect(page.getByRole('status')).toHaveText('学生を検索しています…');
  release();
  await expect(page.getByRole('alert')).toContainText('学生を検索できませんでした。');
  await expect(input).toHaveValue('田中');
  await page.route('**/api/students/search?*', route => route.fulfill({ json: [] }));
  await page.getByRole('button', { name: '学生検索をやり直す' }).click();
  await expect(page.getByRole('status')).toHaveText('追加できる学生が見つかりませんでした。');
  await expect(page.getByRole('alert')).toBeHidden();
});

import { test, expect, type Page } from '@playwright/test';

const calendar = { rows: [{ studioId: 'A', studioName: 'Aスタ', slots: [1, 2, 3, 4, 5].map(n => ({
  periodId: `P${n}`, status: n === 2 ? '予約済み' : '空', startTime: '09:00', endTime: '10:30', graceExpired: false,
})) }] };

test('読み込みを伝え、通信失敗後に同じ日付を再試行できる', async ({ page }) => {
  let release: () => void = () => {};
  const gate = new Promise<void>(resolve => { release = resolve; });
  const dates: string[] = [];
  await page.route('**/api/booking-calendar?*', async route => {
    dates.push(new URL(route.request().url()).searchParams.get('date')!);
    if (dates.length === 1) {
      await gate;
      await route.fulfill({ status: 503, body: 'Unavailable' });
    } else await route.fulfill({ json: calendar });
  });
  await page.goto('/schedule');
  await expect(page.getByRole('status')).toHaveText('予約状況を読み込んでいます…');
  await expect(page.getByRole('button', { name: /空き枠を選ぶ/ })).toHaveCount(0);
  release();
  await expect(page.getByRole('alert')).toContainText('予約状況を読み込めませんでした。');
  await page.getByRole('button', { name: 'もう一度読み込む' }).click();
  await expect(page.getByRole('heading', { name: 'Aスタ' })).toBeVisible();
  expect(dates).toHaveLength(2);
  expect(dates[1]).toBe(dates[0]);
});

async function mockCalendar(page: Page) {
  await page.route('**/api/booking-calendar?*', route => route.fulfill({ json: calendar }));
  await page.route('http://127.0.0.1:5174/api/equipment', route => route.fulfill({ json: [] }));
}

for (const staleStatus of [200, 503]) {
test('日付変更後の旧応答で表示を戻さない（HTTP ' + staleStatus + '）', async ({ page }) => {
  let release: () => void = () => {};
  const gate = new Promise<void>(resolve => { release = resolve; });
  let requests = 0;
  await page.route('**/api/booking-calendar?*', async route => {
    const isFirst = ++requests === 1;
    if (isFirst) await gate;
    await route.fulfill({ status: isFirst ? staleStatus : 200, json: { rows: [{ ...calendar.rows[0], studioName: isFirst ? '旧日のスタジオ' : '選択日のスタジオ' }] } });
  });
  await page.goto('/schedule');
  await expect(page.getByRole('status')).toBeVisible();
  const date = page.getByLabel('日付を選択');
  await date.fill((await date.getAttribute('max'))!);
  await expect(page.getByRole('heading', { name: '選択日のスタジオ' })).toBeVisible();
  const staleResponse = page.waitForResponse(response => response.url().includes('/api/booking-calendar?'));
  release();
  await staleResponse;
  // レスポンス本文の反映まで待ち、旧結果が後から表示されないことを調べる。
  await page.waitForTimeout(100);
  await expect(page.getByRole('heading', { name: '選択日のスタジオ' })).toBeVisible();
  await expect(page.getByRole('heading', { name: '旧日のスタジオ' })).toHaveCount(0);
});
}

test('空き枠をキーボードで選び、キャンセル後に別の枠を選べる', async ({ page }) => {
  await mockCalendar(page);
  let writes = 0;
  page.on('request', request => { if (request.method() === 'POST') writes++; });
  await page.goto('/schedule');
  const first = page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' });
  await first.focus();
  await first.press('Enter');
  await expect(page.getByRole('heading', { name: 'スタジオ予約' })).toBeVisible();
  await page.getByRole('button', { name: 'キャンセル', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'スタジオ予約' })).toBeHidden();
  const last = page.getByRole('button', { name: 'P5 09:00–10:30 空き枠を選ぶ' });
  await last.focus();
  await last.press('Space');
  await expect(page.locator('.info-value').filter({ hasText: /^P5$/ })).toBeVisible();
  await page.getByRole('button', { name: 'キャンセル', exact: true }).click();
  await expect(page.getByRole('button', { name: /P2.*空き枠を選ぶ/ })).toHaveCount(0);
  expect(writes).toBe(0);
});

test('320pxでも全時間枠が横に隠れず、実際の選択方法が分かる', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 640 });
  await mockCalendar(page);
  await page.goto('/schedule');
  await expect(page.getByText('日付を選び、希望する時間の「空き枠を選ぶ」を押してください。')).toBeVisible();
  await expect(page.getByRole('button', { name: '予約する', exact: true })).toHaveCount(0);
  const fits = await page.locator('.periods').evaluate(el => el.scrollWidth <= el.clientWidth);
  expect(fits).toBe(true);
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  for (const button of await page.getByRole('button', { name: /空き枠を選ぶ/ }).all()) {
    const bounds = await button.boundingBox();
    expect(bounds!.width).toBeGreaterThanOrEqual(44);
    expect(bounds!.height).toBeGreaterThanOrEqual(44);
  }
});

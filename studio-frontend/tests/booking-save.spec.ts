import { test, expect, type Page, type Route } from '@playwright/test';

const calendar = { rows: [{ studioId: 'A', studioName: 'Aスタ', slots: [1, 2].map(n => ({
  periodId: `P${n}`, status: '空', startTime: '09:00', endTime: '10:30', graceExpired: false,
})) }] };
async function open(page: Page) {
  await page.route(url => url.pathname.startsWith('/api/'), route => route.fulfill({ status: 404, json: {} }));
  await page.route('**/api/booking-calendar?*', route => route.fulfill({ json: calendar }));
  await page.route('**/api/equipment', route => route.fulfill({ json: [{ id: 'mic', name: 'マイク', stock: 2, isActive: true }] }));
  await page.goto('/schedule');
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
  await page.getByLabel('学生番号で追加').fill('AB12345678');
  await page.getByRole('button', { name: '追加', exact: true }).click();
  await page.getByRole('button', { name: 'マイクを1個増やす' }).click();
}
function success(route: Route) {
  const request = route.request().postDataJSON();
  return route.fulfill({ json: { ...request, bookingId: 'test-booking', status: 'Pending', message: '予約が作成されました' } });
}

test('保存待ちに入力を残し、連打とEnterを受けてもPOSTは一回だけにする', async ({ page }) => {
  await open(page);
  let route!: Route;
  let writes = 0;
  await page.route('**/api/bookings', r => { route = r; writes++; });
  await page.locator('.submit-button').click();
  await expect.poll(() => writes).toBe(1);
  if (process.env.SAVE_EVIDENCE) await page.screenshot({ path: `../docs/ui-evidence/save-${process.env.SAVE_EVIDENCE}-375.png` });
  await expect(page.getByRole('dialog', { name: 'スタジオ予約' })).toBeVisible();
  await expect(page.getByText('予約内容を保存しています…', { exact: true })).toBeVisible();
  await page.locator('.submit-button').click({ clickCount: 2, force: true });
  await page.locator('.submit-button').press('Enter');
  expect(writes).toBe(1);
  await expect(page.locator('.student-item')).toContainText('AB12345678');
  await expect(page.getByLabel('マイクの選択数量')).toHaveText('1個');
  await success(route);
  await expect(page.getByText('予約内容を保存しました。', { exact: true })).toBeVisible();
  await expect(page.locator('.submit-button')).toBeFocused();
});

test('503でイベント名と備品を保持し、閉じて戻っても再送を許可しない', async ({ page }) => {
  await open(page);
  await page.locator('.form-select').selectOption('EventReservation');
  await page.getByPlaceholder('イベント名を入力してください').fill('学内ライブ');
  let writes = 0;
  await page.route('**/api/bookings', route => { writes++; return route.fulfill({ status: 503, json: { error: 'unavailable' } }); });
  await page.locator('.submit-button').click();
  await expect(page.getByText('保存できたか確認できません。再送しないでください。', { exact: true })).toBeVisible();
  await expect(page.getByPlaceholder('イベント名を入力してください')).toHaveValue('学内ライブ');
  await page.keyboard.press('Escape');
  await page.getByRole('button', { name: '送信した内容を表示' }).click();
  await expect(page.getByPlaceholder('イベント名を入力してください')).toHaveValue('学内ライブ');
  await expect(page.getByLabel('マイクの選択数量')).toHaveText('1個');
  await page.locator('.submit-button').press('Enter');
  expect(writes).toBe(1);
});

test('保存成功後の一覧取得失敗は再読込だけ行いPOSTを繰り返さない', async ({ page }) => {
  await open(page);
  let writes = 0;
  await page.route('**/api/bookings', route => { writes++; return success(route); });
  await page.route('**/api/booking-calendar?*', route => route.fulfill({ status: 503, json: {} }));
  await page.locator('.submit-button').click();
  await expect(page.getByText('予約内容を保存しました。', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: '閉じる', exact: true }).click();
  await expect(page.getByText('予約状況を読み込めませんでした。')).toBeVisible();
  await page.route('**/api/booking-calendar?*', route => route.fulfill({ json: calendar }));
  await page.getByRole('button', { name: 'もう一度読み込む' }).click();
  await expect(page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' })).toBeVisible();
  expect(writes).toBe(1);
});

for (const outcome of ['通信断', '400', '409', '空の成功応答', 'JSON以外', '別の枠の応答', '202受付のみ']) {
  test(`${outcome}は保存結果不明として入力を保持し再送しない`, async ({ page }) => {
    await open(page);
    let writes = 0;
    await page.route('**/api/bookings', route => {
      writes++;
      if (outcome === '通信断') return route.abort();
      if (outcome === 'JSON以外') return route.fulfill({ body: '<html>error</html>' });
      if (outcome === '202受付のみ') return route.fulfill({ status: 202, json: { ...route.request().postDataJSON(), bookingId: 'accepted', status: 'Pending', message: 'Accepted' } });
      if (outcome === '別の枠の応答') return route.fulfill({ json: { ...route.request().postDataJSON(), period: 'P2', bookingId: 'other', status: 'Pending', message: '' } });
      return route.fulfill({ status: ['400', '409'].includes(outcome) ? Number(outcome) : 200, json: {} });
    });
    await page.locator('.submit-button').click();
    await expect(page.getByText('保存できたか確認できません。再送しないでください。', { exact: true })).toBeVisible();
    await expect(page.locator('.student-item')).toContainText('AB12345678');
    await page.locator('.submit-button').press('Enter');
    await page.getByRole('button', { name: '閉じる', exact: true }).click();
    await expect(page.getByRole('button', { name: '送信した内容を表示' })).toBeVisible();
    expect(writes).toBe(1);
  });
}

test('待機中にEscapeで閉じても取消せず、戻ると進むの後にも遅れた応答を同じ入力で確認する', async ({ page }) => {
  await open(page);
  let route!: Route;
  let writes = 0;
  await page.route('**/api/bookings', r => { route = r; writes++; });
  await page.locator('.submit-button').click();
  await expect.poll(() => writes).toBe(1);
  await page.keyboard.press('Escape');
  await page.evaluate("import('/src/router/index.ts').then(({ default: router }) => router.push('/ping'))");
  await expect(page).toHaveURL(/\/ping$/);
  await page.route('**/api/equipment', route => route.fulfill({ status: 503, json: {} }));
  await success(route);
  await page.goBack();
  await expect(page.getByText('予約内容を保存しました。', { exact: true })).toBeVisible();
  await page.goForward();
  await expect(page).toHaveURL(/\/ping$/);
  await expect(page.locator('dialog[open]')).toHaveCount(0);
  await page.goBack();
  await page.getByRole('button', { name: '送信した内容を表示' }).click();
  await expect(page.locator('.student-item')).toContainText('AB12345678');
  await expect(page.getByLabel('マイクの選択数量')).toHaveText('1個');
  await expect(page.getByRole('heading', { name: 'スタジオ予約' })).toBeFocused();
  await page.getByRole('button', { name: '閉じる', exact: true }).click();
  await page.getByRole('button', { name: 'P2 09:00–10:30 空き枠を選ぶ' }).click();
  await expect(page.locator('.student-item')).toHaveCount(0);
  expect(writes).toBe(1);
});

test('320pxで結果不明の内容を確認でき、フォーカスは閉じると再表示の間で戻る', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 640 });
  await open(page);
  await page.route('**/api/bookings', route => route.abort());
  await page.locator('.submit-button').click();
  await expect(page.getByText('保存できたか確認できません。再送しないでください。', { exact: true })).toBeVisible();
  const dialog = page.getByRole('dialog', { name: 'スタジオ予約' });
  expect(await dialog.evaluate(el => el.scrollWidth <= el.clientWidth)).toBe(true);
  await page.keyboard.press('Escape');
  const resume = page.getByRole('button', { name: '送信した内容を表示' });
  await resume.click();
  await page.keyboard.press('Escape');
  await expect(resume).toBeFocused();
});

test('離れた画面の古い備品応答で、新しい入力の備品と数量を消さない', async ({ page }) => {
  await page.route('**/api/booking-calendar?*', route => route.fulfill({ json: calendar }));
  let oldEquipment!: Route;
  await page.route('**/api/equipment', route => { oldEquipment = route; });
  await page.goto('/schedule');
  await expect.poll(() => Boolean(oldEquipment)).toBe(true);
  await page.evaluate("import('/src/router/index.ts').then(({ default: router }) => router.push('/ping'))");
  await page.route('**/api/equipment', route => route.fulfill({ json: [{ id: 'mic', name: 'マイク', stock: 2, isActive: true }] }));
  await page.goBack();
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
  await page.getByRole('button', { name: 'マイクを1個増やす' }).click();
  const delivered = page.waitForResponse(response => response.url().endsWith('/api/equipment'));
  await oldEquipment.fulfill({ json: [] });
  await delivered;
  await expect(page.getByLabel('マイクの選択数量')).toHaveText('1個');
});

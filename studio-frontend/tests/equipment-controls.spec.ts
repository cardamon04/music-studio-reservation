import { test, expect, type Page } from '@playwright/test';

const equipment = [
  { id: 'mic', name: 'マイク', stock: 2, isActive: true },
  { id: 'stand', name: 'マイクスタンド', stock: 1, isActive: true },
];

async function openBooking(page: Page, equipmentList = equipment, equipmentStatus = 200) {
  await page.route('**/api/booking-calendar?*', route => route.fulfill({ json: {
    rows: [{ studioId: 'A', studioName: 'Aスタ', slots: [{
      periodId: 'P1', status: '空', startTime: '09:00', endTime: '10:30', graceExpired: false,
    }] }],
  } }));
  await page.route('**/api/equipment', route => route.fulfill({ status: equipmentStatus, json: equipmentList }));
  await page.goto('/schedule');
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
  await page.locator('.form-section').last().scrollIntoViewIfNeeded();
}

test('数量ボタンの名前から備品と増減の操作を区別できる', async ({ page }) => {
  await openBooking(page);
  if (process.env.EQUIPMENT_EVIDENCE) await page.screenshot({ path: `../docs/ui-evidence/equipment-${process.env.EQUIPMENT_EVIDENCE}-375.png` });
  await expect(page.getByRole('button', { name: 'マイクを1個増やす', exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: 'マイクを1個減らす', exact: true })).toBeDisabled();
  await expect(page.getByRole('button', { name: 'マイクスタンドを1個増やす', exact: true })).toBeVisible();
});

test('在庫0個の備品は増減できず選択数量は0個のままになる', async ({ page }) => {
  await openBooking(page, [{ ...equipment[0], stock: 0 }]);
  await expect(page.getByRole('button', { name: 'マイクを1個増やす' })).toBeDisabled();
  await expect(page.getByRole('button', { name: 'マイクを1個減らす' })).toBeDisabled();
  await expect(page.getByRole('status', { name: 'マイクの選択数量' })).toHaveText('0個');
});

test('数量を選んでもキャンセル・Escapeでは送信せず再表示で0個に戻る', async ({ page }) => {
  let writes = 0;
  await page.route('**/api/bookings', route => { writes++; return route.fulfill({ json: {} }); });
  await openBooking(page);
  const trigger = page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' });
  for (const closeWithEscape of [false, true]) {
    await page.getByRole('button', { name: 'マイクを1個増やす', exact: true }).click();
    if (closeWithEscape) await page.keyboard.press('Escape');
    else await page.getByRole('button', { name: 'キャンセル', exact: true }).click();
    await expect(trigger).toBeFocused();
    await trigger.press('Enter');
    await expect(page.getByRole('status', { name: 'マイクの選択数量', exact: true })).toHaveText('0個');
  }
  expect(writes).toBe(0);
});

test('備品の取得失敗から再試行すると数量を選べ、選択した数量だけ送信する', async ({ page }) => {
  await openBooking(page, equipment, 503);
  await expect(page.locator('.error-message')).toContainText('備品一覧の取得に失敗しました');
  await page.route('**/api/equipment', route => route.fulfill({ json: equipment }));
  await page.getByRole('button', { name: '再試行', exact: true }).click();
  await page.getByRole('button', { name: 'マイクを1個増やす', exact: true }).click();
  await page.locator('.form-select').selectOption('ClassRental');
  let requestBody: unknown;
  await page.route('**/api/bookings', route => {
    requestBody = route.request().postDataJSON();
    return route.fulfill({ json: { bookingId: 'test-booking' } });
  });
  await page.getByRole('button', { name: '予約確定', exact: true }).click();
  await expect(page.getByRole('dialog', { name: '予約完了' })).toBeVisible();
  expect(requestBody).toMatchObject({ equipmentItems: [{ equipmentId: 'mic', quantity: 1 }] });
});

test('ブラウザの戻る・進むの後も数量を選び直せる', async ({ page }) => {
  await page.route('**/api/health', route => route.fulfill({ body: 'ok' }));
  await page.goto('/ping');
  await openBooking(page);
  await page.getByRole('button', { name: 'マイクを1個増やす', exact: true }).click();
  await page.goBack();
  await expect(page).toHaveURL(/\/ping$/);
  await page.goForward();
  await expect(page).toHaveURL(/\/schedule$/);
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
  await expect(page.getByRole('status', { name: 'マイクの選択数量', exact: true })).toHaveText('0個');
  await page.getByRole('button', { name: 'マイクを1個増やす', exact: true }).click();
  await expect(page.getByRole('status', { name: 'マイクの選択数量', exact: true })).toHaveText('1個');
});

test('320pxでも長い備品名と44pxの数量ボタンが画面内に収まる', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 640 });
  const longName = 'コンデンサーマイク専用ショックマウント';
  await openBooking(page, [{ id: 'long', name: longName, stock: 2, isActive: true }]);
  const increase = page.getByRole('button', { name: `${longName}を1個増やす` });
  await increase.scrollIntoViewIfNeeded();
  const bounds = (await increase.boundingBox())!;
  expect(bounds.width).toBeGreaterThanOrEqual(44);
  expect(bounds.height).toBeGreaterThanOrEqual(44);
  expect(bounds.x).toBeGreaterThanOrEqual(0);
  expect(bounds.x + bounds.width).toBeLessThanOrEqual(320);
  expect(await page.locator('.dialog-content').evaluate(el => el.scrollWidth <= el.clientWidth)).toBe(true);
  await increase.click();
  await expect(page.getByRole('status', { name: `${longName}の選択数量` })).toHaveText('1個');
});

test('増減後の数量を備品ごとに確認でき、0個と在庫数で操作を止める', async ({ page }) => {
  await openBooking(page);
  const increase = page.getByRole('button', { name: 'マイクを1個増やす', exact: true });
  const decrease = page.getByRole('button', { name: 'マイクを1個減らす', exact: true });
  const quantity = page.getByRole('status', { name: 'マイクの選択数量', exact: true });
  await increase.focus();
  await increase.press('Enter');
  await expect(quantity).toHaveText('1個');
  await expect(page.getByRole('status', { name: 'マイクスタンドの選択数量', exact: true })).toHaveText('0個');
  await increase.press('Space');
  await expect(quantity).toHaveText('2個');
  await expect(increase).toBeDisabled();
  await decrease.click();
  await decrease.click();
  await expect(quantity).toHaveText('0個');
  await expect(decrease).toBeDisabled();
});

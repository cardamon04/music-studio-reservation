import { test, expect, type Page } from '@playwright/test';

async function openBooking(page: Page) {
  await page.route('**/api/booking-calendar?*', route => route.fulfill({ json: {
    rows: [{ studioId: 'A', studioName: 'Aスタ', slots: [1, 2].map(n => ({
      periodId: `P${n}`, status: '空', startTime: '09:00', endTime: '10:30', graceExpired: false,
    })) }],
  } }));
  await page.route('**/api/equipment', route => route.fulfill({ json: [] }));
  await page.goto('/schedule');
  const trigger = page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' });
  await trigger.focus();
  await trigger.press('Enter');
  return trigger;
}

test('予約入力を開くと見出しにフォーカスし、Escapeで選んだ枠に戻る', async ({ page }) => {
  const trigger = await openBooking(page);
  await expect(page.getByRole('heading', { name: 'スタジオ予約' })).toBeVisible();
  if (process.env.UI_EVIDENCE) await page.screenshot({ path: `../docs/ui-evidence/dialog-${process.env.UI_EVIDENCE}-375.png` });
  await expect(page.getByRole('heading', { name: 'スタジオ予約' })).toBeFocused();
  await page.keyboard.press('Escape');
  await expect(page.getByRole('heading', { name: 'スタジオ予約' })).toBeHidden();
  await expect(trigger).toBeFocused();
});

test('320pxの入力画面で下部ボタンまで操作し、背景タップで閉じられる', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 640 });
  const trigger = await openBooking(page);
  const dialog = page.getByRole('dialog', { name: 'スタジオ予約' });
  expect(await dialog.evaluate(el => el.scrollWidth <= el.clientWidth)).toBe(true);
  const cancel = dialog.getByRole('button', { name: 'キャンセル', exact: true });
  await cancel.focus();
  const bounds = (await cancel.boundingBox())!;
  expect(bounds.y).toBeGreaterThanOrEqual(0);
  expect(bounds.y + bounds.height).toBeLessThanOrEqual(640);
  await cancel.press('Enter');
  await expect(trigger).toBeFocused();
  await trigger.press('Enter');
  await page.mouse.click(2, 2);
  await expect(dialog).toBeHidden();
  await expect(trigger).toBeFocused();
});

test('ブラウザの戻るでページを離れ、進むで戻っても背景操作を妨げない', async ({ page }) => {
  await page.route('**/api/health', route => route.fulfill({ body: 'ok' }));
  await page.goto('/ping');
  await openBooking(page);
  await page.goBack();
  await expect(page).toHaveURL(/\/ping$/);
  await expect(page.locator('dialog[open]')).toHaveCount(0);
  await page.goForward();
  await expect(page).toHaveURL(/\/schedule$/);
  const trigger = page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' });
  await trigger.click();
  await expect(page.getByRole('heading', { name: 'スタジオ予約' })).toBeFocused();
  await page.keyboard.press('Escape');
  await expect(trigger).toBeFocused();
});

test('備品の読込中も閉じられ、エラー後の再試行ボタンへフォーカスできる', async ({ page }) => {
  await openBooking(page);
  await page.keyboard.press('Escape');
  let release!: () => void;
  const gate = new Promise<void>(resolve => { release = resolve; });
  await page.route('**/api/equipment', async route => {
    await gate;
    await route.fulfill({ status: 503, body: 'Unavailable' });
  });
  await page.reload();
  const trigger = page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' });
  await trigger.click();
  await expect(page.getByText('備品一覧を読み込み中...')).toBeVisible();
  await page.keyboard.press('Escape');
  await expect(trigger).toBeFocused();
  release();
  await trigger.click();
  const retry = page.getByRole('button', { name: '再試行', exact: true });
  await expect(retry).toBeVisible();
  await retry.focus();
  await expect(retry).toBeFocused();
  await page.route('**/api/equipment', route => route.fulfill({ json: [] }));
  await retry.press('Enter');
  await expect(retry).toBeHidden();
  await page.keyboard.press('Escape');
  await expect(trigger).toBeFocused();
});

test('予約エラーも名前付きモーダルで表示し、閉じた後に枠を選び直せる', async ({ page }) => {
  const trigger = await openBooking(page);
  let writes = 0;
  await page.route('**/api/bookings', route => {
    writes++;
    return route.fulfill({ status: 503, json: { message: 'テスト用の通信エラー' } });
  });
  await page.locator('.form-select').selectOption('ClassRental');
  await page.getByRole('button', { name: '予約確定', exact: true }).click();
  await expect(page.getByRole('heading', { name: '予約エラー' })).toBeVisible();
  await expect(page.getByRole('dialog', { name: '予約エラー' })).toBeVisible();
  await expect(page.getByRole('heading', { name: '予約エラー' })).toBeFocused();
  await page.keyboard.press('Escape');
  await expect(trigger).toBeFocused();
  await trigger.press('Enter');
  await expect(page.getByRole('heading', { name: 'スタジオ予約' })).toBeFocused();
  await page.getByRole('button', { name: 'キャンセル', exact: true }).click();
  expect(writes).toBe(1);
});

test('成功後の一覧再描画で元の枠が消えたら、閉じた後は予約状況の見出しに戻る', async ({ page }) => {
  await openBooking(page);
  await page.route('**/api/bookings', route => route.fulfill({ json: { bookingId: 'test-booking' } }));
  await page.locator('.form-select').selectOption('ClassRental');
  await page.getByRole('button', { name: '予約確定', exact: true }).click();
  await expect(page.getByRole('dialog', { name: '予約完了' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ', includeHidden: true })).toBeAttached();
  await page.getByRole('button', { name: 'OK', exact: true }).click();
  await expect(page.getByRole('heading', { name: '予約状況', exact: true })).toBeFocused();
});

test('TabとShift+Tabで背景に移らず、開閉を繰り返しても元の枠に戻る', async ({ page }) => {
  const trigger = await openBooking(page);
  const dialog = page.getByRole('dialog', { name: 'スタジオ予約' });
  const close = dialog.getByRole('button', { name: '予約入力を閉じる' });
  const cancel = dialog.getByRole('button', { name: 'キャンセル', exact: true });
  await close.focus();
  await page.keyboard.press('Shift+Tab');
  await expect(cancel).toBeFocused();
  await page.keyboard.press('Tab');
  await expect(close).toBeFocused();
  await trigger.evaluate(el => (el as HTMLElement).focus());
  await expect(close).toBeFocused();
  await cancel.click();
  await expect(trigger).toBeFocused();
  await trigger.press('Space');
  await expect(page.getByRole('heading', { name: 'スタジオ予約' })).toBeFocused();
  await page.getByRole('button', { name: '予約入力を閉じる' }).click();
  await expect(trigger).toBeFocused();
});

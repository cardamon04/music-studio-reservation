import { test, expect, type Page } from '@playwright/test';

const students = [
  { studentNumber: 'AB12345678', name: '田中 テスト' },
  { studentNumber: 'CD12345678', name: '鈴木 テスト' },
];

async function openBooking(page: Page) {
  await page.route('**/api/booking-calendar?*', route => route.fulfill({ json: {
    rows: [{ studioId: 'A', studioName: 'Aスタ', slots: [{
      periodId: 'P1', status: '空', startTime: '09:00', endTime: '10:30', graceExpired: false,
    }] }],
  } }));
  await page.route('**/api/equipment', route => route.fulfill({ json: [] }));
  await page.route('**/api/students/search?*', route => route.fulfill({ json: students }));
  await page.goto('/schedule');
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
}

async function addStudent(page: Page, index: number) {
  const student = students[index];
  await page.getByLabel('学生名で検索', { exact: true }).fill(student.name);
  await page.getByRole('button', { name: `${student.name} ${student.studentNumber} を追加` }).click();
}

test('選択から外すボタンで対象学生と操作を区別できる', async ({ page }) => {
  await openBooking(page);
  await addStudent(page, 0);
  await addStudent(page, 1);
  await page.locator('.student-list').scrollIntoViewIfNeeded();
  if (process.env.MEMBER_EVIDENCE) await page.screenshot({ path: `../docs/ui-evidence/member-${process.env.MEMBER_EVIDENCE}-375.png` });
  await expect(page.getByRole('button', { name: '田中 テスト AB12345678 を選択から外す' })).toBeVisible();
  await expect(page.getByRole('button', { name: '鈴木 テスト CD12345678 を選択から外す' })).toBeVisible();
});

test('320px幅でも長い学生名と44pxの操作領域が収まる', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 812 });
  await openBooking(page);
  const longName = '名前が長い学生の表示確認用テスト名前が長い学生の表示確認用テスト';
  await page.route('**/api/students/search?*', route => route.fulfill({ json: [{ ...students[0], name: longName }] }));
  await page.getByLabel('学生名で検索', { exact: true }).fill(longName);
  await page.getByRole('button', { name: `${longName} AB12345678 を追加` }).click();
  const button = page.getByRole('button', { name: `${longName} AB12345678 を選択から外す` });
  const box = await button.boundingBox();
  expect(box!.width).toBeGreaterThanOrEqual(44);
  expect(box!.height).toBeGreaterThanOrEqual(44);
  expect(box!.x + box!.width).toBeLessThanOrEqual(320);
  expect(await page.locator('.student-item').evaluate(el => el.scrollWidth <= el.clientWidth)).toBe(true);
});

test('選択から外した後も次・前・検索へキーボード操作を続けられる', async ({ page }) => {
  await openBooking(page);
  await addStudent(page, 0);
  await addStudent(page, 1);
  const first = page.getByRole('button', { name: '田中 テスト AB12345678 を選択から外す' });
  const second = page.getByRole('button', { name: '鈴木 テスト CD12345678 を選択から外す' });
  await first.focus();
  await page.keyboard.press('Enter');
  await expect(second).toBeFocused();
  await addStudent(page, 0);
  await first.focus();
  await page.keyboard.press('Enter');
  await expect(second).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(page.getByLabel('学生名で検索', { exact: true })).toBeFocused();
  await expect(page.locator('.student-item')).toHaveCount(0);
});

test('繰り返し選び直しても重複せず、キャンセルと再表示で選択が残らない', async ({ page }) => {
  await openBooking(page);
  let writes = 0;
  await page.route('**/api/bookings', route => { writes++; return route.abort(); });
  for (let i = 0; i < 3; i++) {
    await addStudent(page, 0);
    await expect(page.locator('.student-item')).toHaveCount(1);
    await page.getByRole('button', { name: '田中 テスト AB12345678 を選択から外す' }).press('Space');
    await expect(page.locator('.student-item')).toHaveCount(0);
    await expect(page.getByRole('button', { name: '予約確定', exact: true })).toBeDisabled();
  }
  await addStudent(page, 1);
  await page.getByRole('button', { name: 'キャンセル', exact: true }).click();
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
  await expect(page.locator('.student-item')).toHaveCount(0);
  await page.keyboard.press('Escape');
  expect(writes).toBe(0);
});

test('検索の通信失敗を再試行した後も選択から外せる', async ({ page }) => {
  await openBooking(page);
  await page.route('**/api/students/search?*', route => route.fulfill({ status: 503, body: 'Unavailable' }));
  await page.getByLabel('学生名で検索', { exact: true }).fill('田中');
  await expect(page.getByRole('alert')).toContainText('学生を検索できませんでした。');
  await page.route('**/api/students/search?*', route => route.fulfill({ json: students }));
  await page.getByRole('button', { name: '学生検索をやり直す' }).click();
  await page.getByRole('button', { name: '田中 テスト AB12345678 を追加' }).click();
  await page.getByRole('button', { name: '田中 テスト AB12345678 を選択から外す' }).click();
  await expect(page.getByLabel('学生名で検索', { exact: true })).toBeFocused();
  await expect(page.locator('.student-item')).toHaveCount(0);
});

test('キャンセルして戻る・進む操作をした後も選択が残らない', async ({ page }) => {
  await page.route('**/api/health', route => route.fulfill({ body: 'ok' }));
  await page.goto('/ping');
  await openBooking(page);
  await addStudent(page, 0);
  await page.getByRole('button', { name: '田中 テスト AB12345678 を選択から外す' }).click();
  await page.getByRole('button', { name: 'キャンセル', exact: true }).click();
  await page.goBack();
  await expect(page).toHaveURL(/\/ping$/);
  await page.goForward();
  await expect(page).toHaveURL(/\/schedule$/);
  await page.getByRole('button', { name: 'P1 09:00–10:30 空き枠を選ぶ' }).click();
  await expect(page.locator('.student-item')).toHaveCount(0);
});

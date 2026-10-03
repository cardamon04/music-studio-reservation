# スタジオ予約システム API詳細設計書

資料間の相違は[要件定義の未確定事項](要件定義.md#pending-decisions)を参照してください。該当する記述は確認待ちです。この文書への記載は、実装済みであることを保証しません。

## 1. 概要

### 1.1 目的
本ドキュメントは、スタジオ予約システムのREST APIの詳細仕様を定義する。

### 1.2 対象読者
- フロントエンド開発者
- システム統合担当者
- テスト担当者

### 1.3 基本情報
- **ベースURL**: `http://localhost:9000`
- **APIバージョン**: v1
- **認証方式**: 現在の実装はなし（開発版）。受付APIの利用にはTAの認証・権限確認を必要とする。認証方式の実装選定は別途行う
- **レスポンス形式**: JSON
- **文字エンコーディング**: UTF-8

## 2. 共通仕様

### 2.1 HTTPステータスコード
| コード | 説明 |
|--------|------|
| 200 | 成功 |
| 201 | 作成成功 |
| 400 | リクエストエラー |
| 401 | 未認証（受付API） |
| 403 | TA権限なし、または操作実行者の不一致（受付API） |
| 404 | リソース未発見 |
| 409 | 現在の受付状態・数量と操作が矛盾（受付API） |
| 500 | サーバーエラー |

### 2.2 エラーレスポンス形式
```json
{
  "error": "エラーメッセージ",
  "code": "ERROR_CODE",
  "details": "詳細情報（オプション）"
}
```

### 2.3 日付形式
- **形式**: `yyyy-MM-dd` (例: `2025-01-27`)
- **タイムゾーン**: JST (日本標準時)

### 2.4 時刻形式
- **形式**: `HH:mm` (例: `09:00`)

### 2.5 スタジオ・備品のIDと表示名
- `StudioId` / `EquipmentId` はUUIDとし、APIではハイフン区切りのUUID文字列で送受信する。
- IDは作成時に割り当て、表示名の変更後も維持する。検索条件・予約・詳細取得にはIDを使い、表示名からIDを生成・推測しない。
- スタジオは予約カレンダーの `studioId` と `studioName`、備品は一覧・詳細の `id` と `name` で、IDと表示名を分ける。予約内の `equipmentId` は備品の `id` と同じUUIDを参照する。
- 以下のUUIDは仕様説明用の例であり、現在の初期データのIDではない。実装との差は[要件定義](要件定義.md)の「現在の実装との差」を参照する。

### 2.6 予約状態と枠表示
- 予約の `status` は `Confirmed`（予約確定）または `Canceled`（予約キャンセル）。予約作成時に条件確認と必要な備品引当を行い、その場で確定する。承認待ちの状態は設けない。
- カレンダーの `slots[].status` は表示用の英語の列挙値とし、画面で日本語に対応させる。本人確認・Check-in/Check-outは予約状態と分けて記録する。
- Check-out後は枠を `CheckedOut`（利用終了）と表示し、予約の `status` は `Confirmed` のまま維持する。同一スタジオ・利用日・限は1組のみとし、早期退室でも残り時間の再予約を認めない。

## 3. エンドポイント一覧

### 3.1 ヘルスチェック

#### GET /api/ping
システムの稼働状況を確認する。

**リクエスト**
- パラメータ: なし

**レスポンス**
```json
{
  "status": "ok",
  "timestamp": "2025-01-27T10:30:00Z"
}
```

### 3.2 予約カレンダー

#### GET /api/booking-calendar
指定した日付の予約カレンダーを取得する。

**リクエスト**
- **URL**: `/api/booking-calendar`
- **メソッド**: GET
- **パラメータ**:
  - `date` (必須): 日付 (yyyy-MM-dd形式)
  - `studioId` (オプション): スタジオID（UUID）

**レスポンス**
```json
{
  "usageDate": "2025-01-27",
  "periodOrder": ["P1", "P2", "P3", "P4", "P5", "P6"],
  "rows": [
    {
      "studioId": "550e8400-e29b-41d4-a716-446655440001",
      "studioName": "Aスタ",
      "slots": [
        {
          "periodId": "P1",
          "status": "Empty",
          "bookingId": null,
          "reservationType": null,
          "eventName": null,
          "graceExpired": false,
          "startTime": "2025-01-27T09:00:00",
          "endTime": "2025-01-27T10:30:00"
        }
      ]
    }
  ]
}
```

**ステータス値**
- `Empty`: 空。有効な予約が存在しない。日付などの予約条件は別途適用する
- `Confirmed`: 予約確定。確定予約があり、`InUse`・`CheckedOut` の条件に当てはまらない
- `InUse`: 使用中。確定予約の利用時間内で、TAによるCheck-in済み・未Check-out
- `CheckedOut`: 利用終了。確定予約でTAによるCheck-out済み。元の予約枠全体を維持し、再予約不可。予定終了時刻の経過だけではこの状態にしない
- `Canceled`: 予約キャンセル。取消済みの履歴表示。空き枠の判定では有効な予約として扱わない

### 3.3 予約管理

#### POST /api/bookings
新しい予約を作成し、その場で確定する。必要な備品引当も同じ処理で行い、成功時は `201` と `status: "Confirmed"` を返す。条件不成立時は予約や一部の引当だけを残さない。

**リクエスト**
- **URL**: `/api/bookings`
- **メソッド**: POST
- **Content-Type**: `application/json`

**リクエストボディ**
```json
{
  "studioId": "550e8400-e29b-41d4-a716-446655440001",
  "period": "P1",
  "usageDate": "2025-01-27",
  "reservationType": "StudentRental",
  "members": ["AB12345678"],
  "equipmentItems": [
    {
      "equipmentId": "550e8400-e29b-41d4-a716-446655440101",
      "quantity": 2
    }
  ],
  "eventName": null
}
```

**パラメータ説明**
- `studioId`: スタジオID（UUID）
- `period`: 時間枠 (P1, P2, P3, P4, P5, P6)
- `usageDate`: 利用日付 (yyyy-MM-dd)
- `reservationType`: 予約タイプ
  - `StudentRental`: 学生レンタル
  - `ClassRental`: 授業レンタル
  - `EventReservation`: イベント予約
- `members`: 利用学生の学生番号リスト
- `equipmentItems`: 備品リスト。備品の選択は任意で、選択しない場合は空配列 `[]` を指定する。
  - `equipmentId`: 備品ID（UUID。備品一覧・詳細の `id`）
  - `quantity`: 数量
- `eventName`: イベント名 (イベント予約の場合のみ必須)

**レスポンス**
```json
{
  "bookingId": "booking-uuid",
  "studioId": "550e8400-e29b-41d4-a716-446655440001",
  "period": "P1",
  "usageDate": "2025-01-27",
  "reservationType": "StudentRental",
  "status": "Confirmed",
  "members": ["AB12345678"],
  "equipmentItems": [
    {
      "equipmentId": "550e8400-e29b-41d4-a716-446655440101",
      "quantity": 2
    }
  ],
  "eventName": null,
  "createdAt": "2025-01-27T10:30:00Z",
  "updatedAt": "2025-01-27T10:30:00Z"
}
```

**バリデーションルール**
- 日付は日本時間（`Asia/Tokyo`）で今日から7日後まで（両端を含む）。過去の日付と8日後以降は不可
- 同じスタジオ・時間枠の重複予約は不可。Check-out済みの確定予約も、元の予約時間枠全体を重複判定の対象とする
- 同じ学生が同じ時間枠で複数スタジオに予約不可
- イベント予約の場合はイベント名が必須
- 備品の選択は任意。`equipmentItems: []` の予約を、備品未選択を理由に拒否しない。
- 備品を選択した場合は在庫を確認し、引当成功時にのみ予約を確定する。在庫不足時は予約作成を失敗とする。

#### GET /api/bookings/:id
指定したIDの予約詳細を取得する。

**リクエスト**
- **URL**: `/api/bookings/{id}`
- **メソッド**: GET
- **パラメータ**:
  - `id`: 予約ID

**レスポンス**
```json
{
  "bookingId": "booking-uuid",
  "studioId": "550e8400-e29b-41d4-a716-446655440001",
  "period": "P1",
  "usageDate": "2025-01-27",
  "reservationType": "StudentRental",
  "status": "Confirmed",
  "members": ["AB12345678"],
  "equipmentItems": [
    {
      "equipmentId": "550e8400-e29b-41d4-a716-446655440101",
      "quantity": 2
    }
  ],
  "eventName": null,
  "createdAt": "2025-01-27T10:30:00Z",
  "updatedAt": "2025-01-27T10:30:00Z"
}
```

#### DELETE /api/bookings/:id
利用者が指定したIDの予約をキャンセルする。理由は `user_request` とし、前日23:59（Asia/Tokyo）までの取消規則を適用する。TAによる当日の無断キャンセルは3.7の受付APIで扱う。

**リクエスト**
- **URL**: `/api/bookings/{id}`
- **メソッド**: DELETE
- **パラメータ**:
  - `id`: 予約ID

**レスポンス**
```json
{
  "bookingId": "booking-uuid",
  "studioId": "550e8400-e29b-41d4-a716-446655440001",
  "period": "P1",
  "usageDate": "2025-01-27",
  "reservationType": "StudentRental",
  "status": "Canceled",
  "members": ["AB12345678"],
  "equipmentItems": [
    {
      "equipmentId": "550e8400-e29b-41d4-a716-446655440101",
      "quantity": 2
    }
  ],
  "eventName": null,
  "createdAt": "2025-01-27T10:30:00Z",
  "updatedAt": "2025-01-27T10:30:00Z"
}
```

### 3.4 備品管理

#### GET /api/equipment
利用可能な備品一覧を取得する。

**リクエスト**
- **URL**: `/api/equipment`
- **メソッド**: GET

**レスポンス**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440101",
    "name": "マイク",
    "stock": 10,
    "isActive": true
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440102",
    "name": "マイクケーブル",
    "stock": 15,
    "isActive": true
  }
]
```

#### GET /api/equipment/:id
指定したIDの備品詳細を取得する。

**リクエスト**
- **URL**: `/api/equipment/{id}`
- **メソッド**: GET
- **パラメータ**:
  - `id`: 備品ID（UUID）

**レスポンス**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440101",
  "name": "マイク",
  "stock": 10,
  "isActive": true
}
```

### 3.5 学生管理

#### GET /api/students
利用可能な学生一覧を取得する。

**リクエスト**
- **URL**: `/api/students`
- **メソッド**: GET

**レスポンス**
```json
[
  {
    "studentNumber": "AB12345678",
    "name": "田中太郎",
    "email": "tanaka@example.com"
  },
  {
    "studentNumber": "CD23456789",
    "name": "佐藤花子",
    "email": "sato@example.com"
  }
]
```

#### GET /api/students/search
学生名で学生を検索する。

**リクエスト**
- **URL**: `/api/students/search`
- **メソッド**: GET
- **パラメータ**:
  - `name`: 検索する学生名（部分一致）

**レスポンス**
```json
[
  {
    "studentNumber": "AB12345678",
    "name": "田中太郎",
    "email": "tanaka@example.com"
  }
]
```

#### GET /api/students/:studentNumber
指定した学生番号の学生詳細を取得する。

**リクエスト**
- **URL**: `/api/students/{studentNumber}`
- **メソッド**: GET
- **パラメータ**:
  - `studentNumber`: 学生番号（英数字10文字）

**レスポンス**
```json
{
  "studentNumber": "AB12345678",
  "name": "田中太郎",
  "email": "tanaka@example.com"
}
```

### 3.6 期間時刻管理

#### GET /api/period-times
全期間の開始・終了時刻を取得する。

**リクエスト**
- **URL**: `/api/period-times`
- **メソッド**: GET

**レスポンス**
```json
{
  "periods": [
    {
      "periodId": "P1",
      "startTime": "09:00",
      "endTime": "10:30"
    },
    {
      "periodId": "P2",
      "startTime": "10:30",
      "endTime": "12:00"
    },
    {
      "periodId": "P3",
      "startTime": "13:00",
      "endTime": "14:30"
    },
    {
      "periodId": "P4",
      "startTime": "14:30",
      "endTime": "16:00"
    },
    {
      "periodId": "P5",
      "startTime": "16:00",
      "endTime": "17:30"
    },
    {
      "periodId": "P6",
      "startTime": "17:30",
      "endTime": "19:00"
    }
  ]
}
```

#### GET /api/period-times/:periodId
指定した期間IDの開始・終了時刻を取得する。

**リクエスト**
- **URL**: `/api/period-times/{periodId}`
- **メソッド**: GET
- **パラメータ**:
  - `periodId`: 期間ID (P1, P2, P3, P4, P5, P6)

**レスポンス**
```json
{
  "periodId": "P1",
  "startTime": "09:00",
  "endTime": "10:30"
}
```

<a id="reception-api"></a>

### 3.7 受付業務（最初の開発範囲・未実装）

本人確認、スタジオのCheck-in/Check-out、TAによる無断キャンセル、備品の貸出・返却を含む。以下は実装予定の契約であり、現在の `conf/routes` には登録されていない。TAによる代行予約は従来どおり任意の別機能とする。

#### 共通条件

- すべてTA向け。サーバーは認証された利用者のTA権限を確認する。本文の `performedBy` を送るだけでは操作権限を得られない。
- `{bookingId}` は予約のUUID。更新リクエストはJSONで、`performedBy`（TAIdのUUID）と `operationId`（操作ごとのUUID）を必須とする。`performedBy` は認証されたTAと一致しなければならない。
- 操作日時はサーバーで記録し、ISO 8601形式の日時として返す。業務日付・時刻の判定はAsia/Tokyoを使う。
- 同じ予約・`operationId` の成功済み操作を同じ内容で再送した場合は、保存済みの応答を返し、イベント・在庫更新を重ねない。同じIDで操作種別・内容・実行者が異なる場合は409。並行送信でも数量の上限と状態条件を守る。
- 成功時は200と操作結果を返す。操作、対象予約、実行者、日時、変更内容を監査記録に残す。更新に失敗した場合、受付記録・引当・貸出数量の一部だけを変更しない。
- 入力形式不正は400、予約・備品が存在しない場合は404、以下の状態・数量条件を満たさない場合は409。エラー本文は2.2の共通形式を使う。

#### エンドポイントと入力

以下のパスの先頭はすべて `/api/reception/bookings/{bookingId}`。

| メソッド | パス末尾 | 用途 | 共通項目以外の入力 |
|---|---|---|---|
| GET | なし | 予約の受付状況を取得 | なし。本文不要 |
| PUT | `/identity-verification` | 学生の本人確認結果を保存 | `verifiedMemberNumbers: string[]`。学生番号の重複なし |
| POST | `/check-in` | スタジオ利用開始 | なし |
| POST | `/check-out` | スタジオ利用終了 | なし |
| POST | `/cancel` | TAによる無断キャンセル | `reason: "unauthorized"` |
| POST | `/equipment-checkouts` | 備品の貸出 | `items: [{ equipmentId: UUID, quantity: 正の整数 }]` |
| POST | `/equipment-returns` | 備品の返却 | `items: [{ equipmentId: UUID, quantity: 正の整数 }]` |

貸出・返却の `items` は1件以上とし、同じ備品IDの重複を認めない。数量は今回受け渡す個数であり、累計値ではない。備品なしの予約ではこれらの操作を行わない。

#### 操作条件と結果

| 操作 | 実行条件 | 保存・更新する内容 |
|---|---|---|
| 本人確認 | 利用日当日の確定済み学生予約で未Check-in。確認対象はその予約のメンバーのみ | 本文を確認済みメンバーの全体として保存。全員の来場・学生証提示をTAが確認した場合に `allMembersVerified=true`。学生証画像は保存しない |
| Check-in | 利用時間内の確定予約で未Check-in・未Check-out。学生予約は全員の本人確認済み | `checkedInAt` と `BookingCheckedIn` を記録。枠は `InUse`、予約状態は `Confirmed` のまま |
| Check-out | 確定予約でCheck-in済み・未Check-out | `checkedOutAt` と `BookingCheckedOut` を記録。枠は `CheckedOut`、元の予約枠は維持。備品返却は別の操作で記録する |
| 無断キャンセル | 利用日当日、確定予約の開始から10分以上経過し、未Check-in | `Canceled` に変更し、理由・TA・日時を記録。未貸出の引当を解放する。貸出中の備品がある場合は返却後に実行する |
| 貸出 | 利用日当日の確定予約で未Check-out。指定備品が予約に含まれ、今回数量が未貸出の引当数量以内 | 予約で確保した数量を貸出として記録し、`EquipmentCheckedOut` を発行。引当と貸出で同じ数量を二重に消費しない |
| 返却 | 指定備品の未返却数量が今回数量以上 | 返却数量と `EquipmentReturned` を記録。Check-out後も未返却分を返却できる。スタジオの予約枠は開放しない |

本人確認は一部メンバーだけでも保存できるが、学生予約のCheck-inは全員確認まで拒否する。`allMembersVerified` はサーバーが予約メンバーと照合して算出し、クライアントから真偽値を指定させない。Check-in後の本人確認結果の変更は対象外。

貸出数量・返却数量は備品ごとに累計し、`0 <= returnedQuantity <= checkedOutQuantity <= reservedQuantity` を守る。返却後に同じ予約へ再貸出する運用は、この最初の契約には含めない。備品の引当期間と予約可能在庫の計算は、Inventoryの実装設計で別途具体化する。

#### リクエスト例（本人確認）

```json
{
  "performedBy": "550e8400-e29b-41d4-a716-446655440201",
  "operationId": "550e8400-e29b-41d4-a716-446655440301",
  "verifiedMemberNumbers": ["AB12345678"]
}
```

#### 更新成功時の共通レスポンス

`action` は `IdentityVerification / CheckIn / CheckOut / Cancel / EquipmentCheckout / EquipmentReturn` のいずれか。更新後の詳細はGETで再取得する。

```json
{
  "bookingId": "550e8400-e29b-41d4-a716-446655440401",
  "operationId": "550e8400-e29b-41d4-a716-446655440301",
  "action": "IdentityVerification",
  "performedBy": "550e8400-e29b-41d4-a716-446655440201",
  "performedAt": "2026-10-10T09:55:00+09:00"
}
```

#### GETのレスポンス例（受付状況）

予約内容は既存の `GET /api/bookings/{id}` で取得する。受付状況では本人確認・利用開始／終了・備品の累計数量を返す。未操作の日時・実行者・取消情報は `null`、確認済みメンバーがいない場合は `[]` とする。学生予約以外では `allMembersVerified` を `null` とする。

```json
{
  "bookingId": "550e8400-e29b-41d4-a716-446655440401",
  "bookingStatus": "Confirmed",
  "verifiedMemberNumbers": ["AB12345678"],
  "allMembersVerified": true,
  "verifiedBy": "550e8400-e29b-41d4-a716-446655440201",
  "verifiedAt": "2026-10-10T09:55:00+09:00",
  "checkedInAt": null,
  "checkedInBy": null,
  "checkedOutAt": null,
  "checkedOutBy": null,
  "cancellation": null,
  "graceExpired": false,
  "graceMinutes": 10,
  "equipmentItems": [
    {
      "equipmentId": "550e8400-e29b-41d4-a716-446655440101",
      "reservedQuantity": 2,
      "checkedOutQuantity": 0,
      "returnedQuantity": 0
    }
  ]
}
```

`cancellation` は取消済みの場合 `{ reason, performedBy, canceledAt }` とし、TAによる無断キャンセルでは `performedBy` を必須とする。利用者都合の取消の場合は `reason=user_request`、TAによる操作でなければ `performedBy=null`。

`graceExpired` は確定予約が未Check-inかつ開始から10分以上経過した場合に真とする。取消済み・Check-in済みは偽とし、自動取消は行わない。カレンダーの同名項目も同じ規則で算出する。

#### 受付画面からの利用順序

1. 既存の予約カレンダーAPIで当日の予約を選ぶ。
2. 予約詳細と受付状況を取得し、本人確認、Check-in/Check-out、貸出・返却、無断キャンセルの各操作を表示する。
3. 更新後に受付状況とカレンダーを再取得する。状態不一致の409でも最新状態を取得して表示する。

#### 受け入れ条件（実装時の確認項目）

- 学生3名のうち2名だけ確認済みの場合、Check-inは409。全員確認後、利用時間内なら成功する。
- 同じ貸出操作を同じ `operationId` で再送しても、貸出数量と監査記録は増えない。返却も同様。
- 未返却数量を超える返却を拒否し、数量と在庫を変更しない。
- 早期Check-out後も予約は `Confirmed`、枠は `CheckedOut`。別の組が同じ枠を予約できない。
- 開始10分経過・未Check-inでも自動取消しない。TAが無断キャンセルを実行した場合に理由と実行者を残す。
- 非TAの受付操作を拒否し、予約・受付・在庫を変更しない。

## 4. エラーハンドリング

### 4.1 バリデーションエラー (400)
```json
{
  "error": "過去の日付は予約できません: 2025-01-20",
  "code": "VALIDATION_ERROR",
  "details": "日付は日本時間で今日から7日後まで（7日後を含む）予約可能です"
}
```

### 4.2 重複予約エラー (400)
```json
{
  "error": "予約が既に存在します: 日付=2025-01-27, スタジオ=Aスタ, 時間枠=P1",
  "code": "DUPLICATE_BOOKING",
  "details": "同じスタジオ・時間枠には予約できません"
}
```

### 4.3 学生重複エラー (400)
```json
{
  "error": "学生が既に同じ時間枠で予約しています: AB12345678",
  "code": "STUDENT_CONFLICT",
  "details": "同じ学生は同じ時間枠で複数スタジオに予約できません"
}
```

### 4.4 リソース未発見エラー (404)
```json
{
  "error": "予約が見つかりません",
  "code": "NOT_FOUND",
  "details": "指定されたIDの予約は存在しません"
}
```

## 5. データモデル

### 5.1 予約 (Booking)
```json
{
  "bookingId": "string (UUID)",
  "studioId": "string (UUID)",
  "period": "string (P1-P6)",
  "usageDate": "string (yyyy-MM-dd)",
  "reservationType": "string (StudentRental|ClassRental|EventReservation)",
  "status": "string (Confirmed|Canceled)",
  "members": ["string (英数字10文字)"],
  "equipmentItems": [
    {
      "equipmentId": "string (UUID)",
      "quantity": "number"
    }
  ],
  "eventName": "string|null",
  "createdAt": "string (ISO 8601)",
  "updatedAt": "string (ISO 8601)"
}
```

### 5.2 学生 (Student)
```json
{
  "studentNumber": "string (英数字10文字)",
  "name": "string",
  "email": "string|null"
}
```

### 5.3 備品 (Equipment)
```json
{
  "id": "string (UUID)",
  "name": "string",
  "stock": "number",
  "isActive": "boolean"
}
```

### 5.4 期間時刻 (PeriodTime)
```json
{
  "periodId": "string (P1-P6)",
  "startTime": "string (HH:mm)",
  "endTime": "string (HH:mm)"
}
```

## 6. 使用例

### 6.1 予約作成の流れ
1. 備品一覧取得: `GET /api/equipment`
2. 学生検索: `GET /api/students/search?name=田中`
3. 期間時刻取得: `GET /api/period-times`
4. 予約作成: `POST /api/bookings`

### 6.2 カレンダー表示の流れ
1. 期間時刻取得: `GET /api/period-times`
2. カレンダー取得: `GET /api/booking-calendar?date=2025-01-27`

## 7. 制限事項

### 7.1 予約制限
- 予約可能期間: 日本時間（`Asia/Tokyo`）で今日から7日後まで（両端を含む）。過去の日付と8日後以降は不可
- 同一スタジオ・利用日・限は1組のみ。早期Check-outでも予約枠を短縮・開放せず、同じ枠の予約作成は重複予約エラー（400）とする。半日・終日は含まれる各限に適用する
- 同一学生の同一時間枠での複数スタジオ予約不可

### 7.2 データ制限
- 学生番号: 英数字10文字
- スタジオID・備品ID: UUID。表示名とは別に保持する
- 期間ID: P1-P6
- イベント名: 100文字以内

## 8. 更新履歴

| バージョン | 日付 | 変更内容 | 担当者 |
|------------|------|----------|--------|
| v1.0 | 2025-01-27 | 初版作成 | AI Assistant |
| v1.1 | 2025-01-27 | 期間時刻管理API追加 | AI Assistant |
| v1.2 | 2025-01-27 | 学生管理API追加 | AI Assistant |

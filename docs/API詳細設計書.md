# スタジオ予約システム API詳細設計書

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
- **認証方式**: なし（開発版）
- **レスポンス形式**: JSON
- **文字エンコーディング**: UTF-8

## 2. 共通仕様

### 2.1 HTTPステータスコード
| コード | 説明 |
|--------|------|
| 200 | 成功 |
| 201 | 作成成功 |
| 400 | リクエストエラー |
| 404 | リソース未発見 |
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
  - `studioId` (オプション): スタジオID (A, B, C)

**レスポンス**
```json
{
  "usageDate": "2025-01-27",
  "periodOrder": ["P1", "P2", "P3", "P4", "P5", "P6"],
  "rows": [
    {
      "studioId": "A",
      "studioName": "Aスタ",
      "slots": [
        {
          "periodId": "P1",
          "status": "空",
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
- `空`: 予約可能
- `予約済み`: 予約されている
- `使用中`: 現在使用中
- `予約キャンセル`: キャンセル済み

### 3.3 予約管理

#### POST /api/bookings
新しい予約を作成する。

**リクエスト**
- **URL**: `/api/bookings`
- **メソッド**: POST
- **Content-Type**: `application/json`

**リクエストボディ**
```json
{
  "studioId": "A",
  "period": "P1",
  "usageDate": "2025-01-27",
  "reservationType": "StudentRental",
  "members": ["AB12345678"],
  "equipmentItems": [
    {
      "equipmentId": "equipment-1",
      "quantity": 2
    }
  ],
  "eventName": null
}
```

**パラメータ説明**
- `studioId`: スタジオID (A, B, C)
- `period`: 時間枠 (P1, P2, P3, P4, P5, P6)
- `usageDate`: 利用日付 (yyyy-MM-dd)
- `reservationType`: 予約タイプ
  - `StudentRental`: 学生レンタル
  - `ClassRental`: 授業レンタル
  - `EventReservation`: イベント予約
- `members`: 利用学生の学生番号リスト
- `equipmentItems`: 備品リスト
  - `equipmentId`: 備品ID
  - `quantity`: 数量
- `eventName`: イベント名 (イベント予約の場合のみ必須)

**レスポンス**
```json
{
  "bookingId": "booking-uuid",
  "studioId": "A",
  "period": "P1",
  "usageDate": "2025-01-27",
  "reservationType": "StudentRental",
  "status": "Reserved",
  "members": ["AB12345678"],
  "equipmentItems": [
    {
      "equipmentId": "equipment-1",
      "quantity": 2
    }
  ],
  "eventName": null,
  "createdAt": "2025-01-27T10:30:00Z",
  "updatedAt": "2025-01-27T10:30:00Z"
}
```

**バリデーションルール**
- 日付は今日から1週間後まで
- 同じスタジオ・時間枠の重複予約は不可
- 同じ学生が同じ時間枠で複数スタジオに予約不可
- イベント予約の場合はイベント名が必須
- 備品は最低1つ選択必須

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
  "studioId": "A",
  "period": "P1",
  "usageDate": "2025-01-27",
  "reservationType": "StudentRental",
  "status": "Reserved",
  "members": ["AB12345678"],
  "equipmentItems": [
    {
      "equipmentId": "equipment-1",
      "quantity": 2
    }
  ],
  "eventName": null,
  "createdAt": "2025-01-27T10:30:00Z",
  "updatedAt": "2025-01-27T10:30:00Z"
}
```

#### DELETE /api/bookings/:id
指定したIDの予約をキャンセルする。

**リクエスト**
- **URL**: `/api/bookings/{id}`
- **メソッド**: DELETE
- **パラメータ**:
  - `id`: 予約ID

**レスポンス**
```json
{
  "bookingId": "booking-uuid",
  "studioId": "A",
  "period": "P1",
  "usageDate": "2025-01-27",
  "reservationType": "StudentRental",
  "status": "Cancelled",
  "members": ["AB12345678"],
  "equipmentItems": [
    {
      "equipmentId": "equipment-1",
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
    "id": "equipment-1",
    "name": "マイク",
    "stock": 10,
    "isActive": true
  },
  {
    "id": "equipment-2",
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
  - `id`: 備品ID

**レスポンス**
```json
{
  "id": "equipment-1",
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

## 4. エラーハンドリング

### 4.1 バリデーションエラー (400)
```json
{
  "error": "過去の日付は予約できません: 2025-01-20",
  "code": "VALIDATION_ERROR",
  "details": "日付は今日から1週間後まで予約可能です"
}
```

### 4.2 重複予約エラー (400)
```json
{
  "error": "予約が既に存在します: 日付=2025-01-27, スタジオ=A, 時間枠=P1",
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
  "studioId": "string (A-Z)",
  "period": "string (P1-P6)",
  "usageDate": "string (yyyy-MM-dd)",
  "reservationType": "string (StudentRental|ClassRental|EventReservation)",
  "status": "string (Reserved|Completed|Cancelled)",
  "members": ["string (英数字10文字)"],
  "equipmentItems": [
    {
      "equipmentId": "string",
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
  "id": "string",
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
- 予約可能期間: 今日から1週間後まで
- 同一スタジオ・時間枠の重複予約不可
- 同一学生の同一時間枠での複数スタジオ予約不可

### 7.2 データ制限
- 学生番号: 英数字10文字
- スタジオID: A-Zの1文字
- 期間ID: P1-P6
- イベント名: 100文字以内

## 8. 更新履歴

| バージョン | 日付 | 変更内容 | 担当者 |
|------------|------|----------|--------|
| v1.0 | 2025-01-27 | 初版作成 | AI Assistant |
| v1.1 | 2025-01-27 | 期間時刻管理API追加 | AI Assistant |
| v1.2 | 2025-01-27 | 学生管理API追加 | AI Assistant |

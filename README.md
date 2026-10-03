# 音楽スタジオ予約システム

音楽学校のスタジオ予約と備品レンタルを扱うWebアプリケーションです。要件・設計資料は [`docs/`](docs/) にあります。

## 開発を始める

Windowsでの準備と起動方法は [`docs/環境構築手順.md`](docs/環境構築手順.md) を参照してください。フロントエンドとバックエンドを別々のターミナルで起動します。

```powershell
# ターミナル1
Set-Location .\studio-backend
sbt run

# ターミナル2（リポジトリのルートから実行）
Set-Location .\studio-frontend
npm ci
npm run dev
```

- フロントエンド: <http://localhost:5173>
- バックエンド疎通確認: <http://localhost:9000/api/ping>
- フロントエンドの `/api` 通信は Vite からバックエンドの `localhost:9000` へ転送されます。

既存の確認用コマンドは、フロントエンドが `npm run build`、バックエンドが `sbt test` です。

## 現在の構成

- フロントエンド: Vue 3、TypeScript、Vite、Vue Router、Pinia (`studio-frontend`)
- バックエンド: Scala 3.4.3、Play 3.0.9、sbt 1.11.6 (`studio-backend`)
- DB用SQL: `DB/script/CREATE_TABLE/`。バックエンドの現在のリポジトリ実装はメモリ上にあり、このSQL群はアプリケーションの起動時には使われません。アプリを再起動すると予約などのデータは保持されません。

## 開発時に知っておくこと

- バックエンドのビルドは sbt (`studio-backend/build.sbt`) に統一しています。sbtの版は `studio-backend/project/build.properties` に従います。
- 設計資料間の主要な相違は確認・反映済みです。[要件定義](docs/要件定義.md)で合意した仕様と現在の実装との差を確認してください。[未確定事項](docs/要件定義.md#pending-decisions)には実装前に具体化する設計事項を残しています。
- フロントエンドには現在、ビルド用の `build` スクリプトがあります。型検査・静的解析や自動実行の設定は、今後整える項目です。

## 資料

- [環境構築と起動](docs/環境構築手順.md)
- [要件定義](docs/要件定義.md)
- [API詳細設計](docs/API詳細設計書.md)
- [予約状況確認画面の基本設計](docs/予約状況確認画面_基本設計書.md)
- [ログ仕様](docs/ログ仕様書.md)
- [ログID管理表](docs/ログID管理表.md)
- [ユビキタス言語帳](docs/ユビキタス言語帳.md)
- [AIエージェント向け案内](AGENTS.md)

# リポジトリの作業指針

音楽学校のスタジオ予約と備品レンタルを扱う、Scala・Web開発の学習用アプリケーションです。

## 作業対象と設定

- `studio-frontend/`: Vue・TypeScript・Vite。依存関係と実行コマンドは `package.json`、インストールする版は `package-lock.json` を参照します。
- `studio-backend/`: Scala・Play。ビルドには sbt を使います。版は `build.sbt`、`project/plugins.sbt`、`project/build.properties` を参照します。`build.sc` は Scala の版が異なる Mill 用定義なので、sbt の設定と混同しないでください。
- `DB/script/CREATE_TABLE/`: 手動実行するSQL。現在の保存先は `studio-backend/app/Module.scala` でメモリ上の実装に結び付けられており、SQLを実行してもDB保存には切り替わりません。

## 実行コマンド

各コマンドは表のディレクトリで実行してください。Windowsでは PowerShell を使います。

| 作業ディレクトリ | 用途 | コマンド |
|---|---|---|
| `studio-frontend` | ロックファイルに沿った依存導入 | `npm ci` |
| `studio-frontend` | 開発サーバー起動 | `npm run dev` |
| `studio-frontend` | ビルド | `npm run build` |
| `studio-backend` | 開発サーバー起動 | `sbt run` |
| `studio-backend` | テスト | `sbt test` |

フロントエンドは `localhost:5173`、バックエンドは `localhost:9000` を使い、Vite が `/api` を転送します。現在、フロントエンドのテスト・型検査・lint用スクリプトはありません。`npm run build` の成功を型検査の成功として扱わないでください。

## 作業に応じて読む資料

- 全体の概要を調べる場合: [README.md](README.md)
- 環境を準備する場合: [環境構築手順](docs/環境構築手順.md)
- 業務用語を導入・変更する場合: [ユビキタス言語帳](docs/ユビキタス言語帳.md)
- 業務ルールを扱う場合: [要件定義](docs/要件定義.md)。[未確定事項](docs/要件定義.md#pending-decisions)にある資料間の相違を、確定仕様として扱わないでください。
- APIを変更する場合: [API詳細設計書](docs/API詳細設計書.md) と `studio-backend/conf/routes`、対象の実装を照合してください。

資料と実装の相違は区別して扱ってください。調査で解消できず、業務仕様の選択が必要な場合に、該当箇所と影響を示して確認してください。

## 設計と説明

- 既存の業務用語と責務の境界を尊重し、依頼に必要な範囲を変更してください。無関係なコード整理や依存更新は含めないでください。
- DDD は学習と保守に役立つ範囲で使い、不要な層や抽象化を増やさないでください。DDD の説明では増田亨氏の書籍・登壇資料を優先してください。
- 学習支援を求められた場合は、エラーの原因、修正の理由、Scala 固有の構文を説明してください。コード例はファイルごとに分け、コメントは処理の意図や読み取りにくい点を補足してください。

## 完了時の確認

変更に応じて表のビルド・テストを選んで実行してください。文書だけの変更では参照先と記述の整合を確認します。

完了報告には変更内容と検証結果を記載し、未実施の確認があれば理由を添えてください。

name := """studio-backend"""
organization := "com.amembow"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

// Upgraded to Scala 3; verify Play and library compatibility manually.
scalaVersion := "3.4.3"
// 依存ライブラリ
libraryDependencies ++= Seq(
  // Play 標準
  guice, // 依存性注入（DI）
  jdbc, // JDBC サポート
  ehcache, // キャッシュ（必要なら）
  specs2 % Test, // デフォのテスト

  // DB
  "org.postgresql" % "postgresql" % "42.7.3",

  // マイグレーション
  "org.flywaydb" % "flyway-core" % "10.17.3",

  // JSON
  "com.typesafe.play" %% "play-json" % "2.10.5",

  // Functional programming
  "org.typelevel" %% "cats-core" % "2.10.0",

  // ログ（JSON出力対応など）
  "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.3",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.15.3",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.15.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.3",

  // テスト（ScalaTest）
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
)
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.amembow.controllers._"

// JVMオプション（文字エンコーディング設定）
javaOptions ++= Seq(
  "-Dfile.encoding=UTF-8",
  "-Dconsole.encoding=UTF-8"
)

// Scalaコンパイラオプション
scalacOptions ++= Seq(
  "-encoding",
  "UTF-8"
)

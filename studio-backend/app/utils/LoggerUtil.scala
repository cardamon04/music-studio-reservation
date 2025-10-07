package utils

import play.api.i18n.MessagesApi
import play.api.Logging
import javax.inject.{Inject, Singleton}
import play.api.mvc.RequestHeader
import scala.util.{Try, Success, Failure}
import org.slf4j.LoggerFactory
import java.lang.StackTraceElement

/** ログ出力用のユーティリティクラス
  *
  * メッセージIDを使ってログを出力する。 メッセージは conf/messages ファイルで管理される。
  *
  * 使用例:
  * {{{
  *   LoggerUtil.info("I001", bookingId)
  *   LoggerUtil.error("E001", errorMessage)
  * }}}
  */
@Singleton
class LoggerUtil @Inject() (messagesApi: MessagesApi) extends Logging {

  // 明示的にロガーを取得
  private val customLogger = LoggerFactory.getLogger("utils.LoggerUtil")

  /** 呼び出し元の情報を取得
    *
    * @return
    *   呼び出し元のクラス名、メソッド名、行番号を含む文字列
    */
  private def getCallerInfo(): String = {
    val stackTrace = Thread.currentThread().getStackTrace
    // LoggerUtilのメソッドをスキップして、実際の呼び出し元を取得
    val caller = stackTrace
      .find { element =>
        val className = element.getClassName
        !className.startsWith("utils.LoggerUtil") &&
        !className.startsWith("java.lang.Thread") &&
        !className.startsWith("scala.")
      }
      .getOrElse(stackTrace(3)) // フォールバック

    val className = caller.getClassName.split("\\.").last
    val methodName = caller.getMethodName
    val lineNumber = caller.getLineNumber

    s"$className.$methodName:$lineNumber"
  }

  /** 情報レベルのログを出力
    *
    * @param messageId
    *   メッセージID (例: "I001")
    * @param args
    *   メッセージのプレースホルダーに渡す引数
    */
  def info(messageId: String, args: Any*): Unit = {
    val message = getMessage(messageId, args *)
    val callerInfo = getCallerInfo()
    customLogger.info(s"[$messageId] [$callerInfo] $message")
  }

  /** エラーレベルのログを出力
    *
    * @param messageId
    *   メッセージID (例: "E001")
    * @param args
    *   メッセージのプレースホルダーに渡す引数
    */
  def error(messageId: String, args: Any*): Unit = {
    val message = getMessage(messageId, args *)
    val callerInfo = getCallerInfo()
    customLogger.error(s"[$messageId] [$callerInfo] $message")
  }

  /** 警告レベルのログを出力
    *
    * @param messageId
    *   メッセージID (例: "W001")
    * @param args
    *   メッセージのプレースホルダーに渡す引数
    */
  def warn(messageId: String, args: Any*): Unit = {
    val message = getMessage(messageId, args *)
    val callerInfo = getCallerInfo()
    customLogger.warn(s"[$messageId] [$callerInfo] $message")
  }

  /** デバッグレベルのログを出力
    *
    * @param messageId
    *   メッセージID (例: "D001")
    * @param args
    *   メッセージのプレースホルダーに渡す引数
    */
  def debug(messageId: String, args: Any*): Unit = {
    val message = getMessage(messageId, args *)
    val callerInfo = getCallerInfo()
    customLogger.debug(s"[$messageId] [$callerInfo] $message")
  }

  /** リクエスト情報付きでログを出力
    *
    * @param level
    *   ログレベル ("info", "error", "warn", "debug")
    * @param messageId
    *   メッセージID
    * @param request
    *   リクエスト情報
    * @param args
    *   メッセージのプレースホルダーに渡す引数
    */
  def logWithRequest(
      level: String,
      messageId: String,
      request: RequestHeader,
      args: Any*
  ): Unit = {
    val message = getMessage(messageId, args *)
    val callerInfo = getCallerInfo()
    val requestInfo =
      s"[${request.method} ${request.uri}] [${request.remoteAddress}]"

    level.toLowerCase match {
      case "info" =>
        customLogger.info(s"[$messageId] [$callerInfo] $requestInfo $message")
      case "error" =>
        customLogger.error(s"[$messageId] [$callerInfo] $requestInfo $message")
      case "warn" =>
        customLogger.warn(s"[$messageId] [$callerInfo] $requestInfo $message")
      case "debug" =>
        customLogger.debug(s"[$messageId] [$callerInfo] $requestInfo $message")
      case _ =>
        customLogger.info(s"[$messageId] [$callerInfo] $requestInfo $message")
    }
  }

  /** 例外情報付きでエラーログを出力
    *
    * @param messageId
    *   メッセージID
    * @param throwable
    *   例外オブジェクト
    * @param args
    *   メッセージのプレースホルダーに渡す引数
    */
  def errorWithException(
      messageId: String,
      throwable: Throwable,
      args: Any*
  ): Unit = {
    val message = getMessage(messageId, args *)
    val callerInfo = getCallerInfo()
    customLogger.error(s"[$messageId] [$callerInfo] $message", throwable)
  }

  /** メッセージIDからメッセージを取得
    *
    * @param messageId
    *   メッセージID
    * @param args
    *   プレースホルダーに渡す引数
    * @return
    *   フォーマットされたメッセージ
    */
  private def getMessage(messageId: String, args: Any*): String = {
    Try {
      // MessagesApiを使ってメッセージを取得（デフォルトロケールを使用）
      val messages = messagesApi.preferred(Seq.empty)
      if (args.nonEmpty) {
        messages(messageId, args *)
      } else {
        messages(messageId)
      }
    } match {
      case Success(message) => message
      case Failure(_)       =>
        // メッセージが見つからない場合はIDをそのまま返す
        customLogger.warn(s"メッセージIDが見つかりません: $messageId")
        s"Message not found: $messageId"
    }
  }

  /** ビジネスロジックの実行ログ
    *
    * @param messageId
    *   メッセージID
    * @param operation
    *   操作名
    * @param entityId
    *   エンティティID
    * @param details
    *   詳細情報
    */
  def businessLog(
      messageId: String,
      operation: String,
      entityId: String,
      details: String = ""
  ): Unit = {
    val detailInfo = if (details.nonEmpty) s" - $details" else ""
    val message = s"$operation: $entityId$detailInfo"
    val formattedMessage = getMessage(messageId, message)
    val callerInfo = getCallerInfo()
    customLogger.info(s"[$messageId] [$callerInfo] $formattedMessage")
  }

  /** パフォーマンス測定用のログ出力
    *
    * @param messageId
    *   メッセージID
    * @param operation
    *   操作名
    * @param duration
    *   実行時間（ミリ秒）
    * @param additionalInfo
    *   追加情報
    */
  def performance(
      messageId: String,
      operation: String,
      duration: Long,
      additionalInfo: String = ""
  ): Unit = {
    val info = if (additionalInfo.nonEmpty) s" - $additionalInfo" else ""
    val message = s"$operation: ${duration}ms$info"
    val formattedMessage = getMessage(messageId, message)
    val callerInfo = getCallerInfo()
    customLogger.info(s"[$messageId] [$callerInfo] $formattedMessage")
  }
}

package app

import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import play.api.routing.Router
import controllers.{HomeController, booking}
import utils.LoggerUtil
import javax.inject.Inject
import play.api.i18n.I18nComponents
import org.slf4j.LoggerFactory

/** アプリケーション起動・停止時の処理を行うクラス
  *
  * Play Frameworkのライフサイクルイベントを処理する
  */
class ApplicationLifecycle @Inject() (loggerUtil: LoggerUtil) {

  private val logger = LoggerFactory.getLogger("app.ApplicationLifecycle")

  /** アプリケーション起動時の処理 */
  def onStart(): Unit = {
    logger.info("=== アプリケーション起動開始 ===")
    loggerUtil.info("I009")
    loggerUtil.businessLog(
      "アプリケーション起動",
      "system",
      "Music Studio Reservation System"
    )
    logger.info("=== アプリケーション起動完了 ===")
  }

  /** アプリケーション停止時の処理 */
  def onStop(): Unit = {
    logger.info("=== アプリケーション停止開始 ===")
    loggerUtil.info("I010")
    loggerUtil.businessLog(
      "アプリケーション停止",
      "system",
      "Music Studio Reservation System"
    )
    logger.info("=== アプリケーション停止完了 ===")
  }
}

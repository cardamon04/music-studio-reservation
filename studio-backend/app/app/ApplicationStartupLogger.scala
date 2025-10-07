package app

import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import play.api.routing.Router
import controllers.{HomeController, booking}
import utils.LoggerUtil
import javax.inject.Inject
import play.api.i18n.I18nComponents

/** アプリケーション起動時のログ出力を行うクラス
  *
  * アプリケーションの起動・停止時にログを出力する
  */
class ApplicationStartupLogger @Inject() (loggerUtil: LoggerUtil) {

  /** アプリケーション起動時のログ出力 */
  def onStart(): Unit = {
    loggerUtil.info("I009")
    loggerUtil.businessLog(
      "アプリケーション起動",
      "system",
      "Music Studio Reservation System"
    )
  }

  /** アプリケーション停止時のログ出力 */
  def onStop(): Unit = {
    loggerUtil.info("I010")
    loggerUtil.businessLog(
      "アプリケーション停止",
      "system",
      "Music Studio Reservation System"
    )
  }
}

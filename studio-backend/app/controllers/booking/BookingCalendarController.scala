package controllers.booking

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import application.booking.BookingCalendarQueryService
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDate
import scala.util.Try
import play.api.libs.json._
import utils.LoggerUtil
import play.api.i18n.MessagesApi

@Singleton
class BookingCalendarController @Inject() (
    controllerComponents: ControllerComponents,
    queryService: BookingCalendarQueryService,
    loggerUtil: LoggerUtil,
    messagesApi: MessagesApi
)(using executionContext: ExecutionContext)
    extends AbstractController(controllerComponents) {

  private val messages = messagesApi.preferred(Seq.empty)

  /** 日付とスタジオIDを指定して予約カレンダーを取得する。
    *
    * @param date
    *   指定された日付
    * @param studioId
    *   指定されたスタジオID
    */
  def getByDate(rawDate: String, studioId: Option[String]): Action[AnyContent] =
    Action.async { request =>
      given ec: ExecutionContext = executionContext

      // リクエスト受信ログ
      loggerUtil.logWithRequest("info", "I017", request, rawDate, studioId.getOrElse("全スタジオ"))

      Try(LocalDate.parse(rawDate)).toOption match {
        case Some(date) =>
          loggerUtil.debug("D011", date.toString, studioId.getOrElse("全スタジオ"))

          queryService.getByDate(date, studioId).map { calendar =>
            loggerUtil.info("I018", date.toString, calendar.rows.length.toString)
            loggerUtil.businessLog("I019", date.toString, studioId.getOrElse("全スタジオ"))

            Ok(
              Json
                .toJson(calendar)(JsonFormats.bookingCalendarViewWrites)
            ).as("application/json; charset=utf-8")
          }.recover {
            case ex: Exception =>
              loggerUtil.errorWithException("E013", ex, date.toString, studioId.getOrElse("全スタジオ"))
              InternalServerError(Json.obj("error" -> messages("E014")))
          }
        case None =>
          loggerUtil.error("E002", rawDate)
          loggerUtil.logWithRequest("error", "E015", request, rawDate)
          Future.successful(BadRequest(Json.obj("error" -> messages("E015"))))
      }
    }
}

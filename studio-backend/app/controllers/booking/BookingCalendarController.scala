package controllers.booking

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import application.booking.BookingCalendarQueryService
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import utils.LoggerUtil
import play.api.i18n.MessagesApi
import domain.booking.{CalendarDate, StudioId}

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
  def getByDate(rawDate: String, rawStudioId: Option[String]): Action[AnyContent] =
    Action.async { request =>
      given ec: ExecutionContext = executionContext

      // スタジオIDを値オブジェクトに変換
      val studioIdOpt = rawStudioId.flatMap(StudioId.fromString)
      val studioIdDisplay = rawStudioId.getOrElse("全スタジオ")

      // リクエスト受信ログ
      loggerUtil.logWithRequest("info", "I017", request, rawDate, studioIdDisplay)

      CalendarDate.fromString(rawDate) match {
        case Right(calendarDate) =>
          val date = calendarDate.value
          loggerUtil.debug("D011", date.toString, studioIdDisplay)

          queryService.getByDate(date, studioIdOpt).map { calendar =>
            loggerUtil.info("I018", date.toString, calendar.rows.length.toString)
            loggerUtil.businessLog("I019", date.toString, studioIdDisplay)

            Ok(
              Json
                .toJson(calendar)(JsonFormats.bookingCalendarViewWrites)
            ).as("application/json; charset=utf-8")
          }.recover {
            case ex: Exception =>
              loggerUtil.errorWithException("E013", ex, date.toString, studioIdDisplay)
              InternalServerError(Json.obj("error" -> messages("E014")))
          }
        case Left(errorMessage) =>
          loggerUtil.error("E002", rawDate)
          loggerUtil.logWithRequest("error", "E015", request, rawDate)
          Future.successful(BadRequest(Json.obj("error" -> messages("E015"), "details" -> errorMessage)))
      }
    }
}

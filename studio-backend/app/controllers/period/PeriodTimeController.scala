package controllers.period

import domain.period.{PeriodId, PeriodTimeRepository}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import scala.concurrent.ExecutionContext

@Singleton
class PeriodTimeController @Inject() (
    cc: ControllerComponents,
    periodTimeRepository: PeriodTimeRepository
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with I18nSupport {

  private val logger = Logger(this.getClass)

  // PeriodTimeをJSONに変換するためのimplicit Writes
  implicit val periodTimeWrites: Writes[domain.period.PeriodTime] = {
    Json.writes[domain.period.PeriodTime]
  }

  // PeriodIdをJSONに変換するためのimplicit Writes
  implicit val periodIdWrites: Writes[domain.period.PeriodId] = {
    Json.writes[domain.period.PeriodId]
  }

  /** 全ての期間時刻を取得
    */
  def getAllPeriodTimes = Action.async { implicit request =>
    implicit val messages: Messages = request.messages
    logger.info(s"[I022] ${messages("I022", "getAllPeriodTimes")}")
    periodTimeRepository.findAll().map { periodTimes =>
      logger.info(s"[I023] ${messages("I023", periodTimes.length)}")
      val response = Json.obj(
        "periods" -> periodTimes.map { pt =>
          Json.obj(
            "periodId" -> pt.periodId.value,
            "startTime" -> pt.startTime.toString,
            "endTime" -> pt.endTime.toString
          )
        }
      )
      logger.info(s"[I024] ${messages("I024", "getAllPeriodTimes")}")
      Ok(response)
    }
  }

  /** 期間IDで期間時刻を取得
    */
  def getPeriodTimeByPeriodId(periodId: String) = Action.async {
    implicit request =>
      implicit val messages: Messages = request.messages
      logger.info(
        s"[I022] ${messages("I022", s"getPeriodTimeByPeriodId($periodId)")}"
      )
      periodTimeRepository.findByPeriodId(PeriodId(periodId)).map {
        case Some(periodTime) =>
          logger.info(s"[I023] ${messages("I023", s"$periodId の期間時刻を取得")}")
          val response = Json.obj(
            "periodId" -> periodTime.periodId.value,
            "startTime" -> periodTime.startTime.toString,
            "endTime" -> periodTime.endTime.toString
          )
          logger.info(
            s"[I024] ${messages("I024", s"getPeriodTimeByPeriodId($periodId)")}"
          )
          Ok(response)
        case None =>
          logger.warn(s"期間時刻が見つかりません: $periodId")
          logger.info(
            s"[I024] ${messages("I024", s"getPeriodTimeByPeriodId($periodId)")}"
          )
          NotFound
      }
  }
}

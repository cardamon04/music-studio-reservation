package controllers.booking

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDate
import scala.util.Try
import utils.LoggerUtil
import application.booking.{BookingApplicationService, CreateBookingRequest, EquipmentItemRequest}
import domain.booking.{Booking, BookingId, BookingStatus, StudioId, PeriodId, EventName, ReservationType}
import domain.student.StudentNumber
import domain.equipment.{EquipmentItem, EquipmentId}

/** 予約作成・管理を行うコントローラー
  *
  * DDDのアプリケーション層として、ドメインサービスを呼び出して
  * 予約の作成・更新・削除などの操作を行う
  */
@Singleton
class BookingController @Inject() (
    controllerComponents: ControllerComponents,
    loggerUtil: LoggerUtil,
    bookingService: BookingApplicationService
)(using executionContext: ExecutionContext)
    extends AbstractController(controllerComponents) {

  /** 予約作成リクエストのJSONフォーマット
    *
    * フロントエンドから送信される予約データの構造を定義
    * application.booking.CreateBookingRequestと同じ構造
    */
  case class ControllerCreateBookingRequest(
      studioId: String,
      period: String,
      usageDate: String,
      reservationType: String,
      members: List[String],
      equipmentItems: List[ControllerEquipmentItemRequest],
      eventName: Option[String] // イベント予約の場合のイベント名
  )

  case class ControllerEquipmentItemRequest(
      equipmentId: String,
      quantity: Int
  )

  // JSONの読み書き用のimplicit定義
  implicit val controllerEquipmentItemRequestReads: Reads[ControllerEquipmentItemRequest] = Json.reads[ControllerEquipmentItemRequest]
  implicit val controllerCreateBookingRequestReads: Reads[ControllerCreateBookingRequest] = Json.reads[ControllerCreateBookingRequest]

  /** 予約作成エンドポイント
    *
    * POST /api/bookings
    *
    * @return 作成された予約の情報
    */
  def create(): Action[JsValue] = Action.async(parse.json) { request =>
    given ec: ExecutionContext = executionContext

    // リクエスト受信ログ
    loggerUtil.debug("D001", request.body.toString())
    loggerUtil.logWithRequest("info", "I007", request, "予約作成")

    // JSONをリクエストオブジェクトに変換
    request.body.validate[ControllerCreateBookingRequest] match {
      case JsSuccess(bookingRequest, _) =>
        loggerUtil.debug("D002", bookingRequest.toString())
        loggerUtil.info("I001", s"スタジオ: ${bookingRequest.studioId}, 時間枠: ${bookingRequest.period}, 日付: ${bookingRequest.usageDate}")

        // 予約アプリケーションサービスを呼び出して予約を作
        val serviceRequest = CreateBookingRequest(
          studioId = bookingRequest.studioId,
          period = bookingRequest.period,
          usageDate = bookingRequest.usageDate,
          reservationType = bookingRequest.reservationType,
          members = bookingRequest.members,
          equipmentItems = bookingRequest.equipmentItems.map { item =>
            EquipmentItemRequest(item.equipmentId, item.quantity)
          },
          eventName = bookingRequest.eventName
        )

        bookingService.createBooking(serviceRequest).map { booking =>
              val response = Json.obj(
                "bookingId" -> booking.bookingId.value,
                "studioId" -> booking.studioId.value,
                "period" -> booking.period.value,
                "usageDate" -> booking.usageDate.toString,
                "reservationType" -> booking.reservationType.value,
                "status" -> booking.status.value,
                "members" -> booking.members.map(_.value),
                "equipmentItems" -> booking.equipmentItems.map { item =>
                  Json.obj(
                    "equipmentId" -> item.equipmentId.value,
                    "quantity" -> item.quantity.value
                  )
                },
                "message" -> "予約が作成されました"
              )

              loggerUtil.businessLog("I001", "予約作成", booking.bookingId.value, s"スタジオ: ${booking.studioId.value}")
              Ok(response).as("application/json; charset=utf-8")
            }.recover {
              case ex: IllegalStateException =>
                loggerUtil.error("E001", ex.getMessage)
                Conflict(Json.obj("error" -> ex.getMessage))
              case ex: IllegalArgumentException =>
                loggerUtil.error("E001", ex.getMessage)
                BadRequest(Json.obj("error" -> ex.getMessage))
              case ex: Exception =>
                loggerUtil.errorWithException("E001", ex, "予約作成エラー")
                InternalServerError(Json.obj("error" -> "Internal server error"))
            }

      case JsError(errors) =>
        // JSONパースエラーの場合
        val errorMessage = errors.map { case (path, errors) =>
          s"${path.toString}: ${errors.map(_.message).mkString(", ")}"
        }.mkString("; ")

        loggerUtil.error("E003", errorMessage)
        loggerUtil.logWithRequest("error", "E003", request, errorMessage)

        Future.successful(BadRequest(Json.obj("error" -> s"Invalid request format: $errorMessage")))
    }
  }

  /** 予約取得エンドポイント
    *
    * GET /api/bookings/:id
    */
  def getById(bookingId: String): Action[AnyContent] = Action.async {
    loggerUtil.debug("D004", s"予約取得: $bookingId")

    bookingService.getBooking(bookingId).map {
      case Some(booking) =>
        val response = Json.obj(
          "bookingId" -> booking.bookingId.value,
          "studioId" -> booking.studioId.value,
          "period" -> booking.period.value,
          "usageDate" -> booking.usageDate.toString,
          "reservationType" -> booking.reservationType.value,
          "status" -> booking.status.value,
          "members" -> booking.members.map(_.value),
          "equipmentItems" -> booking.equipmentItems.map { item =>
            Json.obj(
              "equipmentId" -> item.equipmentId.value,
              "quantity" -> item.quantity.value
            )
          },
          "createdAt" -> booking.createdAt.toString,
          "updatedAt" -> booking.updatedAt.toString
        )

        loggerUtil.info("I003", s"予約取得成功: $bookingId")
        Ok(response)

      case None =>
        loggerUtil.warn("W001", s"予約が見つかりません: $bookingId")
        NotFound(Json.obj("error" -> "Booking not found"))
    }.recover {
      case ex: Exception =>
        loggerUtil.errorWithException("E004", ex, s"予約取得エラー: $bookingId")
        InternalServerError(Json.obj("error" -> "Internal server error"))
    }
  }

  /** 予約キャンセルエンドポイント
    *
    * DELETE /api/bookings/:id
    */
  def cancel(bookingId: String): Action[AnyContent] = Action.async {
    loggerUtil.debug("D005", s"予約キャンセル: $bookingId")

    bookingService.cancelBooking(bookingId).map { booking =>
      val response = Json.obj(
        "bookingId" -> booking.bookingId.value,
        "status" -> booking.status.value,
        "message" -> "予約がキャンセルされました"
      )

      loggerUtil.businessLog("I004", "予約キャンセル", booking.bookingId.value, "")
      Ok(response)
    }.recover {
      case ex: IllegalStateException =>
        loggerUtil.warn("W002", s"予約キャンセル失敗: ${ex.getMessage}")
        Conflict(Json.obj("error" -> ex.getMessage))
      case ex: IllegalArgumentException =>
        loggerUtil.warn("W003", s"予約キャンセル失敗: ${ex.getMessage}")
        NotFound(Json.obj("error" -> ex.getMessage))
      case ex: Exception =>
        loggerUtil.errorWithException("E005", ex, s"予約キャンセルエラー: $bookingId")
        InternalServerError(Json.obj("error" -> "Internal server error"))
    }
  }
}

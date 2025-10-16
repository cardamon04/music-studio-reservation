package application.booking

import java.time.{LocalDate, LocalDateTime}
import play.api.libs.json._
import domain.booking._

/** スロットビュー（DTO）
  *
  * カレンダー表示用のスロット情報。 プリミティブ型ではなく、ドメインの値オブジェクトを使用。
  */
final case class SlotView(
    periodId: PeriodId,

    /** スタジオの空き状況
      */
    status: SlotStatus,

    /** 予約ID
      */
    bookingId: Option[BookingId],

    /** 予約種類
      */
    reservationType: Option[ReservationType],

    /** イベント名（イベント予約の場合）
      */
    eventName: Option[EventName],

    /** 猶予時間が過ぎたかどうか。true:過ぎた、false:過ぎていない
      */
    graceExpired: Boolean,

    /** 予約開始
      */
    startTime: LocalDateTime,

    /** 予約終了
      */
    endTime: LocalDateTime
)

object SlotView {

  // カスタムJSON形式（値オブジェクトを文字列にシリアライズ）
  implicit val slotViewWrites: OWrites[SlotView] = new OWrites[SlotView] {
    def writes(slot: SlotView): JsObject = Json.obj(
      "periodId" -> slot.periodId.value,
      "status" -> slot.status.displayName,
      "bookingId" -> slot.bookingId.map(_.value),
      "reservationType" -> slot.reservationType.map(_.value),
      "eventName" -> slot.eventName.map(_.value),
      "graceExpired" -> slot.graceExpired,
      "startTime" -> slot.startTime.toString,
      "endTime" -> slot.endTime.toString
    )
  }

  implicit val slotViewReads: Reads[SlotView] = new Reads[SlotView] {
    def reads(json: JsValue): JsResult[SlotView] = {
      for {
        periodIdStr <- (json \ "periodId").validate[String]
        periodId <- PeriodId
          .fromString(periodIdStr)
          .map(JsSuccess(_))
          .getOrElse(JsError(s"Invalid periodId: $periodIdStr"))

        statusStr <- (json \ "status").validate[String]
        status <- SlotStatus
          .fromDisplayName(statusStr)
          .map(JsSuccess(_))
          .getOrElse(JsError(s"Invalid status: $statusStr"))

        bookingIdOpt <- (json \ "bookingId").validateOpt[String]
        bookingId = bookingIdOpt.flatMap(BookingId.fromString)

        resTypeOpt <- (json \ "reservationType").validateOpt[String]
        reservationType = resTypeOpt.flatMap(ReservationType.fromString)

        eventNameOpt <- (json \ "eventName").validateOpt[String]
        eventName = eventNameOpt.flatMap(EventName.fromString)

        graceExpired <- (json \ "graceExpired").validate[Boolean]
        startTimeStr <- (json \ "startTime").validate[String]
        startTime <-
          try {
            JsSuccess(LocalDateTime.parse(startTimeStr))
          } catch {
            case _: Exception => JsError(s"Invalid startTime: $startTimeStr")
          }
        endTimeStr <- (json \ "endTime").validate[String]
        endTime <-
          try {
            JsSuccess(LocalDateTime.parse(endTimeStr))
          } catch {
            case _: Exception => JsError(s"Invalid endTime: $endTimeStr")
          }
      } yield SlotView(
        periodId,
        status,
        bookingId,
        reservationType,
        eventName,
        graceExpired,
        startTime,
        endTime
      )
    }
  }

  implicit val slotViewFormat: OFormat[SlotView] =
    OFormat(slotViewReads, slotViewWrites)
}

/** スタジオ行（DTO）
  *
  * カレンダー表示用のスタジオ単位の情報。
  */
final case class StudioRow(
    studioId: StudioId,
    studioName: String,
    slots: List[SlotView]
)

object StudioRow {

  implicit val studioRowWrites: OWrites[StudioRow] = new OWrites[StudioRow] {
    def writes(row: StudioRow): JsObject = Json.obj(
      "studioId" -> row.studioId.value,
      "studioName" -> row.studioName,
      "slots" -> row.slots
    )
  }

  implicit val studioRowReads: Reads[StudioRow] = new Reads[StudioRow] {
    def reads(json: JsValue): JsResult[StudioRow] = {
      for {
        studioIdStr <- (json \ "studioId").validate[String]
        studioId <- StudioId
          .fromString(studioIdStr)
          .map(JsSuccess(_))
          .getOrElse(JsError(s"Invalid studioId: $studioIdStr"))
        studioName <- (json \ "studioName").validate[String]
        slots <- (json \ "slots").validate[List[SlotView]]
      } yield StudioRow(studioId, studioName, slots)
    }
  }

  implicit val studioRowFormat: OFormat[StudioRow] =
    OFormat(studioRowReads, studioRowWrites)
}

/** 予約カレンダービュー（DTO）
  *
  * 特定日付の予約状況をカレンダー形式で表示するための情報。
  */
final case class BookingCalendarView(
    usageDate: CalendarDate,
    periodOrder: List[PeriodId],
    rows: List[StudioRow]
)

object BookingCalendarView {

  implicit val bookingCalendarViewWrites: OWrites[BookingCalendarView] =
    new OWrites[BookingCalendarView] {
      def writes(view: BookingCalendarView): JsObject = Json.obj(
        "usageDate" -> view.usageDate.value.toString,
        "periodOrder" -> view.periodOrder.map(_.value),
        "rows" -> view.rows
      )
    }

  implicit val bookingCalendarViewReads: Reads[BookingCalendarView] =
    new Reads[BookingCalendarView] {
      def reads(json: JsValue): JsResult[BookingCalendarView] = {
        for {
          usageDateStr <- (json \ "usageDate").validate[String]
          usageDate <- CalendarDate.fromString(usageDateStr) match {
            case Right(date) => JsSuccess(date)
            case Left(error) => JsError(error)
          }
          periodOrderStrs <- (json \ "periodOrder").validate[List[String]]
          periodOrder = periodOrderStrs.flatMap(PeriodId.fromString)
          rows <- (json \ "rows").validate[List[StudioRow]]
        } yield BookingCalendarView(usageDate, periodOrder, rows)
      }
    }

  implicit val bookingCalendarViewFormat: OFormat[BookingCalendarView] =
    OFormat(bookingCalendarViewReads, bookingCalendarViewWrites)
}

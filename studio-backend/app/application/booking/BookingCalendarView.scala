package application.booking

import java.time.{LocalDate, LocalDateTime}
import play.api.libs.json._

final case class SlotView(
    periodId: String,
    /** スタジオの空き状況。 "空" / "予約済み" / "予約確定" / "使用中" / "予約キャンセル"。
      */
    status: String,

    /** 予約ID。
      */
    bookingId: Option[String],

    /** 予約種類。
      */
    reservationType: Option[String],

    /** イベント名（イベント予約の場合）。
      */
    eventName: Option[String],

    /** 猶予時間が過ぎたかどうか。true:過ぎた、false:過ぎていない。
      */
    graceExpired: Boolean,

    /** 予約開始
      */
    startTime: LocalDateTime,

    /** 予約終了
      */
    endTime: LocalDateTime
)

/** スタジオ行。
  */
object SlotView {
  implicit val slotViewFormat: OFormat[SlotView] = Json.format[SlotView]
}

final case class StudioRow(
    studioId: String,
    studioName: String,
    slots: List[SlotView]
)

/** 予約日のカレンダービュー。
  */
object StudioRow {
  implicit val studioRowFormat: OFormat[StudioRow] = Json.format[StudioRow]
}

final case class BookingCalendarView(
    usageDate: LocalDate,
    periodOrder: List[String],
    rows: List[StudioRow]
)

object BookingCalendarView {
  implicit val bookingCalendarViewFormat: OFormat[BookingCalendarView] =
    Json.format[BookingCalendarView]
}

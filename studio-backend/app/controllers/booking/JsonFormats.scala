package controllers.booking

import play.api.libs.json.*
import application.booking.*
import java.time.*
import java.time.format.DateTimeFormatter

object JsonFormats {

  implicit val localTimeWrites: Writes[LocalTime] =
    Writes.temporalWrites[LocalTime, String]("HH:mm")
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, String]("yyyy-MM-dd")

  // SlotViewのcompanion objectのフォーマットを使用
  implicit val slotViewWrites: Writes[SlotView] = SlotView.slotViewFormat
  implicit val studioRowWrites: Writes[StudioRow] = StudioRow.studioRowFormat
  implicit val bookingCalendarViewWrites: Writes[BookingCalendarView] =
    BookingCalendarView.bookingCalendarViewFormat
}

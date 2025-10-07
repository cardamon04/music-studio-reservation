package infrastructure.booking

import application.booking.{
  BookingCalendarQueryService,
  BookingCalendarView,
  StudioRow,
  SlotView
}
import domain.booking.{
  Booking,
  BookingStatus,
  StudioId,
  PeriodId,
  ReservationType
}
import domain.reservationtype.{ReservationTypeRepository, ReservationTypeCode}
import java.time.*
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Singleton}

@Singleton
final class InMemoryBookingCalendarQueryService @Inject() (
    bookingRepository: InMemoryBookingRepository,
    reservationTypeRepository: ReservationTypeRepository
)(implicit ec: ExecutionContext)
    extends BookingCalendarQueryService {

  // ★本来はDB: PeriodDefinition/DayHalfMapping/Booking等から集計
  private val periods: List[(String, LocalTime, LocalTime)] = List(
    ("P1", LocalTime.of(9, 0), LocalTime.of(10, 0)),
    ("P2", LocalTime.of(10, 0), LocalTime.of(11, 0)),
    ("P3", LocalTime.of(11, 0), LocalTime.of(12, 0)),
    ("P4", LocalTime.of(13, 0), LocalTime.of(14, 0)),
    ("P5", LocalTime.of(14, 0), LocalTime.of(15, 0))
  )

  // ダミー：スタジオ
  private val studios: List[(String, String)] = List(
    ("A", "Aスタ"),
    ("B", "Bスタ"),
    ("C", "Cスタ")
  )

  override def getByDate(
      date: LocalDate,
      studioId: Option[String]
  ): Future[BookingCalendarView] = {
    // InMemoryBookingRepositoryから実際の予約データを取得
    bookingRepository.findByDate(date).map { bookings =>
      val now = LocalDateTime.now()
      val rows = studios
        .filter { case (id, _) => studioId.forall(_ == id) }
        .map { case (id, name) =>
          val studioBookings = bookings.filter(_.studioId.value == id)
          val slotViews = periods.map { case (pid, start, end) =>
            val bookingOpt = studioBookings.find(_.period.value == pid)
            val (statusJp, bidOpt, rTypeOpt, eventNameOpt, usingStart) =
              bookingOpt match {
                case Some(booking) =>
                  val s = booking.status match {
                    case BookingStatus.Reserved  => "予約済み"
                    case BookingStatus.Completed => "使用中"
                    case BookingStatus.Cancelled => "予約キャンセル"
                  }
                  val reservationTypeJp =
                    booking.reservationType.value match {
                      case "StudentRental"    => "学生レンタル"
                      case "ClassRental"      => "授業レンタル"
                      case "EventReservation" => "イベント予約"
                      case _                  => "不明"
                    }
                  (
                    s,
                    Some(booking.bookingId.value),
                    Some(reservationTypeJp),
                    booking.eventName.map(_.value),
                    None
                  )
                case None =>
                  ("空", None, None, None, None)
              }
            val slotStartDateTime = LocalDateTime.of(date, start)
            val graceExpired = bidOpt.isDefined &&
              usingStart.isEmpty && // 未Check-in
              now.isAfter(slotStartDateTime.plusMinutes(10)) // 10分経過

            SlotView(
              periodId = pid,
              status = statusJp,
              bookingId = bidOpt,
              reservationType = rTypeOpt,
              eventName = eventNameOpt,
              graceExpired = graceExpired,
              startTime = LocalDateTime.of(date, start),
              endTime = LocalDateTime.of(date, end)
            )
          }
          StudioRow(id, name, slotViews)
        }

      BookingCalendarView(
        usageDate = date,
        periodOrder = periods.map(_._1),
        rows = rows
      )
    }
  }
}

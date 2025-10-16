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
  BookingId,
  StudioId,
  PeriodId,
  ReservationType,
  EventName,
  SlotStatus,
  CalendarDate
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
      studioId: Option[StudioId]
  ): Future[BookingCalendarView] = {
    // InMemoryBookingRepositoryから実際の予約データを取得
    bookingRepository.findByDate(date).map { bookings =>
      val now = LocalDateTime.now()
      val rows = studios
        .filter { case (id, _) => studioId.forall(_.value == id) }
        .map { case (id, name) =>
          val studioBookings = bookings.filter(_.studioId.value == id)
          val slotViews = periods.map { case (pid, start, end) =>
            val bookingOpt = studioBookings.find(_.period.value == pid)
            val (status, bidOpt, rTypeOpt, eventNameOpt, usingStart) =
              bookingOpt match {
                case Some(booking) =>
                  val s = booking.status match {
                    case BookingStatus.Reserved  => SlotStatus.Reserved
                    case BookingStatus.Completed => SlotStatus.InUse
                    case BookingStatus.Cancelled => SlotStatus.Cancelled
                  }
                  (
                    s,
                    Some(booking.bookingId),
                    Some(booking.reservationType),
                    booking.eventName,
                    None
                  )
                case None =>
                  (SlotStatus.Empty, None, None, None, None)
              }
            val slotStartDateTime = LocalDateTime.of(date, start)
            val graceExpired = bidOpt.isDefined &&
              usingStart.isEmpty && // 未Check-in
              now.isAfter(slotStartDateTime.plusMinutes(10)) // 10分経過

            SlotView(
              periodId = PeriodId.fromString(pid).get,
              status = status,
              bookingId = bidOpt,
              reservationType = rTypeOpt,
              eventName = eventNameOpt,
              graceExpired = graceExpired,
              startTime = LocalDateTime.of(date, start),
              endTime = LocalDateTime.of(date, end)
            )
          }
          StudioRow(StudioId.fromString(id).get, name, slotViews)
        }

      BookingCalendarView(
        usageDate = CalendarDate
          .fromLocalDate(date)
          .getOrElse(throw new IllegalArgumentException("Invalid date")),
        periodOrder = periods.map(p => PeriodId.fromString(p._1).get),
        rows = rows
      )
    }
  }
}

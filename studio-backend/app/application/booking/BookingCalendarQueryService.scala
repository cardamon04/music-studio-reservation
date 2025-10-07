package application.booking

import java.time.LocalDate
import scala.concurrent.Future

trait BookingCalendarQueryService {
  def getByDate(
      date: LocalDate,
      studioId: Option[String]
  ): Future[BookingCalendarView]
}

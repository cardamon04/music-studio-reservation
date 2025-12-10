package domain.booking

import domain.student.StudentNumber
import java.time.LocalDate
import scala.concurrent.Future

/** 予約リポジトリの契約 (ドメイン側ポート) */
trait BookingRepository {

  def save(booking: Booking): Future[Booking]

  def findById(bookingId: BookingId): Future[Option[Booking]]

  def findByDateStudioPeriod(
      date: LocalDate,
      studioId: StudioId,
      period: PeriodId
  ): Future[Option[Booking]]

  def findByDate(date: LocalDate): Future[List[Booking]]

  def findByStudioId(studioId: StudioId): Future[List[Booking]]

  def findByStudentNumber(studentNumber: StudentNumber): Future[List[Booking]]

  /** 指定日・時間帯に学生が予約済みかを確認 */
  def hasStudentBookingOnPeriod(
      date: LocalDate,
      period: PeriodId,
      studentNumber: StudentNumber
  ): Future[Boolean]

  def findAll(): Future[List[Booking]]

  def delete(bookingId: BookingId): Future[Boolean]
}

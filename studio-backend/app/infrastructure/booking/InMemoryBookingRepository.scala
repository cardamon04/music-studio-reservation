package infrastructure.booking

import domain.booking.{
  Booking,
  BookingId,
  BookingRepository,
  BookingStatus,
  StudioId,
  PeriodId,
  EventName,
  ReservationType
}
import domain.student.StudentNumber
import java.time.LocalDate
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Singleton}

/** InMemory 予約リポジトリ (開発・テスト用の簡易実装) */
@Singleton
class InMemoryBookingRepository @Inject() ()(implicit ec: ExecutionContext)
    extends BookingRepository {

  // 予約ID -> 予約本体
  private val bookings = TrieMap[BookingId, Booking]()

  // (日付, スタジオ, 時限) -> 予約ID で重複チェック用
  private val dateStudioPeriodIndex =
    TrieMap[(LocalDate, StudioId, PeriodId), BookingId]()

  /** 予約を保存 */
  override def save(booking: Booking): Future[Booking] = Future {
    bookings.put(booking.bookingId, booking)

    val key = (booking.usageDate, booking.studioId, booking.period)
    dateStudioPeriodIndex.put(key, booking.bookingId)

    booking
  }

  /** IDで取得 */
  override def findById(bookingId: BookingId): Future[Option[Booking]] =
    Future {
      bookings.get(bookingId)
    }

  /** 日付・スタジオ・時限で取得 */
  override def findByDateStudioPeriod(
      date: LocalDate,
      studioId: StudioId,
      period: PeriodId
  ): Future[Option[Booking]] = Future {
    val key = (date, studioId, period)
    dateStudioPeriodIndex.get(key).flatMap(bookings.get)
  }

  /** 日付で一覧取得 */
  override def findByDate(date: LocalDate): Future[List[Booking]] = Future {
    bookings.values
      .filter(_.usageDate == date)
      .toList
      .sortBy(_.createdAt)
  }

  /** スタジオIDで一覧取得 */
  override def findByStudioId(studioId: StudioId): Future[List[Booking]] =
    Future {
      bookings.values
        .filter(_.studioId == studioId)
        .toList
        .sortBy(_.createdAt)
    }

  /** 学籍番号で一覧取得 */
  override def findByStudentNumber(
      studentNumber: StudentNumber
  ): Future[List[Booking]] = Future {
    bookings.values
      .filter(_.members.contains(studentNumber))
      .toList
      .sortBy(_.createdAt)
  }

  /** 全件取得 */
  override def findAll(): Future[List[Booking]] = Future {
    bookings.values.toList.sortBy(_.createdAt)
  }

  /** 予約を削除 */
  override def delete(bookingId: BookingId): Future[Boolean] = Future {
    bookings.get(bookingId) match {
      case Some(booking) =>
        bookings.remove(bookingId)

        val key = (booking.usageDate, booking.studioId, booking.period)
        dateStudioPeriodIndex.remove(key)

        true
      case None =>
        false
    }
  }

  /** 指定日・時限に学生が予約済みか確認 */
  override def hasStudentBookingOnPeriod(
      date: LocalDate,
      period: PeriodId,
      studentNumber: StudentNumber
  ): Future[Boolean] = Future {
    bookings.values.exists { booking =>
      booking.usageDate == date &&
      booking.period == period &&
      booking.members.contains(studentNumber) &&
      booking.isValid
    }
  }

  /** テスト用のリセット */
  def clear(): Unit = {
    bookings.clear()
    dateStudioPeriodIndex.clear()
  }

  /** 件数取得 */
  def count(): Int = bookings.size
}

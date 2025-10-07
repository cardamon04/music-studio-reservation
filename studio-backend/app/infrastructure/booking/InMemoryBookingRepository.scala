package infrastructure.booking

import domain.booking.{
  Booking,
  BookingId,
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

/** InMemory予約リポジトリ
  *
  * DDDのインフラストラクチャ層として、予約データの永続化を担当 開発・テスト用のInMemory実装
  */
@Singleton
class InMemoryBookingRepository @Inject() ()(implicit ec: ExecutionContext)
    extends BookingRepository {

  // スレッドセーフなMapで予約データを管理
  private val bookings = TrieMap[BookingId, Booking]()

  // 日付・スタジオ・時間枠での高速検索用インデックス
  private val dateStudioPeriodIndex =
    TrieMap[(LocalDate, StudioId, PeriodId), BookingId]()

  /** 予約を保存
    *
    * @param booking
    *   保存する予約
    * @return
    *   保存された予約
    */
  override def save(booking: Booking): Future[Booking] = Future {
    bookings.put(booking.bookingId, booking)

    // インデックスを更新
    val key = (booking.usageDate, booking.studioId, booking.period)
    dateStudioPeriodIndex.put(key, booking.bookingId)

    booking
  }

  /** 予約IDで予約を取得
    *
    * @param bookingId
    *   予約ID
    * @return
    *   予約（存在しない場合はNone）
    */
  override def findById(bookingId: BookingId): Future[Option[Booking]] =
    Future {
      bookings.get(bookingId)
    }

  /** 日付・スタジオ・時間枠で予約を検索
    *
    * @param date
    *   使用日
    * @param studioId
    *   スタジオID
    * @param period
    *   時間枠ID
    * @return
    *   予約（存在しない場合はNone）
    */
  override def findByDateStudioPeriod(
      date: LocalDate,
      studioId: StudioId,
      period: PeriodId
  ): Future[Option[Booking]] = Future {
    val key = (date, studioId, period)
    dateStudioPeriodIndex.get(key).flatMap(bookings.get)
  }

  /** 日付で予約一覧を取得
    *
    * @param date
    *   使用日
    * @return
    *   該当日付の予約一覧
    */
  override def findByDate(date: LocalDate): Future[List[Booking]] = Future {
    bookings.values
      .filter(_.usageDate == date)
      .toList
      .sortBy(_.createdAt)
  }

  /** スタジオIDで予約一覧を取得
    *
    * @param studioId
    *   スタジオID
    * @return
    *   該当スタジオの予約一覧
    */
  override def findByStudioId(studioId: StudioId): Future[List[Booking]] =
    Future {
      bookings.values
        .filter(_.studioId == studioId)
        .toList
        .sortBy(_.createdAt)
    }

  /** 学生番号で予約一覧を取得
    *
    * @param studentNumber
    *   学生番号
    * @return
    *   該当学生の予約一覧
    */
  override def findByStudentNumber(
      studentNumber: StudentNumber
  ): Future[List[Booking]] = Future {
    bookings.values
      .filter(_.members.contains(studentNumber))
      .toList
      .sortBy(_.createdAt)
  }

  /** すべての予約を取得
    *
    * @return
    *   全予約一覧
    */
  override def findAll(): Future[List[Booking]] = Future {
    bookings.values.toList.sortBy(_.createdAt)
  }

  /** 予約を削除
    *
    * @param bookingId
    *   予約ID
    * @return
    *   削除成功の場合true
    */
  override def delete(bookingId: BookingId): Future[Boolean] = Future {
    bookings.get(bookingId) match {
      case Some(booking) =>
        bookings.remove(bookingId)

        // インデックスからも削除
        val key = (booking.usageDate, booking.studioId, booking.period)
        dateStudioPeriodIndex.remove(key)

        true
      case None =>
        false
    }
  }

  /** 指定された日付・時間枠で学生が既に予約しているかチェック
    *
    * @param date
    *   利用日
    * @param period
    *   時間枠
    * @param studentNumber
    *   学生番号
    * @return
    *   既に予約している場合true
    */
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

  /** リポジトリの状態をクリア（テスト用）
    */
  def clear(): Unit = {
    bookings.clear()
    dateStudioPeriodIndex.clear()
  }

  /** 現在の予約数を取得
    *
    * @return
    *   予約数
    */
  def count(): Int = bookings.size
}

/** 予約リポジトリのインターフェース
  *
  * DDDのドメイン層で定義されるリポジトリインターフェース
  */
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

  /** 指定された日付・時間枠で学生が既に予約しているかチェック
    *
    * @param date
    *   利用日
    * @param period
    *   時間枠
    * @param studentNumber
    *   学生番号
    * @return
    *   既に予約している場合true
    */
  def hasStudentBookingOnPeriod(
      date: LocalDate,
      period: PeriodId,
      studentNumber: StudentNumber
  ): Future[Boolean]

  def findAll(): Future[List[Booking]]

  def delete(bookingId: BookingId): Future[Boolean]
}

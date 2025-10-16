package application.booking

import domain.booking.{Booking, BookingId, BookingStatus, StudioId, PeriodId, EventName, ReservationType, UsageDate}
import domain.student.StudentNumber
import domain.equipment.{EquipmentItem, EquipmentId}
import infrastructure.booking.BookingRepository
import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import domain.reservationtype.{ReservationTypeRepository, ReservationTypeCode}
import javax.inject.{Inject, Singleton}
import scala.util.{Try, Success, Failure}

/** 予約作成リクエスト（DTOとして機能）
  *
  * バリデーションはドメイン層に委譲
  */
case class CreateBookingRequest(
    studioId: String,
    period: String,
    usageDate: String,
    reservationType: String,
    members: List[String],
    equipmentItems: List[EquipmentItemRequest],
    eventName: Option[String] // イベント予約の場合のイベント名
)

/** 備品リクエスト（DTO）
  */
case class EquipmentItemRequest(
    equipmentId: String,
    quantity: Int
)

/** 予約アプリケーションサービス
  *
  * DDDのアプリケーション層として、予約のビジネスロジックを管理
  */
@Singleton
class BookingApplicationService @Inject() (
    bookingRepository: BookingRepository,
    reservationTypeRepository: ReservationTypeRepository
)(using executionContext: ExecutionContext) {

  /** 予約を作成
    *
    * @param request 予約作成リクエスト
    * @return 作成された予約
    */
  def createBooking(request: CreateBookingRequest): Future[Booking] = {
    for {
      booking <- createBookingFromRequest(request)
      savedBooking <- saveBookingWithDuplicateCheck(booking)
    } yield savedBooking
  }

  /** リクエストからドメインオブジェクトを作成（ドメイン層にバリデーションを委譲）
    */
  private def createBookingFromRequest(request: CreateBookingRequest): Future[Booking] = {
    val result = for {
      // 値オブジェクトに変換（各値オブジェクトが自己バリデーション）
      usageDate <- UsageDate.fromString(request.usageDate)
      studioId <- StudioId.fromString(request.studioId)
        .toRight(s"Invalid studio ID: ${request.studioId}")
      periodId <- PeriodId.fromString(request.period)
        .toRight(s"Invalid period ID: ${request.period}")
      reservationType <- ReservationType.fromString(request.reservationType)
        .toRight(s"Invalid reservation type: ${request.reservationType}")

      // 学生番号リスト（無効な学生番号は除外）
      members = request.members.flatMap(StudentNumber.fromString)

      // 備品リスト（DTOからドメインオブジェクトに変換、バリデーション付き）
      equipmentItems <- toEquipmentItems(request.equipmentItems)

      // イベント名（オプション）
      eventName <- request.eventName match {
        case Some(name) =>
          EventName.fromString(name)
            .toRight(s"Invalid event name: $name")
            .map(Some(_))
        case None => Right(None)
      }

      // ドメインオブジェクトを作成（ビジネスルール検証を含む）
      booking <- Booking.create(
        studioId,
        periodId,
        usageDate,
        reservationType,
        members,
        equipmentItems,
        eventName
      )
    } yield booking

    // Either を Future に変換
    result match {
      case Right(booking) => Future.successful(booking)
      case Left(errorMessage) => Future.failed(new IllegalArgumentException(errorMessage))
    }
  }

  /** 備品リクエストをドメインオブジェクトに変換
    *
    * DTOからドメインの値オブジェクトへの変換。
    * バリデーションはドメイン層（EquipmentItem）に完全に委譲。
    */
  private def toEquipmentItems(
      items: List[EquipmentItemRequest]
  ): Either[String, List[EquipmentItem]] = {
    items.foldLeft[Either[String, List[EquipmentItem]]](Right(Nil)) {
      case (Right(acc), item) =>
        // ドメイン層のファクトリメソッドで変換（バリデーション含む）
        EquipmentItem.fromStrings(item.equipmentId, item.quantity) match {
          case Right(equipmentItem) => Right(acc :+ equipmentItem)
          case Left(error)          => Left(error)
        }
      case (Left(error), _) => Left(error)
    }
  }

  /** 重複チェック付きで予約を保存
    */
  private def saveBookingWithDuplicateCheck(booking: Booking): Future[Booking] = {
    for {
      // 同じスタジオ・時間枠の重複チェック
      existingStudioBooking <- bookingRepository.findByDateStudioPeriod(booking.usageDate, booking.studioId, booking.period)
      _ <- existingStudioBooking match {
        case Some(existing) if existing.isValid =>
          Future.failed(new IllegalStateException(
            s"予約が既に存在します: 日付=${booking.usageDate}, スタジオ=${booking.studioId.value}, 時間枠=${booking.period.value}"
          ))
        case _ => Future.successful(())
      }

      // 学生の重複チェック（同じ時間枠で他のスタジオに予約していないか）
      studentConflicts <- Future.sequence(
        booking.members.map { studentNumber =>
          bookingRepository.hasStudentBookingOnPeriod(booking.usageDate, booking.period, studentNumber)
        }
      )

      _ <- if (studentConflicts.contains(true)) {
        val conflictingStudents = booking.members.zip(studentConflicts)
          .filter(_._2)
          .map(_._1.value)
          .mkString(", ")
        Future.failed(new IllegalStateException(
          s"学生が既に同じ時間枠で予約しています: ${conflictingStudents}"
        ))
      } else {
        Future.successful(())
      }

      // 重複がない場合は保存
      savedBooking <- bookingRepository.save(booking)
    } yield savedBooking
  }

  /** 予約を取得
    *
    *
    * @param bookingId 予約ID
    * @return 予約（存在しない場合はNone）
    */
  def getBooking(bookingId: String): Future[Option[Booking]] = {
    bookingRepository.findById(BookingId.fromString(bookingId).get)
  }

  /** 予約をキャンセル
    *
    * @param bookingId 予約ID
    * @return キャンセルされた予約
    */
  def cancelBooking(bookingId: String): Future[Booking] = {
    for {
      bookingOpt <- bookingRepository.findById(BookingId.fromString(bookingId).get)
      booking <- bookingOpt match {
        case Some(b) if b.isValid =>
          val cancelledBooking = b.cancel()
          bookingRepository.save(cancelledBooking)
        case Some(b) if b.isCancelled =>
          Future.failed(new IllegalStateException("既にキャンセル済みの予約です"))
        case Some(_) =>
          Future.failed(new IllegalStateException("完了済みの予約はキャンセルできません"))
        case None =>
          Future.failed(new IllegalArgumentException("予約が見つかりません"))
      }
    } yield booking
  }

  /** 日付で予約一覧を取得
    *
    * @param date 日付
    * @return 該当日付の予約一覧
    */
  def getBookingsByDate(date: LocalDate): Future[List[Booking]] = {
    bookingRepository.findByDate(date)
  }

  /** スタジオIDで予約一覧を取得
    *
    * @param studioId スタジオID
    * @return 該当スタジオの予約一覧
    */
  def getBookingsByStudioId(studioId: String): Future[List[Booking]] = {
    bookingRepository.findByStudioId(StudioId.fromString(studioId).get)
  }

  /** 学生番号で予約一覧を取得
    *
    * @param studentNumber 学生番号
    * @return 該当学生の予約一覧
    */
  def getBookingsByStudentNumber(studentNumber: String): Future[List[Booking]] = {
    bookingRepository.findByStudentNumber(StudentNumber.fromString(studentNumber).get)
  }

  /** すべての予約を取得
    *
    * @return 全予約一覧
    */
  def getAllBookings(): Future[List[Booking]] = {
    bookingRepository.findAll()
  }
}

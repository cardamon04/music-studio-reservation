package application.booking

import domain.booking.{Booking, BookingId, BookingStatus, StudioId, PeriodId, EventName, ReservationType}
import domain.student.StudentNumber
import domain.equipment.{EquipmentItem, EquipmentId}
import infrastructure.booking.BookingRepository
import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import domain.reservationtype.{ReservationTypeRepository, ReservationTypeCode}
import javax.inject.{Inject, Singleton}

/** 予約作成リクエスト
  */
case class CreateBookingRequest(
    studioId: String,
    period: String,
    usageDate: String,
    reservationType: String,
    members: List[String],
    equipmentItems: List[EquipmentItemRequest],
    eventName: Option[String] // イベント予約の場合のイベント名
) {
  /** リクエストのバリデーション
    */
  def validate(): Either[String, CreateBookingRequest] = {
    // 日付の検証
    val parsedDate = try {
      java.time.LocalDate.parse(usageDate)
    } catch {
      case _: Exception => return Left(s"Invalid date format: $usageDate")
    }

    // 日付範囲の検証（1週間後まで）
    val today = java.time.LocalDate.now()
    val maxDate = today.plusDays(7)

    if (parsedDate.isBefore(today)) {
      return Left(s"過去の日付は予約できません: $usageDate")
    }
    if (parsedDate.isAfter(maxDate)) {
      return Left(s"1週間後以降の日付は予約できません: $usageDate (最大: $maxDate)")
    }

    // 予約タイプの検証
    ReservationType.fromString(reservationType) match {
      case None => return Left(s"Invalid reservation type: $reservationType")
      case Some(resType) =>
        // イベント名の検証
        eventName match {
          case Some(_) if resType != ReservationType.EventReservation =>
            return Left("イベント名はイベント予約の場合のみ指定できます")
          case None if resType == ReservationType.EventReservation =>
            return Left("イベント予約の場合はイベント名を指定してください")
          case _ => // OK
        }
    }

    // 備品リクエストの検証
    equipmentItems.find(_.validate().isLeft) match {
      case Some(item) =>
        item.validate() match {
          case Left(error) => Left(s"備品リクエストエラー: $error")
          case Right(_) => Right(this) // この行は到達しない
        }
      case None =>
        Right(this)
    }
  }
}

case class EquipmentItemRequest(
    equipmentId: String,
    quantity: Int
) {
  /** 備品リクエストのバリデーション
    */
  def validate(): Either[String, EquipmentItemRequest] = {
    if (equipmentId.trim.isEmpty) {
      Left("備品IDは必須です")
    } else if (quantity <= 0) {
      Left("数量は1以上である必要があります")
    } else {
      Right(this)
    }
  }
}

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
      validatedRequest <- validateRequest(request)
      booking <- createBookingFromRequest(validatedRequest)
      savedBooking <- saveBookingWithDuplicateCheck(booking)
    } yield savedBooking
  }

  /** リクエストのバリデーション（値オブジェクトに委譲）
    */
  private def validateRequest(request: CreateBookingRequest): Future[CreateBookingRequest] = {
    request.validate() match {
      case Right(validRequest) => Future.successful(validRequest)
      case Left(errorMessage) => Future.failed(new IllegalArgumentException(errorMessage))
    }
  }

  /** リクエストからドメインオブジェクトを作成
    */
  private def createBookingFromRequest(request: CreateBookingRequest): Future[Booking] = {
    Future {
      val usageDate = LocalDate.parse(request.usageDate)
      val studioId = StudioId.fromString(request.studioId).get
      val periodId = PeriodId.fromString(request.period).get
      val reservationType = ReservationType.fromString(request.reservationType).get

      val members = request.members.flatMap(StudentNumber.fromString)
      val equipmentItems = request.equipmentItems.map { item =>
        EquipmentItem(EquipmentId.fromString(item.equipmentId).get, item.quantity)
      }

      val eventName = request.eventName.flatMap(EventName.fromString)
      val now = java.time.LocalDateTime.now()
      val bookingId = BookingId.generate()

      Booking(
        bookingId = bookingId,
        studioId = studioId,
        period = periodId,
        usageDate = usageDate,
        reservationType = reservationType,
        members = members,
        equipmentItems = equipmentItems,
        eventName = eventName,
        status = BookingStatus.Reserved,
        createdAt = now,
        updatedAt = now
      )
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

package domain.booking

import java.time.LocalDate
import _root_.domain.student.StudentNumber
import _root_.domain.equipment.EquipmentItem

/** 予約ドメインエンティティ
  *
  * DDDのドメイン層として、予約のビジネスルールと状態を管理する
  */
case class Booking(
    bookingId: BookingId,
    studioId: StudioId,
    period: PeriodId,
    usageDate: LocalDate,
    reservationType: ReservationType,
    members: List[StudentNumber],
    equipmentItems: List[EquipmentItem],
    eventName: Option[EventName], // イベント予約の場合のイベント名
    status: BookingStatus,
    createdAt: java.time.LocalDateTime,
    updatedAt: java.time.LocalDateTime
) {

  /** 予約のキャンセル
    *
    * @return
    *   キャンセルされた予約
    */
  def cancel(): Booking = {
    this.copy(
      status = BookingStatus.Cancelled,
      updatedAt = java.time.LocalDateTime.now()
    )
  }

  /** 予約の完了
    *
    * @return
    *   完了された予約
    */
  def complete(): Booking = {
    this.copy(
      status = BookingStatus.Completed,
      updatedAt = java.time.LocalDateTime.now()
    )
  }

  /** 予約が有効かどうか
    *
    * @return
    *   有効な予約の場合true
    */
  def isValid: Boolean = status == BookingStatus.Reserved

  /** 予約がキャンセル済みかどうか
    *
    * @return
    *   キャンセル済みの場合true
    */
  def isCancelled: Boolean = status == BookingStatus.Cancelled
}

object Booking {

  /** 予約を作成（ビジネスルール検証付き）
    *
    * 値オブジェクトに検証を委譲し、ドメインロジックを集約
    *
    * @param studioId
    *   スタジオID
    * @param period
    *   時間枠
    * @param usageDate
    *   利用日
    * @param reservationType
    *   予約タイプ
    * @param members
    *   利用学生リスト
    * @param equipmentItems
    *   レンタル備品リスト
    * @param eventName
    *   イベント名（イベント予約の場合のみ）
    * @return
    *   作成された予約またはエラーメッセージ
    */
  def create(
      studioId: StudioId,
      period: PeriodId,
      usageDate: UsageDate,
      reservationType: ReservationType,
      members: List[StudentNumber],
      equipmentItems: List[EquipmentItem],
      eventName: Option[EventName]
  ): Either[String, Booking] = {

    // 予約タイプが自身のビジネスルールに基づいてイベント名を検証
    reservationType.validateEventName(eventName).map { _ =>
      val now = java.time.LocalDateTime.now()
      Booking(
        bookingId = BookingId.generate(),
        studioId = studioId,
        period = period,
        usageDate = usageDate.value,
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
}

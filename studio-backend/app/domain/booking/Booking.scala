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

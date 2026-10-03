package domain.equipment

import domain.booking.{BookingId, EquipmentValidationError}

/** 予約・備品ごとの貸出累計。数量計算の成功だけでは受付操作の許可を表さない。
  * 認証、予約状態、返却後の追加貸出可否は、呼び出し側で別途判定する。
  */
final class LoanLine private (
    val bookingId: BookingId,
    val equipmentId: EquipmentId,
    val reservedQuantity: Int,
    val checkedOutQuantity: Int,
    val returnedQuantity: Int
) {
  require(reservedQuantity >= 0, "予約数量は0以上である必要があります")
  require(checkedOutQuantity >= 0 && checkedOutQuantity <= reservedQuantity,
    "累計貸出数量は0以上、予約数量以下である必要があります")
  require(returnedQuantity >= 0 && returnedQuantity <= checkedOutQuantity,
    "累計返却数量は0以上、累計貸出数量以下である必要があります")

  def checkout(quantity: Int): Either[EquipmentValidationError, LoanLine] =
    if (quantity <= 0)
      Left(EquipmentValidationError("貸出数量は1以上である必要があります"))
    else if (quantity > reservedQuantity - checkedOutQuantity)
      Left(EquipmentValidationError("予約数量を超えて貸出できません"))
    else Right(new LoanLine(
      bookingId, equipmentId, reservedQuantity,
      checkedOutQuantity + quantity, returnedQuantity
    ))

  def returnEquipment(quantity: Int): Either[EquipmentValidationError, LoanLine] =
    if (quantity <= 0)
      Left(EquipmentValidationError("返却数量は1以上である必要があります"))
    else if (quantity > checkedOutQuantity - returnedQuantity)
      Left(EquipmentValidationError("未返却数量を超えて返却できません"))
    else Right(new LoanLine(
      bookingId, equipmentId, reservedQuantity,
      checkedOutQuantity, returnedQuantity + quantity
    ))
}

object LoanLine {
  /** 累計0の明細を作る。負の予約数量は契約違反として拒否する。
    * 予約入力の数量上限や引当在庫の妥当性はここでは判定しない。
    */
  def apply(bookingId: BookingId, equipmentId: EquipmentId, reservedQuantity: Int): LoanLine =
    new LoanLine(bookingId, equipmentId, reservedQuantity, 0, 0)
}

package domain.equipment

import domain.booking.{BookingId, EquipmentValidationError}

case class LoanLine(
    bookingId: BookingId,
    equipmentId: EquipmentId,
    reservedQuantity: Int,
    checkedOutQuantity: Int = 0,
    returnedQuantity: Int = 0
) {
  def checkout(quantity: Int): Either[EquipmentValidationError, LoanLine] =
    if (quantity <= 0)
      Left(EquipmentValidationError("貸出数量は1以上である必要があります"))
    else if (quantity > reservedQuantity - checkedOutQuantity)
      Left(EquipmentValidationError("予約数量を超えて貸出できません"))
    else Right(copy(checkedOutQuantity = checkedOutQuantity + quantity))

  def returnEquipment(quantity: Int): Either[EquipmentValidationError, LoanLine] =
    if (quantity <= 0)
      Left(EquipmentValidationError("返却数量は1以上である必要があります"))
    else if (quantity > checkedOutQuantity - returnedQuantity)
      Left(EquipmentValidationError("未返却数量を超えて返却できません"))
    else Right(copy(returnedQuantity = returnedQuantity + quantity))
}

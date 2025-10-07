package domain.equipment

/** 備品アイテム（値オブジェクト）
  *
  * 成瀬允宣氏のDDD定義に従った値オブジェクト
  */
case class EquipmentItem(
    equipmentId: EquipmentId,
    quantity: Int
) {
  require(quantity > 0, "備品の数量は1以上である必要があります")
  require(quantity <= 100, "備品の数量は100以下である必要があります")

  override def equals(obj: Any): Boolean = obj match {
    case other: EquipmentItem =>
      this.equipmentId == other.equipmentId && this.quantity == other.quantity
    case _ => false
  }

  override def hashCode(): Int = equipmentId.hashCode + quantity.hashCode
}

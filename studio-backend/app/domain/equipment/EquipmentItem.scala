package domain.equipment

/** 備品アイテム（値オブジェクト）
  *
  * 備品ID と数量の組み合わせ。 数量は Quantity 値オブジェクトで表現される。
  */
case class EquipmentItem(
    equipmentId: EquipmentId,
    quantity: Quantity
) {

  override def equals(obj: Any): Boolean = obj match {
    case other: EquipmentItem =>
      this.equipmentId == other.equipmentId && this.quantity == other.quantity
    case _ => false
  }

  override def hashCode(): Int = equipmentId.hashCode + quantity.hashCode
}

object EquipmentItem {

  /** ファクトリメソッド（ビジネスルール検証付き）
    *
    * @param equipmentId
    *   備品ID
    * @param quantity
    *   数量（整数値）
    * @return
    *   作成された EquipmentItem またはエラーメッセージ
    */
  def create(
      equipmentId: EquipmentId,
      quantity: Int
  ): Either[String, EquipmentItem] = {
    Quantity.fromInt(quantity).map { qty =>
      EquipmentItem(equipmentId, qty)
    }
  }

  /** 文字列とIntから EquipmentItem を作成
    *
    * @param equipmentIdStr
    *   備品ID文字列
    * @param quantityInt
    *   数量
    * @return
    *   作成された EquipmentItem またはエラーメッセージ
    */
  def fromStrings(
      equipmentIdStr: String,
      quantityInt: Int
  ): Either[String, EquipmentItem] = {
    for {
      equipmentId <- EquipmentId
        .fromString(equipmentIdStr)
        .toRight(s"Invalid equipment ID: $equipmentIdStr")
      quantity <- Quantity.fromInt(quantityInt)
      item = EquipmentItem(equipmentId, quantity)
    } yield item
  }
}

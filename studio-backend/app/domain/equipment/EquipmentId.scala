package domain.equipment

import java.util.UUID

/** 備品ID（値オブジェクト）
  *
  * 成瀬允宣氏のDDD定義に従った値オブジェクト
  */
case class EquipmentId(value: String) {
  require(value.nonEmpty, "備品IDは空文字列にできません")
  require(value.length <= 50, "備品IDは50文字以内である必要があります")
  require(value.matches("^[A-Za-z0-9_-]+$"), "備品IDは英数字、ハイフン、アンダースコアのみ使用可能です")

  override def toString: String = value

  override def equals(obj: Any): Boolean = obj match {
    case other: EquipmentId => this.value == other.value
    case _                  => false
  }

  override def hashCode(): Int = value.hashCode
}

object EquipmentId {
  def generate(): EquipmentId = EquipmentId(UUID.randomUUID().toString)

  /** 文字列からEquipmentIdを作成
    *
    * @param value
    *   備品ID文字列
    * @return
    *   作成されたEquipmentIdまたはNone
    */
  def fromString(value: String): Option[EquipmentId] = {
    try {
      Some(EquipmentId(value))
    } catch {
      case _: IllegalArgumentException => None
    }
  }
}

package domain.booking

/** スタジオID（値オブジェクト）
  *
  * 成瀬允宣氏のDDD定義に従った値オブジェクト
  */
case class StudioId(value: String) {
  require(value.nonEmpty, "スタジオIDは空文字列にできません")
  require(value.length <= 10, "スタジオIDは10文字以内である必要があります")
  require(value.matches("^[A-Z]$"), "スタジオIDはA-Zの1文字である必要があります")

  override def toString: String = value

  override def equals(obj: Any): Boolean = obj match {
    case other: StudioId => this.value == other.value
    case _               => false
  }

  override def hashCode(): Int = value.hashCode
}

object StudioId {

  /** 文字列からStudioIdを作成
    *
    * @param value
    *   スタジオID文字列
    * @return
    *   作成されたStudioIdまたはNone
    */
  def fromString(value: String): Option[StudioId] = {
    try {
      Some(StudioId(value))
    } catch {
      case _: IllegalArgumentException => None
    }
  }
}

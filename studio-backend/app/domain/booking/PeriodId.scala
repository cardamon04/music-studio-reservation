package domain.booking

/** 時間枠ID（値オブジェクト）
  *
  * 成瀬允宣氏のDDD定義に従った値オブジェクト
  */
case class PeriodId(value: String) {
  require(value.nonEmpty, "時間枠IDは空文字列にできません")
  require(value.matches("^P[1-9]\\d*$"), "時間枠IDはP1, P2, P3...の形式である必要があります")

  override def toString: String = value

  override def equals(obj: Any): Boolean = obj match {
    case other: PeriodId => this.value == other.value
    case _               => false
  }

  override def hashCode(): Int = value.hashCode
}

object PeriodId {

  /** 文字列からPeriodIdを作成
    *
    * @param value
    *   期間ID文字列
    * @return
    *   作成されたPeriodIdまたはNone
    */
  def fromString(value: String): Option[PeriodId] = {
    try {
      Some(PeriodId(value))
    } catch {
      case _: IllegalArgumentException => None
    }
  }
}

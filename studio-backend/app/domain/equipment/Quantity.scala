package domain.equipment

/** 数量（値オブジェクト）
  *
  * 備品のレンタル数量を表す。1以上100以下の正の整数。
  */
case class Quantity(value: Int) {
  require(value > 0, "数量は1以上である必要があります")
  require(value <= 100, "数量は100以下である必要があります")

  override def toString: String = value.toString

  override def equals(obj: Any): Boolean = obj match {
    case other: Quantity => this.value == other.value
    case _               => false
  }

  override def hashCode(): Int = value.hashCode
}

object Quantity {

  /** 整数から Quantity を作成
    *
    * @param value
    *   数量値
    * @return
    *   作成されたQuantityまたはエラーメッセージ
    */
  def fromInt(value: Int): Either[String, Quantity] = {
    try {
      Right(Quantity(value))
    } catch {
      case e: IllegalArgumentException => Left(e.getMessage)
    }
  }

  /** Option[Int]から Quantity を作成
    *
    * @param value
    *   数量値
    * @return
    *   作成されたQuantityまたはNone
    */
  def fromIntOption(value: Int): Option[Quantity] = {
    fromInt(value).toOption
  }
}


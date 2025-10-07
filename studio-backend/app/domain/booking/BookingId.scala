package domain.booking

import java.util.UUID

/** 予約ID（値オブジェクト）
  *
  * 成瀬允宣氏のDDD定義に従った値オブジェクト
  *   - 不変性: 作成後に状態が変わらない
  *   - 同一性の非保持: 値が同じであれば同一とみなす
  *   - 副作用のない振る舞い: ドメインロジックをカプセル化
  */
case class BookingId(value: String) {
  require(value.nonEmpty, "予約IDは空文字列にできません")
  require(value.length <= 50, "予約IDは50文字以内である必要があります")

  override def toString: String = value

  override def equals(obj: Any): Boolean = obj match {
    case other: BookingId => this.value == other.value
    case _                => false
  }

  override def hashCode(): Int = value.hashCode
}

object BookingId {
  def generate(): BookingId = BookingId(UUID.randomUUID().toString)

  /** 文字列からBookingIdを作成
    *
    * @param value
    *   予約ID文字列
    * @return
    *   作成されたBookingIdまたはNone
    */
  def fromString(value: String): Option[BookingId] = {
    try {
      Some(BookingId(value))
    } catch {
      case _: IllegalArgumentException => None
    }
  }
}

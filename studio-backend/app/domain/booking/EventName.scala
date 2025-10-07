package domain.booking

import play.api.libs.json.{Json, OFormat}

/** イベント名（値オブジェクト）
  */
case class EventName(value: String) {
  require(value != null, "イベント名はnullにできません")
  require(value.trim.nonEmpty, "イベント名は空文字列にできません")
  require(value.length <= 100, "イベント名は100文字以内である必要があります")

  override def toString: String = value

  override def equals(obj: Any): Boolean = obj match {
    case other: EventName => this.value == other.value
    case _                => false
  }

  override def hashCode(): Int = value.hashCode
}

object EventName {
  implicit val format: OFormat[EventName] = Json.format[EventName]

  /** 文字列からEventNameを作成
    *
    * @param value
    *   イベント名文字列
    * @return
    *   作成されたEventNameまたはNone
    */
  def fromString(value: String): Option[EventName] = {
    try {
      Some(EventName(value))
    } catch {
      case _: IllegalArgumentException => None
    }
  }
}

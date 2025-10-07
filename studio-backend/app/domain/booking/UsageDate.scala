package domain.booking

import java.time.LocalDate
import play.api.libs.json.{Json, OFormat}

/** 利用日付（値オブジェクト）
  */
case class UsageDate(value: LocalDate) {
  require(value != null, "利用日付はnullにできません")

  override def toString: String = value.toString

  override def equals(obj: Any): Boolean = obj match {
    case other: UsageDate => this.value == other.value
    case _                => false
  }

  override def hashCode(): Int = value.hashCode
}

object UsageDate {
  implicit val format: OFormat[UsageDate] = Json.format[UsageDate]

  /** 文字列からUsageDateを作成
    *
    * @param value
    *   日付文字列（yyyy-MM-dd形式）
    * @return
    *   作成されたUsageDateまたはNone
    */
  def fromString(value: String): Option[UsageDate] = {
    try {
      Some(UsageDate(LocalDate.parse(value)))
    } catch {
      case _: Exception => None
    }
  }
}

package domain.booking

import java.time.LocalDate

/** 利用日（値オブジェクト）
  *
  * 今日から1週間後までの日付のみ許可するビジネスルール
  */
case class UsageDate(value: LocalDate) {
  require(value != null, "利用日はnullにできません")

  private val today = LocalDate.now()
  private val maxDate = today.plusDays(7)

  require(
    !value.isBefore(today),
    s"過去の日付は予約できません: $value"
  )
  require(
    !value.isAfter(maxDate),
    s"1週間後以降の日付は予約できません: $value (最大: $maxDate)"
  )

  override def toString: String = value.toString

  override def equals(obj: Any): Boolean = obj match {
    case other: UsageDate => this.value == other.value
    case _                => false
  }

  override def hashCode(): Int = value.hashCode
}

object UsageDate {

  /** 文字列から UsageDate を作成
    *
    * @param dateString
    *   日付文字列（yyyy-MM-dd形式）
    * @return
    *   作成されたUsageDateまたはエラーメッセージ
    */
  def fromString(dateString: String): Either[String, UsageDate] = {
    try {
      val date = LocalDate.parse(dateString)
      fromLocalDate(date)
    } catch {
      case _: Exception => Left(s"Invalid date format: $dateString")
    }
  }

  /** LocalDate から UsageDate を作成
    *
    * @param date
    *   LocalDate
    * @return
    *   作成されたUsageDateまたはエラーメッセージ
    */
  def fromLocalDate(date: LocalDate): Either[String, UsageDate] = {
    try {
      Right(UsageDate(date))
    } catch {
      case e: IllegalArgumentException => Left(e.getMessage)
    }
  }
}

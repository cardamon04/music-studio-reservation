package domain.booking

import java.time.LocalDate

/** カレンダー日付（値オブジェクト）
  *
  * カレンダー表示用の日付。UsageDateと異なり、過去・未来の日付制限はない。 有効な日付フォーマットであることのみを検証する。
  */
case class CalendarDate(value: LocalDate) {
  require(value != null, "カレンダー日付はnullにできません")

  override def toString: String = value.toString

  override def equals(obj: Any): Boolean = obj match {
    case other: CalendarDate => this.value == other.value
    case _                   => false
  }

  override def hashCode(): Int = value.hashCode
}

object CalendarDate {

  /** 文字列から CalendarDate を作成
    *
    * @param dateString
    *   日付文字列（yyyy-MM-dd形式）
    * @return
    *   作成されたCalendarDateまたはエラーメッセージ
    */
  def fromString(dateString: String): Either[String, CalendarDate] = {
    try {
      val date = LocalDate.parse(dateString)
      fromLocalDate(date)
    } catch {
      case _: Exception => Left(s"Invalid date format: $dateString")
    }
  }

  /** LocalDate から CalendarDate を作成
    *
    * @param date
    *   LocalDate
    * @return
    *   作成されたCalendarDateまたはエラーメッセージ
    */
  def fromLocalDate(date: LocalDate): Either[String, CalendarDate] = {
    try {
      Right(CalendarDate(date))
    } catch {
      case e: IllegalArgumentException => Left(e.getMessage)
    }
  }

  /** Option[LocalDate]から CalendarDate を作成
    *
    * @param dateString
    *   日付文字列
    * @return
    *   作成されたCalendarDateまたはNone
    */
  def fromStringOption(dateString: String): Option[CalendarDate] = {
    fromString(dateString).toOption
  }
}

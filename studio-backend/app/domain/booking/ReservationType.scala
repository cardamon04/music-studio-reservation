package domain.booking

/** 予約タイプ（列挙型）
  *
  * 各予約タイプは自身のビジネスルールを持つ
  */
sealed trait ReservationType {
  def value: String

  /** イベント名のバリデーション
    *
    * 各予約タイプが自身のルールに基づいてイベント名を検証
    *
    * @param eventName
    *   イベント名（オプション）
    * @return
    *   バリデーション結果（成功時はRight(()), 失敗時はLeft(エラーメッセージ)）
    */
  def validateEventName(eventName: Option[EventName]): Either[String, Unit]
}

object ReservationType {

  /** 学生レンタル
    *
    * イベント名は不要
    */
  case object StudentRental extends ReservationType {
    val value = "StudentRental"

    def validateEventName(
        eventName: Option[EventName]
    ): Either[String, Unit] = {
      eventName match {
        case Some(_) => Left("イベント名は学生レンタルでは指定できません")
        case None    => Right(())
      }
    }
  }

  /** 授業レンタル
    *
    * イベント名は不要
    */
  case object ClassRental extends ReservationType {
    val value = "ClassRental"

    def validateEventName(
        eventName: Option[EventName]
    ): Either[String, Unit] = {
      eventName match {
        case Some(_) => Left("イベント名は授業レンタルでは指定できません")
        case None    => Right(())
      }
    }
  }

  /** イベント予約
    *
    * イベント名が必須
    */
  case object EventReservation extends ReservationType {
    val value = "EventReservation"

    def validateEventName(
        eventName: Option[EventName]
    ): Either[String, Unit] = {
      eventName match {
        case None    => Left("イベント予約の場合はイベント名を指定してください")
        case Some(_) => Right(())
      }
    }
  }

  /** 文字列から ReservationType を取得
    *
    * @param value
    *   予約タイプ文字列
    * @return
    *   対応する ReservationType またはNone
    */
  def fromString(value: String): Option[ReservationType] = value match {
    case "StudentRental"    => Some(StudentRental)
    case "ClassRental"      => Some(ClassRental)
    case "EventReservation" => Some(EventReservation)
    case _                  => None
  }
}

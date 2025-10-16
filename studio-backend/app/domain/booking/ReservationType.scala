package domain.booking

/** 予約タイプ（列挙型）
  */
sealed trait ReservationType {
  def value: String
}

object ReservationType {
  case object StudentRental extends ReservationType {
    val value = "StudentRental"
  }

  case object ClassRental extends ReservationType {
    val value = "ClassRental"
  }

  case object EventReservation extends ReservationType {
    val value = "EventReservation"
  }

  def fromString(value: String): Option[ReservationType] = value match {
    case "StudentRental"    => Some(StudentRental)
    case "ClassRental"      => Some(ClassRental)
    case "EventReservation" => Some(EventReservation)
    case _                  => None
  }
}

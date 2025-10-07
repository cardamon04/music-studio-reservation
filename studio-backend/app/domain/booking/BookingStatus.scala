package domain.booking

/** 予約ステータス（列挙型）
  */
sealed trait BookingStatus {
  def value: String
}

object BookingStatus {
  case object Reserved extends BookingStatus {
    val value = "Reserved"
  }

  case object Cancelled extends BookingStatus {
    val value = "Cancelled"
  }

  case object Completed extends BookingStatus {
    val value = "Completed"
  }

  def fromString(value: String): Option[BookingStatus] = value match {
    case "Reserved"  => Some(Reserved)
    case "Cancelled" => Some(Cancelled)
    case "Completed" => Some(Completed)
    case _           => None
  }
}

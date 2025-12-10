package domain.booking

/** スロット状態（値オブジェクト）
  *
  * カレンダービューでの表示用ステータス。 ドメインの BookingStatus とは異なり、画面表示に特化した状態。
  */
sealed trait SlotStatus {
  def value: String
  def displayName: String
}

object SlotStatus {

  /** 空き
    */
  case object Empty extends SlotStatus {
    val value = "Empty"
    val displayName = "空"
  }

  /** 予約済み（未確定）
    */
  case object Reserved extends SlotStatus {
    val value = "Reserved"
    val displayName = "予約済み"
  }

  /** 予約確定
    */
  case object Confirmed extends SlotStatus {
    val value = "Confirmed"
    val displayName = "予約確定"
  }

  /** 使用中
    */
  case object InUse extends SlotStatus {
    val value = "InUse"
    val displayName = "使用中"
  }

  /** キャンセル済み
    */
  case object Cancelled extends SlotStatus {
    val value = "Cancelled"
    val displayName = "予約キャンセル"
  }

  /** 文字列から SlotStatus を取得
    *
    * @param value
    *   ステータス文字列
    * @return
    *   対応する SlotStatus またはNone
    */
  def fromString(value: String): Option[SlotStatus] = value match {
    case "Empty" | "空"           => Some(Empty)
    case "Reserved" | "予約済み"     => Some(Reserved)
    case "Confirmed" | "予約確定"    => Some(Confirmed)
    case "InUse" | "使用中"         => Some(InUse)
    case "Cancelled" | "予約キャンセル" => Some(Cancelled)
    case _                       => None
  }

  /** 表示名から SlotStatus を取得
    *
    * @param displayName
    *   表示名
    * @return
    *   対応する SlotStatus またはNone
    */
  def fromDisplayName(displayName: String): Option[SlotStatus] =
    displayName match {
      case "空"       => Some(Empty)
      case "予約済み"    => Some(Reserved)
      case "予約確定"    => Some(Confirmed)
      case "使用中"     => Some(InUse)
      case "予約キャンセル" => Some(Cancelled)
      case _         => None
    }
}

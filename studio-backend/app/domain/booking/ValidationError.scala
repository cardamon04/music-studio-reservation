package domain.booking

package domain.booking

/** バリデーションエラーの基底型
  */
sealed trait ValidationError {
  def message: String
}

/** 日付関連のエラー
  */
case class DateValidationError(message: String) extends ValidationError

/** 予約タイプ関連のエラー
  */
case class ReservationTypeValidationError(message: String)
    extends ValidationError

/** 学生関連のエラー
  */
case class StudentValidationError(message: String) extends ValidationError

/** 備品関連のエラー
  */
case class EquipmentValidationError(message: String) extends ValidationError

/** イベント名関連のエラー
  */
case class EventNameValidationError(message: String) extends ValidationError

/** 一般的なバリデーションエラー
  */
case class GeneralValidationError(message: String) extends ValidationError

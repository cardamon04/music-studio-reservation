package domain.reservationtype

import java.util.UUID

/** 予約種類ドメインエンティティ
  *
  * DDDのドメイン層として、予約種類のビジネスルールと状態を管理する
  */
case class ReservationType(
    reservationTypeId: ReservationTypeId,
    code: ReservationTypeCode,
    name: ReservationTypeName,
    description: Option[ReservationTypeDescription],
    isActive: Boolean,
    sortOrder: Int,
    createdAt: java.time.LocalDateTime,
    updatedAt: java.time.LocalDateTime
) {

  /** 予約種類が利用可能かどうか
    *
    * @return
    *   アクティブな場合true
    */
  def isAvailable: Boolean = isActive

  /** 予約種類を無効化
    *
    * @return
    *   無効化された予約種類
    */
  def deactivate(): ReservationType = {
    this.copy(
      isActive = false,
      updatedAt = java.time.LocalDateTime.now()
    )
  }

  /** 予約種類を有効化
    *
    * @return
    *   有効化された予約種類
    */
  def activate(): ReservationType = {
    this.copy(
      isActive = true,
      updatedAt = java.time.LocalDateTime.now()
    )
  }
}

/** 予約種類ID（値オブジェクト）
  */
case class ReservationTypeId(value: String) {
  override def toString: String = value
}

object ReservationTypeId {
  def generate(): ReservationTypeId = ReservationTypeId(
    UUID.randomUUID().toString
  )
}

/** 予約種類コード（値オブジェクト）
  */
case class ReservationTypeCode(value: String) {
  require(value.nonEmpty, "予約種類コードは空文字列にできません")
  require(value.length <= 50, "予約種類コードは50文字以内である必要があります")
  require(value.matches("^[A-Z][A-Za-z0-9]*$"), "予約種類コードは英数字で始まる必要があります")
  override def toString: String = value
}

/** 予約種類名（値オブジェクト）
  */
case class ReservationTypeName(value: String) {
  require(value.nonEmpty, "予約種類名は空文字列にできません")
  require(value.length <= 100, "予約種類名は100文字以内である必要があります")
  override def toString: String = value
}

/** 予約種類説明（値オブジェクト）
  */
case class ReservationTypeDescription(value: String) {
  require(value.length <= 500, "予約種類説明は500文字以内である必要があります")
  override def toString: String = value
}

object ReservationType {

  /** 文字列からReservationTypeを作成
    *
    * @param code
    *   予約種類コード
    * @return
    *   作成されたReservationTypeまたはNone
    */
  def fromString(code: String): Option[ReservationType] = {
    try {
      val reservationTypeCode = ReservationTypeCode(code)
      // 実際の実装では、リポジトリから取得する必要がある
      // ここでは簡易的な実装
      Some(
        ReservationType(
          reservationTypeId = ReservationTypeId.generate(),
          code = reservationTypeCode,
          name = ReservationTypeName(code),
          description = None,
          isActive = true,
          sortOrder = 1,
          createdAt = java.time.LocalDateTime.now(),
          updatedAt = java.time.LocalDateTime.now()
        )
      )
    } catch {
      case _: IllegalArgumentException => None
    }
  }
}

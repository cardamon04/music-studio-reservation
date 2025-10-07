package domain.reservationtype

import scala.concurrent.Future

/** 予約種類リポジトリインターフェース
  *
  * DDDのドメイン層として、予約種類の永続化に関するインターフェースを定義
  */
trait ReservationTypeRepository {

  /** 全てのアクティブな予約種類を取得
    *
    * @return
    *   アクティブな予約種類のリスト（ソート順）
    */
  def findAllActive(): Future[List[ReservationType]]

  /** 全ての予約種類を取得（管理者用）
    *
    * @return
    *   全ての予約種類のリスト（ソート順）
    */
  def findAll(): Future[List[ReservationType]]

  /** IDで予約種類を取得
    *
    * @param reservationTypeId
    *   予約種類ID
    * @return
    *   予約種類（存在しない場合はNone）
    */
  def findById(
      reservationTypeId: ReservationTypeId
  ): Future[Option[ReservationType]]

  /** コードで予約種類を取得
    *
    * @param code
    *   予約種類コード
    * @return
    *   予約種類（存在しない場合はNone）
    */
  def findByCode(code: ReservationTypeCode): Future[Option[ReservationType]]

  /** 予約種類を保存
    *
    * @param reservationType
    *   保存する予約種類
    * @return
    *   保存された予約種類
    */
  def save(reservationType: ReservationType): Future[ReservationType]

  /** 予約種類を削除
    *
    * @param reservationTypeId
    *   削除する予約種類のID
    * @return
    *   削除が成功した場合true
    */
  def delete(reservationTypeId: ReservationTypeId): Future[Boolean]
}

package domain.equipment

import scala.concurrent.Future

/** 備品リポジトリインターフェース
  *
  * DDDのドメイン層として、備品の永続化に関するインターフェースを定義
  */
trait EquipmentRepository {

  /** 全てのアクティブな備品を取得
    *
    * @return
    *   アクティブな備品のリスト
    */
  def findAllActive(): Future[List[Equipment]]

  /** 全ての備品を取得（管理者用）
    *
    * @return
    *   全ての備品のリスト
    */
  def findAll(): Future[List[Equipment]]

  /** IDで備品を取得
    *
    * @param equipmentId
    *   備品ID
    * @return
    *   備品（存在しない場合はNone）
    */
  def findById(equipmentId: EquipmentId): Future[Option[Equipment]]

  /** 備品を保存
    *
    * @param equipment
    *   保存する備品
    * @return
    *   保存された備品
    */
  def save(equipment: Equipment): Future[Equipment]

  /** 備品を削除
    *
    * @param equipmentId
    *   削除する備品のID
    * @return
    *   削除が成功した場合true
    */
  def delete(equipmentId: EquipmentId): Future[Boolean]

  /** 在庫数を更新
    *
    * @param equipmentId
    *   備品ID
    * @param newStock
    *   新しい在庫数
    * @return
    *   更新された備品
    */
  def updateStock(
      equipmentId: EquipmentId,
      newStock: Int
  ): Future[Option[Equipment]]
}

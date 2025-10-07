package domain.equipment

import java.util.UUID

/** 備品ドメインエンティティ
  *
  * DDDのドメイン層として、備品のビジネスルールと状態を管理する
  */
case class Equipment(
    equipmentId: EquipmentId,
    name: EquipmentName,
    stock: Stock,
    isActive: Boolean,
    createdAt: java.time.LocalDateTime,
    updatedAt: java.time.LocalDateTime
) {

  /** 在庫が利用可能かどうか
    *
    * @return
    *   在庫が1以上でアクティブな場合true
    */
  def isAvailable: Boolean = isActive && stock.value > 0

  /** 指定された数量が在庫内かどうか
    *
    * @param quantity
    *   確認したい数量
    * @return
    *   在庫内の場合true
    */
  def canRent(quantity: Int): Boolean = isActive && stock.value >= quantity

  /** 在庫を減らす
    *
    * @param quantity
    *   減らす数量
    * @return
    *   在庫を減らした備品
    */
  def reduceStock(quantity: Int): Equipment = {
    require(quantity > 0, "減らす数量は1以上である必要があります")
    require(
      canRent(quantity),
      s"在庫不足: 現在の在庫${stock.value}個に対して${quantity}個を借りることはできません"
    )

    this.copy(
      stock = Stock(stock.value - quantity),
      updatedAt = java.time.LocalDateTime.now()
    )
  }

  /** 在庫を増やす
    *
    * @param quantity
    *   増やす数量
    * @return
    *   在庫を増やした備品
    */
  def addStock(quantity: Int): Equipment = {
    require(quantity > 0, "増やす数量は1以上である必要があります")

    this.copy(
      stock = Stock(stock.value + quantity),
      updatedAt = java.time.LocalDateTime.now()
    )
  }

  /** 備品を無効化
    *
    * @return
    *   無効化された備品
    */
  def deactivate(): Equipment = {
    this.copy(
      isActive = false,
      updatedAt = java.time.LocalDateTime.now()
    )
  }

  /** 備品を有効化
    *
    * @return
    *   有効化された備品
    */
  def activate(): Equipment = {
    this.copy(
      isActive = true,
      updatedAt = java.time.LocalDateTime.now()
    )
  }
}

/** 備品名（値オブジェクト）
  */
case class EquipmentName(value: String) {
  require(value.nonEmpty, "備品名は空文字列にできません")
  require(value.length <= 100, "備品名は100文字以内である必要があります")
  override def toString: String = value
}

/** 在庫数（値オブジェクト）
  */
case class Stock(value: Int) {
  require(value >= 0, "在庫数は0以上である必要があります")
  override def toString: String = value.toString
}

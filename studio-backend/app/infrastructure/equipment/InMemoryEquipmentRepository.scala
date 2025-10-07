package infrastructure.equipment

import domain.equipment.{
  Equipment,
  EquipmentId,
  EquipmentName,
  Stock,
  EquipmentRepository
}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.concurrent.TrieMap

/** InMemory備品リポジトリ実装
  *
  * DDDのインフラストラクチャ層として、備品のInMemory永続化を実装
  */
@Singleton
class InMemoryEquipmentRepository @Inject() ()(implicit ec: ExecutionContext)
    extends EquipmentRepository {

  // スレッドセーフなマップで備品を管理
  private val equipmentMap = TrieMap[EquipmentId, Equipment]()

  // 初期データを設定
  initializeDefaultEquipment()

  override def findAllActive(): Future[List[Equipment]] = {
    Future {
      equipmentMap.values.filter(_.isActive).toList.sortBy(_.name.value)
    }
  }

  override def findAll(): Future[List[Equipment]] = {
    Future {
      equipmentMap.values.toList.sortBy(_.name.value)
    }
  }

  override def findById(equipmentId: EquipmentId): Future[Option[Equipment]] = {
    Future {
      equipmentMap.get(equipmentId)
    }
  }

  override def save(equipment: Equipment): Future[Equipment] = {
    Future {
      equipmentMap.put(equipment.equipmentId, equipment)
      equipment
    }
  }

  override def delete(equipmentId: EquipmentId): Future[Boolean] = {
    Future {
      equipmentMap.remove(equipmentId).isDefined
    }
  }

  override def updateStock(
      equipmentId: EquipmentId,
      newStock: Int
  ): Future[Option[Equipment]] = {
    Future {
      equipmentMap.get(equipmentId).map { equipment =>
        val updatedEquipment = equipment.copy(
          stock = domain.equipment.Stock(newStock),
          updatedAt = java.time.LocalDateTime.now()
        )
        equipmentMap.put(equipmentId, updatedEquipment)
        updatedEquipment
      }
    }
  }

  /** デフォルトの備品データを初期化
    */
  private def initializeDefaultEquipment(): Unit = {
    val now = java.time.LocalDateTime.now()

    val defaultEquipment = List(
      Equipment(
        equipmentId = EquipmentId("mic1"),
        name = EquipmentName("マイク"),
        stock = Stock(5),
        isActive = true,
        createdAt = now,
        updatedAt = now
      ),
      Equipment(
        equipmentId = EquipmentId("cable1"),
        name = EquipmentName("マイクケーブル"),
        stock = Stock(10),
        isActive = true,
        createdAt = now,
        updatedAt = now
      )
    )

    defaultEquipment.foreach { equipment =>
      equipmentMap.put(equipment.equipmentId, equipment)
    }
  }
}

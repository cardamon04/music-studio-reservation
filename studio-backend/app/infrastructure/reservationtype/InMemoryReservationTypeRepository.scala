package infrastructure.reservationtype

import domain.reservationtype.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.concurrent.TrieMap

/** InMemory予約種類リポジトリ実装
  *
  * DDDのインフラストラクチャ層として、予約種類のInMemory永続化を実装
  */
@Singleton
class InMemoryReservationTypeRepository @Inject() ()(implicit
    ec: ExecutionContext
) extends ReservationTypeRepository {

  // スレッドセーフなマップで予約種類を管理
  private val reservationTypeMap = TrieMap[ReservationTypeId, ReservationType]()
  private val codeIndex = TrieMap[ReservationTypeCode, ReservationTypeId]()

  // 初期データを設定
  initializeDefaultReservationTypes()

  override def findAllActive(): Future[List[ReservationType]] = {
    Future {
      reservationTypeMap.values
        .filter(_.isActive)
        .toList
        .sortBy(_.sortOrder)
    }
  }

  override def findAll(): Future[List[ReservationType]] = {
    Future {
      reservationTypeMap.values.toList
        .sortBy(_.sortOrder)
    }
  }

  override def findById(
      reservationTypeId: ReservationTypeId
  ): Future[Option[ReservationType]] = {
    Future {
      reservationTypeMap.get(reservationTypeId)
    }
  }

  override def findByCode(
      code: ReservationTypeCode
  ): Future[Option[ReservationType]] = {
    Future {
      codeIndex.get(code).flatMap(reservationTypeMap.get)
    }
  }

  override def save(
      reservationType: ReservationType
  ): Future[ReservationType] = {
    Future {
      reservationTypeMap.put(reservationType.reservationTypeId, reservationType)
      codeIndex.put(reservationType.code, reservationType.reservationTypeId)
      reservationType
    }
  }

  override def delete(reservationTypeId: ReservationTypeId): Future[Boolean] = {
    Future {
      reservationTypeMap
        .get(reservationTypeId)
        .map { reservationType =>
          codeIndex.remove(reservationType.code)
          reservationTypeMap.remove(reservationTypeId).isDefined
        }
        .getOrElse(false)
    }
  }

  /** デフォルトの予約種類データを初期化
    */
  private def initializeDefaultReservationTypes(): Unit = {
    val now = java.time.LocalDateTime.now()

    val defaultReservationTypes = List(
      ReservationType(
        reservationTypeId = ReservationTypeId("rt1"),
        code = ReservationTypeCode("StudentRental"),
        name = ReservationTypeName("学生レンタル"),
        description =
          Some(ReservationTypeDescription("学生が個人でスタジオを利用する場合の予約種類")),
        isActive = true,
        sortOrder = 1,
        createdAt = now,
        updatedAt = now
      ),
      ReservationType(
        reservationTypeId = ReservationTypeId("rt2"),
        code = ReservationTypeCode("ClassRental"),
        name = ReservationTypeName("授業レンタル"),
        description = Some(ReservationTypeDescription("授業でスタジオを利用する場合の予約種類")),
        isActive = true,
        sortOrder = 2,
        createdAt = now,
        updatedAt = now
      ),
      ReservationType(
        reservationTypeId = ReservationTypeId("rt3"),
        code = ReservationTypeCode("EventReservation"),
        name = ReservationTypeName("イベント予約"),
        description =
          Some(ReservationTypeDescription("イベントやコンサートでスタジオを利用する場合の予約種類")),
        isActive = true,
        sortOrder = 3,
        createdAt = now,
        updatedAt = now
      )
    )

    defaultReservationTypes.foreach { reservationType =>
      reservationTypeMap.put(reservationType.reservationTypeId, reservationType)
      codeIndex.put(reservationType.code, reservationType.reservationTypeId)
    }
  }
}

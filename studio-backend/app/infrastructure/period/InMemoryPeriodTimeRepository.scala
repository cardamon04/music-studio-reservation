package infrastructure.period

import domain.period.{PeriodId, PeriodTime, PeriodTimeRepository}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.MessagesApi
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InMemoryPeriodTimeRepository @Inject() (
    messagesApi: MessagesApi
)(implicit ec: ExecutionContext)
    extends PeriodTimeRepository {

  private val logger = Logger(this.getClass)
  private val periodTimeMap = TrieMap[PeriodId, PeriodTime]()

  initializeDefaultPeriodTimes()

  override def findAll(): Future[List[PeriodTime]] = Future {
    implicit val messages = messagesApi.preferred(Seq.empty)
    logger.info(s"[I025] ${messages("I025", "findAll")}")
    val result = periodTimeMap.values.toList.sortBy(_.periodId.value)
    logger.info(s"[I026] ${messages("I026", result.length)}")
    logger.info(s"[I027] ${messages("I027", "findAll")}")
    result
  }

  override def findByPeriodId(periodId: PeriodId): Future[Option[PeriodTime]] =
    Future {
      implicit val messages = messagesApi.preferred(Seq.empty)
      logger.info(
        s"[I025] ${messages("I025", s"findByPeriodId(${periodId.value})")}"
      )
      val result = periodTimeMap.get(periodId)
      if (result.isDefined) {
        logger.info(
          s"[I026] ${messages("I026", s"${periodId.value} の期間時刻を取得")}"
        )
      } else {
        logger.warn(s"期間時刻が見つかりません: ${periodId.value}")
      }
      logger.info(
        s"[I027] ${messages("I027", s"findByPeriodId(${periodId.value})")}"
      )
      result
    }

  override def save(periodTime: PeriodTime): Future[PeriodTime] = Future {
    periodTimeMap.put(periodTime.periodId, periodTime)
    periodTime
  }

  override def delete(periodId: PeriodId): Future[Boolean] = Future {
    periodTimeMap.remove(periodId).isDefined
  }

  private def initializeDefaultPeriodTimes(): Unit = {
    implicit val messages = messagesApi.preferred(Seq.empty)
    logger.info(s"[I028] ${messages("I028")}")
    val defaultPeriodTimes = List(
      PeriodTime(
        periodId = PeriodId("P1"),
        startTime = java.time.LocalTime.of(9, 0),
        endTime = java.time.LocalTime.of(10, 30)
      ),
      PeriodTime(
        periodId = PeriodId("P2"),
        startTime = java.time.LocalTime.of(10, 30),
        endTime = java.time.LocalTime.of(12, 0)
      ),
      PeriodTime(
        periodId = PeriodId("P3"),
        startTime = java.time.LocalTime.of(13, 0),
        endTime = java.time.LocalTime.of(14, 30)
      ),
      PeriodTime(
        periodId = PeriodId("P4"),
        startTime = java.time.LocalTime.of(14, 30),
        endTime = java.time.LocalTime.of(16, 0)
      ),
      PeriodTime(
        periodId = PeriodId("P5"),
        startTime = java.time.LocalTime.of(16, 0),
        endTime = java.time.LocalTime.of(17, 30)
      ),
      PeriodTime(
        periodId = PeriodId("P6"),
        startTime = java.time.LocalTime.of(17, 30),
        endTime = java.time.LocalTime.of(19, 0)
      )
    )

    defaultPeriodTimes.foreach { periodTime =>
      periodTimeMap.put(periodTime.periodId, periodTime)
      logger.info(
        s"[I029] ${messages("I029", periodTime.periodId.value, periodTime.startTime.toString, periodTime.endTime.toString)}"
      )
    }
    logger.info(s"[I030] ${messages("I030", defaultPeriodTimes.length)}")
  }
}

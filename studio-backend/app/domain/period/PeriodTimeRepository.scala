package domain.period

import scala.concurrent.Future

trait PeriodTimeRepository {
  def findAll(): Future[List[PeriodTime]]
  def findByPeriodId(periodId: PeriodId): Future[Option[PeriodTime]]
  def save(periodTime: PeriodTime): Future[PeriodTime]
  def delete(periodId: PeriodId): Future[Boolean]
}

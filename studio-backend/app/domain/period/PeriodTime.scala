package domain.period

import java.time.LocalTime

case class PeriodTime(
    periodId: PeriodId,
    startTime: LocalTime,
    endTime: LocalTime
) {
  require(startTime.isBefore(endTime), "開始時刻は終了時刻より前である必要があります")
}

case class PeriodId(value: String) {
  require(value.nonEmpty, "期間IDは必須です")
  require(value.matches("^P[1-9][0-9]*$"), "期間IDはP1, P2, P3...の形式である必要があります")
}

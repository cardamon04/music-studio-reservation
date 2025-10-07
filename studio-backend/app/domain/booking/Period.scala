package domain.booking

/** 期間（値オブジェクト）
  *
  * 成瀬允宣氏のDDD定義に従った値オブジェクト 開始時間と終了時間を持つ不変な期間を表現
  */
case class Period(
    startTime: java.time.LocalTime,
    endTime: java.time.LocalTime
) {
  require(startTime.isBefore(endTime), "開始時間は終了時間より前である必要があります")
  require(!startTime.equals(endTime), "開始時間と終了時間は同じであってはいけません")

  /** 期間の長さ（分）
    */
  def durationInMinutes: Int = {
    java.time.Duration.between(startTime, endTime).toMinutes.toInt
  }

  /** 指定された時間がこの期間内かどうか
    */
  def contains(time: java.time.LocalTime): Boolean = {
    !time.isBefore(startTime) && time.isBefore(endTime)
  }

  /** 他の期間と重複するかどうか
    */
  def overlaps(other: Period): Boolean = {
    this.startTime.isBefore(other.endTime) && other.startTime.isBefore(
      this.endTime
    )
  }

  override def equals(obj: Any): Boolean = obj match {
    case other: Period =>
      this.startTime == other.startTime && this.endTime == other.endTime
    case _ => false
  }

  override def hashCode(): Int = startTime.hashCode + endTime.hashCode

  override def toString: String = s"${startTime} - ${endTime}"
}

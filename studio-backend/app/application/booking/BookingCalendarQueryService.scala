package application.booking

import java.time.LocalDate
import scala.concurrent.Future
import domain.booking.StudioId

/** 予約カレンダークエリサービス
  *
  * カレンダー表示用のクエリを提供する。
  */
trait BookingCalendarQueryService {

  /** 指定日のカレンダーを取得
    *
    * @param date
    *   対象日付
    * @param studioId
    *   特定のスタジオに絞り込む場合に指定（Noneの場合は全スタジオ）
    * @return
    *   カレンダービュー
    */
  def getByDate(
      date: LocalDate,
      studioId: Option[StudioId]
  ): Future[BookingCalendarView]
}

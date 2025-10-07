error id: file:///C:/repo/Scala/music-studio-reservation/studio-backend/app/controllers/booking/BookingCalendarController.scala:`<none>`.
file:///C:/repo/Scala/music-studio-reservation/studio-backend/app/controllers/booking/BookingCalendarController.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 716
uri: file:///C:/repo/Scala/music-studio-reservation/studio-backend/app/controllers/booking/BookingCalendarController.scala
text:
```scala
package controllers.booking

@Singleton
class BookingCalendarController @Inject()(
    controllerComponents: ControllerComponents
    queryService: BookingControllerQueryService
)(using executionContext: ExecutionContext) extends AbstractController(controllerComponents) {

/**
  * 日付とスタジオIDを指定して予約カレンダーを取得する。
  *
  * @param date 指定された日付
  * @param studioId 指定されたスタジオID
  */
  def getByDate(rawDate: String, studioId: Option[String]): Action[AnyContent] = Action[AnyContent] = Action.async {
    //val date = LocalDate.parse(rawDate)
    for {
        date <- LocalDate.parse(rawDate)
        calendar <- queryService.getByDate(date, studioId)
    } yield {
        Ok(play.api.libs.json.Json.toJ@@son(calendar)(JsonFormatters.bookingCalendarViewWrites))
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.
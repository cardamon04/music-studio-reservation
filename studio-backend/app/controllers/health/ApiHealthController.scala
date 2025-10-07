package controllers.health

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._

class ApiHealthController @Inject() (cc: ControllerComponents)
    extends AbstractController(cc) {

  def ping(): Action[AnyContent] = Action {
    val json: JsObject = Json.obj(
      "status" -> "ok",
      "message" -> "pong"
    )
    Ok(json)
  }
}

package controllers.equipment

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import domain.equipment.{Equipment, EquipmentId, EquipmentRepository}
import utils.LoggerUtil

/** 備品管理を行うコントローラー
  * 
  * DDDのアプリケーション層として、備品の取得・管理操作を行う
  */
@Singleton
class EquipmentController @Inject() (
    controllerComponents: ControllerComponents,
    loggerUtil: LoggerUtil,
    equipmentRepository: EquipmentRepository
)(using executionContext: ExecutionContext)
    extends AbstractController(controllerComponents) {

  /** 備品レスポンスのJSONフォーマット
    */
  case class EquipmentResponse(
      id: String,
      name: String,
      stock: Int,
      isActive: Boolean
  )

  implicit val equipmentResponseWrites: Writes[EquipmentResponse] = Json.writes[EquipmentResponse]

  /** 全てのアクティブな備品を取得
    * 
    * GET /api/equipment
    */
  def getAllActive(): Action[AnyContent] = Action.async { request =>
    given ec: ExecutionContext = executionContext

    loggerUtil.info("I013", "備品一覧取得リクエスト", request)

    equipmentRepository.findAllActive().map { equipmentList =>
      val response = equipmentList.map { equipment =>
        EquipmentResponse(
          id = equipment.equipmentId.value,
          name = equipment.name.value,
          stock = equipment.stock.value,
          isActive = equipment.isActive
        )
      }

      loggerUtil.info("I014", s"備品一覧取得成功: ${response.length}件", request)
      Ok(Json.toJson(response)).as("application/json; charset=utf-8")
    }.recover {
      case ex: Exception =>
        loggerUtil.error("E011", s"備品一覧取得エラー: ${ex.getMessage}", request, Some(ex))
        InternalServerError(Json.obj("error" -> "備品一覧の取得に失敗しました"))
    }
  }

  /** IDで備品を取得
    * 
    * GET /api/equipment/:id
    */
  def getById(id: String): Action[AnyContent] = Action.async { request =>
    given ec: ExecutionContext = executionContext

    loggerUtil.info("I015", s"備品詳細取得リクエスト: ${id}", request)

    val equipmentId = EquipmentId(id)
    equipmentRepository.findById(equipmentId).map {
      case Some(equipment) =>
        val response = EquipmentResponse(
          id = equipment.equipmentId.value,
          name = equipment.name.value,
          stock = equipment.stock.value,
          isActive = equipment.isActive
        )
        loggerUtil.info("I016", s"備品詳細取得成功: ${equipment.name.value}", request)
        Ok(Json.toJson(response)).as("application/json; charset=utf-8")
      case None =>
        loggerUtil.warn("W011", s"備品が見つかりません: ${id}", request)
        NotFound(Json.obj("error" -> "指定された備品が見つかりません"))
    }.recover {
      case ex: Exception =>
        loggerUtil.error("E012", s"備品詳細取得エラー: ${ex.getMessage}", request, Some(ex))
        InternalServerError(Json.obj("error" -> "備品詳細の取得に失敗しました"))
    }
  }
}

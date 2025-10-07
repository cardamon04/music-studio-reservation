package controllers.student

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import domain.student.{Student, StudentNumber, StudentRepository}
import utils.LoggerUtil

/** 学生管理を行うコントローラー
  * 
  * DDDのアプリケーション層として、学生の取得・検索操作を行う
  */
@Singleton
class StudentController @Inject() (
    controllerComponents: ControllerComponents,
    loggerUtil: LoggerUtil,
    studentRepository: StudentRepository
)(using executionContext: ExecutionContext)
    extends AbstractController(controllerComponents) {

  /** 学生レスポンスのJSONフォーマット
    */
  case class StudentResponse(
      studentNumber: String,
      name: String,
      email: Option[String]
  )

  implicit val studentResponseWrites: Writes[StudentResponse] = Json.writes[StudentResponse]

  /** 全てのアクティブな学生を取得
    * 
    * GET /api/students
    */
  def getAllActive(): Action[AnyContent] = Action.async { request =>
    given ec: ExecutionContext = executionContext

    loggerUtil.info("I020", "学生一覧取得リクエスト", request)

    studentRepository.findAllActive().map { studentList =>
      val response = studentList.map { student =>
        StudentResponse(
          studentNumber = student.studentNumber.value,
          name = student.name.value,
          email = student.email.map(_.value)
        )
      }

      loggerUtil.info("I021", s"学生一覧取得成功: ${response.length}件", request)
      Ok(Json.toJson(response)).as("application/json; charset=utf-8")
    }.recover {
      case ex: Exception =>
        loggerUtil.error("E020", s"学生一覧取得エラー: ${ex.getMessage}", request, Some(ex))
        InternalServerError(Json.obj("error" -> "学生一覧の取得に失敗しました"))
    }
  }

  /** 学生名で検索
    * 
    * GET /api/students/search?name={name}
    */
  def searchByName(name: String): Action[AnyContent] = Action.async { request =>
    given ec: ExecutionContext = executionContext

    loggerUtil.info("I022", s"学生検索リクエスト: ${name}", request)

    studentRepository.searchByName(name).map { studentList =>
      val response = studentList.map { student =>
        StudentResponse(
          studentNumber = student.studentNumber.value,
          name = student.name.value,
          email = student.email.map(_.value)
        )
      }

      loggerUtil.info("I023", s"学生検索成功: ${response.length}件", request)
      Ok(Json.toJson(response)).as("application/json; charset=utf-8")
    }.recover {
      case ex: Exception =>
        loggerUtil.error("E021", s"学生検索エラー: ${ex.getMessage}", request, Some(ex))
        InternalServerError(Json.obj("error" -> "学生検索に失敗しました"))
    }
  }

  /** 学生番号で学生を取得
    * 
    * GET /api/students/{studentNumber}
    */
  def getByStudentNumber(studentNumber: String): Action[AnyContent] = Action.async { request =>
    given ec: ExecutionContext = executionContext

    loggerUtil.info("I024", s"学生詳細取得リクエスト: ${studentNumber}", request)

    val studentNumberObj = StudentNumber(studentNumber)
    studentRepository.findByStudentNumber(studentNumberObj).map {
      case Some(student) =>
        val response = StudentResponse(
          studentNumber = student.studentNumber.value,
          name = student.name.value,
          email = student.email.map(_.value)
        )
        loggerUtil.info("I025", s"学生詳細取得成功: ${student.name.value}", request)
        Ok(Json.toJson(response)).as("application/json; charset=utf-8")
      case None =>
        loggerUtil.warn("W010", s"学生が見つかりません: ${studentNumber}", request)
        NotFound(Json.obj("error" -> "指定された学生が見つかりません"))
    }.recover {
      case ex: Exception =>
        loggerUtil.error("E022", s"学生詳細取得エラー: ${ex.getMessage}", request, Some(ex))
        InternalServerError(Json.obj("error" -> "学生詳細の取得に失敗しました"))
    }
  }
}

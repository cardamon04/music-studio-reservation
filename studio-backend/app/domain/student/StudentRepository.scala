package domain.student

import scala.concurrent.Future

/** 学生リポジトリインターフェース
  *
  * DDDのドメイン層として、学生の永続化に関するインターフェースを定義
  */
trait StudentRepository {

  /** 全てのアクティブな学生を取得
    *
    * @return
    *   アクティブな学生のリスト
    */
  def findAllActive(): Future[List[Student]]

  /** 全ての学生を取得
    *
    * @return
    *   全学生のリスト
    */
  def findAll(): Future[List[Student]]

  /** 学生番号で学生を取得
    *
    * @param studentNumber
    *   学生番号
    * @return
    *   学生（存在しない場合はNone）
    */
  def findByStudentNumber(studentNumber: StudentNumber): Future[Option[Student]]

  /** 学生名で検索
    *
    * @param name
    *   検索する学生名（部分一致）
    * @return
    *   該当する学生のリスト
    */
  def searchByName(name: String): Future[List[Student]]

  /** 学生を保存
    *
    * @param student
    *   保存する学生
    * @return
    *   保存された学生
    */
  def save(student: Student): Future[Student]

  /** 学生を削除
    *
    * @param studentNumber
    *   削除する学生の学生番号
    * @return
    *   削除成功の場合true
    */
  def delete(studentNumber: StudentNumber): Future[Boolean]
}

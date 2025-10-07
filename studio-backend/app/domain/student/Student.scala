package domain.student

import java.time.LocalDateTime

/** 学生ドメインエンティティ
  *
  * DDDのドメイン層として、学生のビジネスルールと状態を管理する
  */
case class Student(
    studentNumber: StudentNumber,
    name: StudentName,
    email: Option[StudentEmail],
    isActive: Boolean,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) {

  /** 学生が有効かどうか
    *
    * @return
    *   有効な学生の場合true
    */
  def isValid: Boolean = isActive
}

/** 学生名値オブジェクト
  */
case class StudentName(value: String) {
  require(value.nonEmpty, "学生名は必須です")
  require(value.length <= 50, "学生名は50文字以内である必要があります")
}

/** 学生メールアドレス値オブジェクト
  */
case class StudentEmail(value: String) {
  require(value.nonEmpty, "メールアドレスは必須です")
  require(value.contains("@"), "メールアドレスの形式が正しくありません")
}

package domain.student

/** 学生番号（値オブジェクト）
  *
  * 成瀬允宣氏のDDD定義に従った値オブジェクト
  */
case class StudentNumber(value: String) {
  require(value.nonEmpty, "学生番号は空文字列にできません")
  require(value.matches("^[A-Za-z0-9]{10}$"), "学生番号は英数字10文字である必要があります")

  override def toString: String = value

  override def equals(obj: Any): Boolean = obj match {
    case other: StudentNumber => this.value == other.value
    case _                    => false
  }

  override def hashCode(): Int = value.hashCode
}

object StudentNumber {

  /** 文字列からStudentNumberを作成
    *
    * @param value
    *   学生番号文字列
    * @return
    *   作成されたStudentNumberまたはNone
    */
  def fromString(value: String): Option[StudentNumber] = {
    try {
      Some(StudentNumber(value))
    } catch {
      case _: IllegalArgumentException => None
    }
  }
}

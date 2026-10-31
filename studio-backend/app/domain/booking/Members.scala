package domain.booking

import domain.student.StudentNumber

/** 学生予約に紐づく1名以上のメンバー。集合の表現を内部に閉じ込める。 */
final case class Members private (private val studentNumbers: Set[StudentNumber]) {
  def contains(studentNumber: StudentNumber): Boolean = studentNumbers.contains(studentNumber)

  def size: Int = studentNumbers.size
}

object Members {
  def from(studentNumbers: Set[StudentNumber]): Either[StudentValidationError, Members] =
    if (studentNumbers.isEmpty)
      Left(StudentValidationError("学生予約のメンバーは1名以上必要です"))
    else Right(new Members(studentNumbers))
}

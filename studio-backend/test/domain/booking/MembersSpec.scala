package domain.booking

import domain.student.StudentNumber
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.EitherValues

class MembersSpec extends AnyFreeSpec with Matchers with EitherValues {
  private val studentA = StudentNumber("AB12345678")
  private val studentB = StudentNumber("CD12345678")

  "予約メンバー" - {
    "1名の予約でも所属する学生と所属しない学生を区別する" in {
      val members = Members.from(Set(studentA)).value

      members.contains(studentA) mustBe true
      members.contains(studentB) mustBe false
    }

    "0名の学生予約をメンバーとして受け付けない" in {
      val result = Members.from(Set.empty)

      result mustBe Left(StudentValidationError("学生予約のメンバーは1名以上必要です"))
    }

    "2名の予約では両方の学生が所属し人数は2名になる" in {
      val members = Members.from(Set(studentA, studentB)).value

      members.contains(studentA) mustBe true
      members.contains(studentB) mustBe true
      members.size mustBe 2
    }

    "同じ学生からなるメンバーは順序に関係なく同じ値になる" in {
      val first = Members.from(Set(studentA, studentB)).value
      val reordered = Members.from(Set(studentB, studentA)).value

      first mustBe reordered
      first.hashCode mustBe reordered.hashCode
    }

    "外部から空のメンバーを直接生成・コピーできず集合のアクセサも公開しない" in {
      val members = Members.from(Set(studentA)).value

      assertCompiles("members.contains(studentA)")
      assertDoesNotCompile("new Members(Set.empty)")
      assertDoesNotCompile("Members(Set.empty)")
      assertDoesNotCompile("members.copy(studentNumbers = Set.empty)")
      assertDoesNotCompile("members.studentNumbers")
    }
  }
}

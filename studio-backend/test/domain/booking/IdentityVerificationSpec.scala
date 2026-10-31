package domain.booking

import domain.student.{StudentNumber, TAId}
import java.time.Instant
import java.util.UUID
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers

class IdentityVerificationSpec extends AnyFreeSpec with Matchers with EitherValues {
  private val alice = StudentNumber("AB12345678")
  private val bob = StudentNumber("CD12345678")
  private val members = Members.from(Set(alice, bob)).value
  private val ta = TAId(UUID.fromString("550e8400-e29b-41d4-a716-446655440201"))
  private val at = Instant.parse("2026-10-10T00:55:00Z")

  "本人確認" - {
    "一部のメンバーだけ確認した結果と確認者・日時を記録する" in {
      val result = IdentityVerification.record(members, List(alice), ta, at).value

      result.isVerified(alice) mustBe true
      result.isVerified(bob) mustBe false
      result.verifiedBy mustBe ta
      result.verifiedAt mustBe at
      result.allMembersVerified mustBe false
    }

    "確認順序に関係なく全メンバーの確認がそろえば全員確認済みとする" in {
      val result = IdentityVerification.record(members, List(bob, alice), ta, at).value

      result.allMembersVerified mustBe true
    }

    "人数が一致していても予約メンバー以外の確認は拒否する" in {
      val outsider = StudentNumber("EF12345678")

      val result = IdentityVerification.record(members, List(alice, outsider), ta, at)

      result mustBe Left(StudentValidationError("予約メンバー以外は本人確認できません"))
    }

    "同じ学生を二重に確認した入力は重複を除かず拒否する" in {
      val result = IdentityVerification.record(members, List(alice, alice), ta, at)

      result mustBe Left(StudentValidationError("確認対象の学生番号は重複できません"))
    }

    "確認一覧・確認者・日時を全体置換し元の記録は変えない" in {
      val original = IdentityVerification.record(members, List(alice, bob), ta, at).value
      val nextTa = TAId(UUID.fromString("550e8400-e29b-41d4-a716-446655440202"))
      val nextTime = at.plusSeconds(60)
      val replaced = original.replace(List(bob), nextTa, nextTime).value

      replaced.isVerified(alice) mustBe false
      replaced.isVerified(bob) mustBe true
      replaced.allMembersVerified mustBe false
      replaced.verifiedBy mustBe nextTa
      replaced.verifiedAt mustBe nextTime
      original.isVerified(alice) mustBe true
      original.isVerified(bob) mustBe true
      original.allMembersVerified mustBe true
      original.verifiedBy mustBe ta
      original.verifiedAt mustBe at
    }

    "生成とコピーで検証を迂回できない" in {
      val original = IdentityVerification.record(members, List(alice), ta, at).value

      assertCompiles("original.allMembersVerified")
      assertCompiles("original.isVerified(alice)")
      assertDoesNotCompile("original.verifiedMemberNumbers")
      assertDoesNotCompile("original.copy(verifiedBy = ta)")
      assertDoesNotCompile("IdentityVerification(members, Set(alice), ta, at)")
      assertDoesNotCompile("new IdentityVerification(members, Set(alice), ta, at)")
    }

    "確認済み一覧を空に置き換えると全員確認済みではなくなる" in {
      val original = IdentityVerification.record(members, List(alice, bob), ta, at).value
      val cleared = original.replace(Nil, ta, at.plusSeconds(60)).value

      cleared.isVerified(alice) mustBe false
      cleared.isVerified(bob) mustBe false
      cleared.allMembersVerified mustBe false
      original.allMembersVerified mustBe true
    }

    "予約外の学生への置換を拒否し元の確認結果を保つ" in {
      val original = IdentityVerification.record(members, List(alice), ta, at).value
      val outsider = StudentNumber("EF12345678")

      val result = original.replace(List(outsider), ta, at.plusSeconds(60))

      result mustBe Left(StudentValidationError("予約メンバー以外は本人確認できません"))
      original.isVerified(alice) mustBe true
      original.isVerified(bob) mustBe false
      original.verifiedAt mustBe at
    }

    "重複した確認一覧への置換を拒否し元の確認結果を保つ" in {
      val original = IdentityVerification.record(members, List(alice), ta, at).value

      val result = original.replace(List(bob, bob), ta, at.plusSeconds(60))

      result mustBe Left(StudentValidationError("確認対象の学生番号は重複できません"))
      original.isVerified(alice) mustBe true
      original.isVerified(bob) mustBe false
      original.verifiedAt mustBe at
    }

    "対象・確認結果・確認者・日時が同じなら同じ値として扱う" in {
      val first = IdentityVerification.record(members, List(alice, bob), ta, at).value
      val reordered = IdentityVerification.record(members, List(bob, alice), ta, at).value
      val later = first.replace(List(alice, bob), ta, at.plusSeconds(60)).value

      first mustBe reordered
      first.hashCode mustBe reordered.hashCode
      first must not be later
    }
  }
}

package domain.booking

import domain.student.{StudentNumber, TAId}
import java.time.Instant
import java.util.UUID
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers

class IdentityVerificationSpec extends AnyWordSpec with Matchers {
  private val alice = StudentNumber("AB12345678")
  private val bob = StudentNumber("CD12345678")
  private val members = Set(alice, bob)
  private val ta = TAId(UUID.fromString("550e8400-e29b-41d4-a716-446655440201"))
  private val at = Instant.parse("2026-10-10T00:55:00Z")

  "Identity verification" should {
    "retain a partial confirmation with its actor and time" in {
      val result = IdentityVerification.record(members, List(alice), ta, at).toOption.get

      result.verifiedMemberNumbers mustBe Set(alice)
      result.verifiedBy mustBe ta
      result.verifiedAt mustBe at
      result.allMembersVerified mustBe false
    }

    "derive full confirmation from the whole membership regardless of order" in {
      val result = IdentityVerification.record(members, List(bob, alice), ta, at).toOption.get

      result.allMembersVerified mustBe true
    }

    "reject a student outside the booking even when the count matches" in {
      val outsider = StudentNumber("EF12345678")

      IdentityVerification.record(members, List(alice, outsider), ta, at).isLeft mustBe true
    }

    "reject duplicate confirmation entries instead of silently removing duplicates" in {
      IdentityVerification.record(members, List(alice, alice), ta, at).isLeft mustBe true
    }

    "reject an empty booking membership rather than treating it as fully verified" in {
      IdentityVerification.record(Set.empty, Nil, ta, at).isLeft mustBe true
    }

    "replace the whole confirmation and its metadata without changing the prior value" in {
      val original = IdentityVerification.record(members, List(alice, bob), ta, at).toOption.get
      val nextTa = TAId(UUID.fromString("550e8400-e29b-41d4-a716-446655440202"))
      val nextTime = at.plusSeconds(60)
      val replaced = original.replace(List(bob), nextTa, nextTime).toOption.get

      replaced.verifiedMemberNumbers mustBe Set(bob)
      replaced.allMembersVerified mustBe false
      replaced.verifiedBy mustBe nextTa
      replaced.verifiedAt mustBe nextTime
      original.verifiedMemberNumbers mustBe members
      original.allMembersVerified mustBe true
      original.verifiedBy mustBe ta
      original.verifiedAt mustBe at
    }

    "keep construction and copying behind the validated factory" in {
      val original = IdentityVerification.record(members, List(alice), ta, at).toOption.get

      assertCompiles("original.allMembersVerified")
      assertDoesNotCompile("original.copy(members = Set.empty)")
      assertDoesNotCompile("IdentityVerification(members, Set(alice), ta, at)")
      assertDoesNotCompile("new IdentityVerification(members, Set(alice), ta, at)")
    }

    "allow an empty confirmation list and clear a previous full confirmation" in {
      val original = IdentityVerification.record(members, List(alice, bob), ta, at).toOption.get
      val cleared = original.replace(Nil, ta, at.plusSeconds(60)).toOption.get

      cleared.verifiedMemberNumbers mustBe Set.empty
      cleared.allMembersVerified mustBe false
      original.allMembersVerified mustBe true
    }

    "reject invalid replacements without changing the previous confirmation" in {
      val original = IdentityVerification.record(members, List(alice), ta, at).toOption.get

      original.replace(List(StudentNumber("EF12345678")), ta, at).isLeft mustBe true
      original.replace(List(bob, bob), ta, at).isLeft mustBe true
      original.verifiedMemberNumbers mustBe Set(alice)
      original.verifiedBy mustBe ta
      original.verifiedAt mustBe at
    }

    "compare verification values by their membership, confirmation and metadata" in {
      val first = IdentityVerification.record(members, List(alice, bob), ta, at).toOption.get
      val reordered = IdentityVerification.record(members, List(bob, alice), ta, at).toOption.get
      val later = first.replace(List(alice, bob), ta, at.plusSeconds(60)).toOption.get

      first mustBe reordered
      first.hashCode mustBe reordered.hashCode
      first must not be later
    }
  }
}

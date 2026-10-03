package domain.booking

import domain.student.StudentNumber
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers

class BookingMemberApprovalsSpec extends AnyWordSpec with Matchers {
  private val alice = StudentNumber("AB12345678")
  private val bob = StudentNumber("CD12345678")
  private val members = Set(alice, bob)

  "Booking member approvals" should {
    "keep zero and partial approvals incomplete" in {
      val none = BookingMemberApprovals.evaluate(members, Set.empty).toOption.get
      val partial = BookingMemberApprovals.evaluate(members, Set(alice)).toOption.get

      none.allMembersApproved mustBe false
      partial.allMembersApproved mustBe false
      partial.approvedMembers mustBe Set(alice)
    }

    "recognize approval from every member" in {
      val approvals = BookingMemberApprovals.evaluate(members, Set(bob, alice)).toOption.get

      approvals.allMembersApproved mustBe true
    }

    "reject an outsider's approval even when the count matches" in {
      val outsider = StudentNumber("EF12345678")

      BookingMemberApprovals.evaluate(members, Set(alice, outsider)).isLeft mustBe true
    }

    "reject an empty target membership" in {
      BookingMemberApprovals.evaluate(Set.empty, Set.empty).isLeft mustBe true
    }

    "handle a single-member reservation" in {
      BookingMemberApprovals.evaluate(Set(alice), Set.empty).toOption.get.allMembersApproved mustBe false
      BookingMemberApprovals.evaluate(Set(alice), Set(alice)).toOption.get.allMembersApproved mustBe true
    }

    "compare snapshots by value without changing an earlier result" in {
      val partial = BookingMemberApprovals.evaluate(members, Set(alice)).toOption.get
      val same = BookingMemberApprovals.evaluate(Set(bob, alice), Set(alice)).toOption.get
      val complete = BookingMemberApprovals.evaluate(members, members).toOption.get

      partial mustBe same
      partial.hashCode mustBe same.hashCode
      partial.approvedMembers mustBe Set(alice)
      partial.allMembersApproved mustBe false
      complete.allMembersApproved mustBe true
    }

    "prevent callers from bypassing validation or supplying a completion flag" in {
      val approvals = BookingMemberApprovals.evaluate(members, Set(alice)).toOption.get

      assertCompiles("approvals.allMembersApproved")
      assertDoesNotCompile("approvals.copy(members = Set.empty)")
      assertDoesNotCompile("BookingMemberApprovals(members, Set(alice))")
      assertDoesNotCompile("new BookingMemberApprovals(members, Set(alice))")
      assertDoesNotCompile("approvals.copy(allMembersApproved = true)")
    }
  }
}

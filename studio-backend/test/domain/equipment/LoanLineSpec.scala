package domain.equipment

import domain.booking.BookingId
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers

class LoanLineSpec extends AnyWordSpec with Matchers {
  private val bookingId = BookingId("550e8400-e29b-41d4-a716-446655440401")
  private val equipmentId = EquipmentId("550e8400-e29b-41d4-a716-446655440101")

  "A loan line" should {
    "record a checkout without changing the original line or its reservation" in {
      val original = LoanLine(bookingId, equipmentId, reservedQuantity = 2)

      val checkedOut = original.checkout(1).toOption.get

      checkedOut.bookingId mustBe bookingId
      checkedOut.equipmentId mustBe equipmentId
      checkedOut.reservedQuantity mustBe 2
      checkedOut.checkedOutQuantity mustBe 1
      checkedOut.returnedQuantity mustBe 0
      original.checkedOutQuantity mustBe 0
    }

    "accumulate partial checkouts and reject more than the reservation" in {
      val first = LoanLine(bookingId, equipmentId, 3).checkout(1).toOption.get
      val second = first.checkout(2).toOption.get

      second.checkedOutQuantity mustBe 3
      second.checkout(1).isLeft mustBe true
      second.checkedOutQuantity mustBe 3
    }

    "accumulate partial returns without changing the original checkout" in {
      val checkedOut = LoanLine(bookingId, equipmentId, 3).checkout(3).toOption.get
      val first = checkedOut.returnEquipment(1).toOption.get
      val second = first.returnEquipment(2).toOption.get

      second.returnedQuantity mustBe 3
      second.checkedOutQuantity mustBe 3
      second.reservedQuantity mustBe 3
      checkedOut.returnedQuantity mustBe 0
      first.returnedQuantity mustBe 1
    }

    "reject a return larger than the outstanding quantity without changing totals" in {
      val checkedOut = LoanLine(bookingId, equipmentId, 3).checkout(2).toOption.get
      val partlyReturned = checkedOut.returnEquipment(1).toOption.get

      partlyReturned.returnEquipment(2).isLeft mustBe true
      partlyReturned.checkedOutQuantity mustBe 2
      partlyReturned.returnedQuantity mustBe 1
    }

    "reject a non-positive checkout quantity without changing the line" in {
      val line = LoanLine(bookingId, equipmentId, 3)

      List(0, -1, Int.MinValue).foreach { quantity =>
        withClue(s"quantity=$quantity: ") {
          line.checkout(quantity).isLeft mustBe true
        }
      }
      line.checkedOutQuantity mustBe 0
    }

    "reject a non-positive return quantity without changing the line" in {
      val line = LoanLine(bookingId, equipmentId, 3).checkout(2).toOption.get

      List(0, -1, Int.MinValue).foreach { quantity =>
        withClue(s"quantity=$quantity: ") {
          line.returnEquipment(quantity).isLeft mustBe true
        }
      }
      line.checkedOutQuantity mustBe 2
      line.returnedQuantity mustBe 0
    }
  }
}

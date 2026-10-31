package domain.booking

import domain.student.{StudentNumber, TAId}
import java.time.Instant

/** 学生予約のメンバーに対する本人確認結果。
  * 生成成功は受付操作の許可ではない。認証・利用日・予約状態・未Check-inの判定は別途必要。
  * 対象は予約から取得して検証したMembers、確認者と日時は呼び出し側で確定した値を渡す。
  */
final case class IdentityVerification private (
    members: Members,
    private val verifiedMemberNumbers: Set[StudentNumber],
    verifiedBy: TAId,
    verifiedAt: Instant
) {
  def isVerified(studentNumber: StudentNumber): Boolean = verifiedMemberNumbers.contains(studentNumber)

  // 生成時に全確認対象の所属を検証しているため、人数がそろえば全員確認済み。
  def allMembersVerified: Boolean = verifiedMemberNumbers.size == members.size

  /** 確認済み一覧を全体置換し、新しい値を返す。空の一覧は確認済み0名を表す。 */
  def replace(
      verifiedMemberNumbers: List[StudentNumber],
      verifiedBy: TAId,
      verifiedAt: Instant
  ): Either[StudentValidationError, IdentityVerification] =
    IdentityVerification.record(members, verifiedMemberNumbers, verifiedBy, verifiedAt)
}

object IdentityVerification {
  /** 不変条件を検証する唯一の生成口。全員確認の真偽値は入力として受け取らない。 */
  def record(
      members: Members,
      verifiedMemberNumbers: List[StudentNumber],
      verifiedBy: TAId,
      verifiedAt: Instant
  ): Either[StudentValidationError, IdentityVerification] = {
    val verified = verifiedMemberNumbers.toSet
    if (verified.size != verifiedMemberNumbers.size)
      Left(StudentValidationError("確認対象の学生番号は重複できません"))
    else if (!verified.forall(members.contains))
      Left(StudentValidationError("予約メンバー以外は本人確認できません"))
    else Right(new IdentityVerification(members, verified, verifiedBy, verifiedAt))
  }
}

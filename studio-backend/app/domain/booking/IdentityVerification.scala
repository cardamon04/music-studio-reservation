package domain.booking

import domain.student.{StudentNumber, TAId}
import java.time.Instant

/** 学生予約のメンバーに対する本人確認結果。
  * 生成成功は受付操作の許可ではない。認証・利用日・予約状態・未Check-inの判定は別途必要。
  * メンバーは予約から取得した集合、確認者と日時は呼び出し側で確定した値を渡す。
  */
final case class IdentityVerification private (
    members: Set[StudentNumber],
    verifiedMemberNumbers: Set[StudentNumber],
    verifiedBy: TAId,
    verifiedAt: Instant
) {
  def allMembersVerified: Boolean = verifiedMemberNumbers == members

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
      members: Set[StudentNumber],
      verifiedMemberNumbers: List[StudentNumber],
      verifiedBy: TAId,
      verifiedAt: Instant
  ): Either[StudentValidationError, IdentityVerification] = {
    val verified = verifiedMemberNumbers.toSet
    if (members.isEmpty)
      Left(StudentValidationError("学生予約のメンバーは1名以上必要です"))
    else if (verified.size != verifiedMemberNumbers.size)
      Left(StudentValidationError("確認対象の学生番号は重複できません"))
    else if (!verified.subsetOf(members))
      Left(StudentValidationError("予約メンバー以外は本人確認できません"))
    else Right(new IdentityVerification(members, verified, verifiedBy, verifiedAt))
  }
}

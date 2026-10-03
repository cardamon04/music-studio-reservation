package domain.booking

import domain.student.StudentNumber

/** 学生予約の対象メンバーと、取得済みの承認結果を照合した値。
  * 全員承認は予約確定の十分条件ではない。認証・通知・枠/在庫・状態遷移は含まない。
  * 承認操作そのものや、作成者を初期状態で承認済みにするかはここでは決めない。
  */
final case class BookingMemberApprovals private (
    members: Set[StudentNumber],
    approvedMembers: Set[StudentNumber]
) {
  def allMembersApproved: Boolean = approvedMembers == members
}

object BookingMemberApprovals {
  /** 対象は1名以上、承認済みは対象の部分集合であることを検証する。
    * 引数は呼び出し側が取得した集合。更新せず、新しい照合結果を返す。
    */
  def evaluate(
      members: Set[StudentNumber],
      approvedMembers: Set[StudentNumber]
  ): Either[StudentValidationError, BookingMemberApprovals] =
    if (members.isEmpty)
      Left(StudentValidationError("学生予約のメンバーは1名以上必要です"))
    else if (!approvedMembers.subsetOf(members))
      Left(StudentValidationError("予約メンバー以外の承認は含められません"))
    else Right(new BookingMemberApprovals(members, approvedMembers))
}

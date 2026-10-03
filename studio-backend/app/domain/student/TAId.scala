package domain.student

import java.util.UUID

/** 受付操作の実行者を参照する識別子。権限を持つことの証明ではない。 */
final case class TAId(value: UUID) {
  require(value != null, "TAIdは必須です")
}

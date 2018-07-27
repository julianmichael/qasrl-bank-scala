package qasrl.bank

import cats.Order
import cats.implicits._

case class DocumentMetadata(
  id: DocumentId,
  part: DatasetPartition,
  title: String
)

object DocumentMetadata {
  implicit val documentMetadataOrder = Order.whenEqual(
    Order.by[DocumentMetadata, String](_.title),
    Order.by[DocumentMetadata, DocumentId](_.id)
  )
}

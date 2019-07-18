package qasrl.bank

import scala.collection.immutable.SortedSet
import io.circe.generic.JsonCodec

import cats.Order.catsKernelOrderingForOrder

@JsonCodec case class DataIndex(
  documents: Map[DatasetPartition, SortedSet[DocumentMetadata]],
  denseIds: SortedSet[SentenceId]
) {
  lazy val allDocumentMetas = documents.values.reduce(_ union _)
  lazy val allDocumentIds = allDocumentMetas.map(_.id)
  def numDocuments = allDocumentMetas.size
  def getPart(id: DocumentId): DatasetPartition = documents.find(_._2.exists(_.id == id)).get._1
}

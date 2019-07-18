package qasrl.bank

import cats.Order
import cats.Order.catsKernelOrderingForOrder

import scala.collection.immutable.SortedSet

import qasrl.data.Sentence

import io.circe.generic.JsonCodec

@JsonCodec case class Document(metadata: DocumentMetadata, sentences: SortedSet[Sentence])

object Document {
  implicit val documentOrder = Order.by[Document, DocumentMetadata](_.metadata)
}

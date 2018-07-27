package qasrl.bank

import cats.Order

import scala.collection.immutable.SortedSet

import qasrl.data.Sentence

case class Document(metadata: DocumentMetadata, sentences: SortedSet[Sentence])

object Document {
  implicit val documentOrder = Order.by[Document, DocumentMetadata](_.metadata)
}

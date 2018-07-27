package qasrl.bank

import cats.Order
import cats.implicits._

case class SentenceId(
  documentId: DocumentId,
  paragraphNum: Int,
  sentenceNum: Int
) {
  def domain = documentId.domain
}

object SentenceId {

  implicit val sentenceIdOrder: Order[SentenceId] = Order.whenEqual(
    Order.by[SentenceId, DocumentId](_.documentId),
    Order.whenEqual(
      Order.by[SentenceId, Int](_.paragraphNum),
      Order.by[SentenceId, Int](_.sentenceNum)
    )
  )

  private[this] val Wiki1kMatch = "Wiki1k:([^:]+):([^:]+):([0-9]+):([0-9]+)".r
  private[this] val TQAMatch = "TQA:([^:]+)_([0-9]+)".r

  // not necessarily used for serialization over the wire, but
  // used for storing to / reading from  the dataset file.
  def toString(sid: SentenceId) = sid match {
    case SentenceId(DocumentId(domain, docId), paragraphNum, sentenceNum) =>
      domain match {
        case Domain.TQA => s"TQA:${docId}_${sentenceNum}"
        case d          => s"Wiki1k:$domain:$docId:$paragraphNum:$sentenceNum"
      }
  }

  def fromString(s: String): SentenceId = s match {
    case TQAMatch(topicId, sentenceNum) =>
      SentenceId(DocumentId(Domain.TQA, topicId), 0, sentenceNum.toInt)
    case Wiki1kMatch(domainStr, docId, paragraphNum, sentenceNum) =>
      val domain = domainStr match {
        case "wikipedia" => Domain.Wikipedia
        case "wikinews"  => Domain.Wikinews
      }
      SentenceId(DocumentId(domain, docId), paragraphNum.toInt, sentenceNum.toInt)
  }
}

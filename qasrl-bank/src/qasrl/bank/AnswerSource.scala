package qasrl.bank

import cats.Order
import cats.implicits._

case class AnswerSource(turkerId: String, round: AnnotationRound)

object AnswerSource {
  private[this] val TurkerMatch = "turk-qasrl2.0-([0-9]+)-?(.*)".r
  import AnnotationRound._

  def fromString(s: String) = s match {
    case TurkerMatch(id, round) =>
      AnswerSource(
        id,
        round match {
          case ""          => Original
          case "expansion" => Expansion
          case "eval"      => Eval
        }
      )
  }

  implicit val answerSourceOrder = Order.whenEqual(
    Order.by[AnswerSource, AnnotationRound](_.round),
    Order.by[AnswerSource, String](_.turkerId)
  )
}

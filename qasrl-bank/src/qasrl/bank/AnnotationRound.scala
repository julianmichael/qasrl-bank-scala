package qasrl.bank

import cats.Order
import cats.implicits._

sealed trait AnnotationRound {
  import AnnotationRound._
  def isOriginal = this == Original
  def isExpansion = this == Expansion
  def isEval = this == Eval
}

object AnnotationRound {
  case object Original extends AnnotationRound
  case object Expansion extends AnnotationRound
  case object Eval extends AnnotationRound

  implicit val annotationRoundOrder = Order.by[AnnotationRound, Int] {
    case Original  => 0
    case Expansion => 1
    case Eval      => 2
  }
}

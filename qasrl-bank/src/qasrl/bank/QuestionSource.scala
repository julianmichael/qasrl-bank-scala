package qasrl.bank

import cats.Order
import cats.implicits._

sealed trait QuestionSource {
  import QuestionSource._
  def getModel = Some(this).collect { case x: Model => x }
  def isModel = getModel.nonEmpty
  def getTurker = Some(this).collect { case x: Turker => x }
  def isTurker = getTurker.nonEmpty
}

object QuestionSource {
  case class Model(version: String) extends QuestionSource
  case class Turker(turkerId: String) extends QuestionSource

  private[this] val ModelMatch = "model-qasrl2.0-(.+)".r
  private[this] val TurkerMatch = "turk-qasrl2.0-([0-9]+)".r

  def fromString(s: String) = s match {
    case ModelMatch(version) => Model(version)
    case TurkerMatch(id)     => Turker(id)
  }

  implicit val questionSourceOrder: Order[QuestionSource] = Order.whenEqual(
    Order.by[QuestionSource, Int] {
      case Turker(_) => 0
      case Model(_)  => 1
    },
    Order.by[QuestionSource, String] {
      case Turker(id) => id
      case Model(ver) => ver
    }
  )
}

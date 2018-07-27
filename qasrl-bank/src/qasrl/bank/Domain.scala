package qasrl.bank

import nlpdata.util.LowerCaseStrings._

import cats.Order
import cats.implicits._

sealed trait Domain {
  import Domain._
  override def toString: String = this match {
    case Wikipedia => "wikipedia"
    case Wikinews  => "wikinews"
    case TQA       => "TQA"
  }
}

object Domain {
  case object Wikipedia extends Domain
  case object Wikinews extends Domain
  case object TQA extends Domain

  implicit val domainOrder = Order.by[Domain, Int] {
    case Wikipedia => 0
    case Wikinews  => 1
    case TQA       => 2
  }

  def fromString(s: LowerCaseString): Option[Domain] = s.toString match {
    case "wikipedia" => Some(Wikipedia)
    case "wikinews"  => Some(Wikinews)
    case "tqa"       => Some(TQA)
  }
}

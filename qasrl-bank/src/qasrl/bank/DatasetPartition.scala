package qasrl.bank

sealed trait DatasetPartition {
  import DatasetPartition._
  override def toString = this match {
    case Train => "train"
    case Dev   => "dev"
    case Test  => "test"
  }
}

object DatasetPartition {
  case object Train extends DatasetPartition
  case object Dev extends DatasetPartition
  case object Test extends DatasetPartition

  def fromString(s: String): Option[DatasetPartition] = s match {
    case "train" => Some(Train)
    case "dev"   => Some(Dev)
    case "test"  => Some(Test)
    case _       => None
  }
}

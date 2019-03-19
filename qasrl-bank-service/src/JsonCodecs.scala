package qasrl.bank.service

import qasrl.bank.DocumentId

import io.circe.{Decoder, Encoder}

object JsonCodecs {
  import JsonCodecsContainer._
  implicit lazy val documentIdSetEncoder = derivedDocumentIdSetEncoder
  implicit lazy val documentIdSetDecoder = derivedDocumentIdSetDecoder
  implicit lazy val searchQueryEncoder = derivedSearchQueryEncoder
  implicit lazy val searchQueryDecoder = derivedSearchQueryDecoder
}

private[service] object JsonCodecsContainer {

  import nlpdata.util.LowerCaseStrings._
  implicit val lowerCaseStringEncoder = Encoder.encodeString.contramap[LowerCaseString](_.toString)
  implicit val lowerCaseStringDecoder = Decoder.decodeString.map(_.lowerCase)
  import qasrl.data.JsonCodecs.{inflectedFormsEncoder, inflectedFormsDecoder}

  val derivedDocumentIdSetEncoder: Encoder[collection.immutable.Set[DocumentId]] = {
    import io.circe.generic.auto._
    import qasrl.bank.JsonCodecs._
    implicitly[Encoder[collection.immutable.Set[DocumentId]]]
  }

  val derivedDocumentIdSetDecoder: Decoder[collection.immutable.Set[DocumentId]] = {
    import cats.Order.catsKernelOrderingForOrder
    import io.circe.generic.auto._
    import qasrl.bank.JsonCodecs._
    implicitly[Decoder[collection.immutable.Set[DocumentId]]]
  }

  val derivedSearchQueryEncoder: Encoder[Search.Query] = {
    import io.circe.generic.auto._
    import qasrl.bank.JsonCodecs._
    implicitly[Encoder[Search.Query]]
  }

  val derivedSearchQueryDecoder: Decoder[Search.Query] = {
    import cats.Order.catsKernelOrderingForOrder
    import io.circe.generic.auto._
    import qasrl.bank.JsonCodecs._
    implicitly[Decoder[Search.Query]]
  }
}

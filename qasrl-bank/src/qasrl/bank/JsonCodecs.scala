package qasrl.bank

import nlpdata.util.LowerCaseStrings._

import io.circe._

object JsonCodecs {

  implicit val datasetPartitionKeyEncoder = KeyEncoder.instance[DatasetPartition](_.toString)
  implicit val datasetPartitionKeyDecoder = KeyDecoder.instance(DatasetPartition.fromString)

  implicit val datasetPartitionEncoder = Encoder.instance[DatasetPartition](
    part => Json.fromString(part.toString)
  )
  implicit val datasetPartitionDecoder = Decoder.instance(
    c =>
      c.as[String]
        .right
        .flatMap(
          str =>
            DatasetPartition.fromString(str) match {
              case Some(part) => Right(part)
              case None =>
                Left(DecodingFailure("Failed to parse dataset partition value", c.history))
          }
      )
  )

  implicit val domainEncoder = Encoder.instance[Domain](
    domain => Json.fromString(domain.toString.toLowerCase)
  )
  implicit val domainDecoder = Decoder.instance(
    c =>
      c.as[String]
        .right
        .flatMap(
          str =>
            Domain.fromString(str.lowerCase) match {
              case Some(part) => Right(part)
              case None       => Left(DecodingFailure("Failed to parse domain value", c.history))
          }
      )
  )

  implicit val documentMetadataEncoder = Encoder.instance[DocumentMetadata] { docMeta =>
    import io.circe.syntax._
    val idPrefix = docMeta.id.domain match {
      case Domain.Wikipedia => "Wiki1k:wikipedia"
      case Domain.Wikinews  => "Wiki1k:wikinews"
      case Domain.TQA       => "TQA"
    }
    val idString = idPrefix + ":" + docMeta.id.id
    Json.obj(
      "part"     -> docMeta.part.asJson,
      "idString" -> Json.fromString(idString),
      "domain"   -> docMeta.id.domain.asJson,
      "id"       -> Json.fromString(docMeta.id.id),
      "title"    -> Json.fromString(docMeta.title)
    )
  }
  implicit val documentMetadataDecoder = Decoder.instance { c =>
    for {
      part   <- c.downField("part").as[DatasetPartition]
      domain <- c.downField("domain").as[Domain]
      id     <- c.downField("id").as[String]
      title  <- c.downField("title").as[String]
    } yield DocumentMetadata(DocumentId(domain, id), part, title)
  }

  implicit val sentenceIdEncoder = Encoder.instance[SentenceId](
    sid => Json.fromString(SentenceId.toString(sid))
  )
  implicit val sentenceIdDecoder = Decoder.instance(
    c =>
      c.as[String]
        .right
        .flatMap(
          str =>
            scala.util.Try(SentenceId.fromString(str)).toOption match {
              case Some(part) => Right(part)
              case None       => Left(DecodingFailure("Failed to parse sentence ID value", c.history))
          }
      )
  )

  implicit val documentEncoder: Encoder[Document] = {
    import qasrl.data.JsonCodecs._
    import io.circe.generic.semiauto._
    deriveEncoder[Document]
  }

  implicit val documentDecoder: Decoder[Document] = {
    import qasrl.data.JsonCodecs._
    import io.circe.generic.semiauto._
    import cats.Order.catsKernelOrderingForOrder
    deriveDecoder[Document]
  }

  implicit val dataIndexEncoder = {
    import io.circe.generic.semiauto._
    deriveEncoder[DataIndex]
  }
  implicit val dataIndexDecoder = {
    import io.circe.generic.semiauto._
    import cats.Order.catsKernelOrderingForOrder
    deriveDecoder[DataIndex]
  }
}

package qasrl.bank.service

import qasrl.bank.DataIndex
import qasrl.bank.Document
import qasrl.bank.DocumentId
import qasrl.bank.Domain

import nlpdata.util.LowerCaseStrings._

import cats.implicits._
import cats.effect._

import org.http4s._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

object HttpDocumentService {

  def makeService(
    index: DataIndex,
    documents: Map[DocumentId, Document],
    searchIndex: Map[LowerCaseString, Set[DocumentId]]
  ) = {

    import qasrl.bank.JsonCodecs._
    import qasrl.bank.service.JsonCodecs._
    import io.circe.syntax._
    import org.http4s.dsl.io._
    import org.http4s.circe._

    HttpService[IO] {
      case GET -> Root / "index" =>
        Ok(index.asJson)
      case GET -> Root / "doc" / domain / id =>
        Ok(documents(DocumentId(Domain.fromString(domain.lowerCase).get, id)).asJson)
      case GET -> Root / "search" / query =>
        val keywords = query.split(" ").map(_.trim).filter(_.nonEmpty).map(_.lowerCase).toSet
        if (keywords.isEmpty) {
          Ok(documents.keySet.asJson)
        } else {
          val results: Set[DocumentId] = keywords
            .map(w => searchIndex.get(w).getOrElse(Set.empty[DocumentId]))
            .reduce(_ intersect _)
          Ok(results.asJson)
        }
    }
  }
}

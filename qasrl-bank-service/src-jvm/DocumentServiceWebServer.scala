package qasrl.bank.service

import qasrl.bank.Data

import cats.data.NonEmptySet
import cats.implicits._
import cats.effect._
import cats.effect.implicits._

import org.http4s._
import org.http4s.implicits._

import org.http4s.server.blaze._

import fs2.Stream

import scala.concurrent.ExecutionContext.Implicits.global

import nlpdata.util.Text
import nlpdata.util.LowerCaseStrings._

object DocumentServiceWebServer {

  def serve(
    data: Data,
    port: Int,
    restrictedClientDomains: Option[NonEmptySet[String]] // None for no restrictions
  )(implicit cs: ContextShift[IO], t: Timer[IO]): Stream[IO, ExitCode] = {

    val index = data.index
    val docs = data.documentsById
    val searchIndex = Search.createSearchIndex(docs.values.iterator)

    val bareService = HttpDocumentService.makeService(index, docs, searchIndex)

    import org.http4s.server.middleware._
    import scala.concurrent.duration._

    val corsConfig = restrictedClientDomains match {
      case None =>
        CORSConfig(
          anyOrigin = true,
          anyMethod = false,
          allowedMethods = Some(Set("GET")),
          allowCredentials = false,
          maxAge = 1.day.toSeconds
        )
      case Some(domains) =>
        CORSConfig(
          anyOrigin = false,
          allowedOrigins = domains.toSortedSet,
          anyMethod = false,
          allowedMethods = Some(Set("GET")),
          allowCredentials = false,
          maxAge = 1.day.toSeconds
        )
    }

    val service = CORS(bareService, corsConfig).orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(service)
      .serve
  }

}

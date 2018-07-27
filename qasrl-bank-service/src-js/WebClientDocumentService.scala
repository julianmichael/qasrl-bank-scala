package qasrl.bank.service

import qasrl.bank.DataIndex
import qasrl.bank.Document
import qasrl.bank.DocumentId

import cats.Monad
import cats.~>
import cats.Id
import cats.implicits._

import io.circe.parser.decode

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class WebClientDocumentServiceInterpreter(
  apiUrl: String
) extends (DocumentService.Request ~> Future) {
  import DocumentService._

  import qasrl.bank.JsonCodecs._
  import qasrl.bank.service.JsonCodecs._

  def apply[A](req: Request[A]): Future[A] = {

    req match {
      case GetDataIndex =>
        sendRequest(req).map(_.responseText).flatMap { dataIndexJsonStr =>
          decode[DataIndex](dataIndexJsonStr) match {
            case Left(err)    => Future.failed[DataIndex](new RuntimeException(err))
            case Right(index) => Future.successful(index)
          }
        }
      case GetDocument(id) => {
        sendRequest(GetDocument(id)).map(_.responseText).flatMap { documentJsonStr =>
          decode[Document](documentJsonStr) match {
            case Left(err)       => Future.failed[Document](new RuntimeException(err))
            case Right(document) => Future.successful(document)
          }
        }
      }
      case SearchDocuments(query) => {
        sendRequest(SearchDocuments(query)).map(_.responseText).flatMap { documentIdSetJsonStr =>
          decode[Set[DocumentId]](documentIdSetJsonStr) match {
            case Left(err)          => Future.failed[Set[DocumentId]](new RuntimeException(err))
            case Right(documentIds) => Future.successful(documentIds)
          }
        }
      }
    }
  }

  private[this] def sendRequest[A](req: Request[A]) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    org.scalajs.dom.ext.Ajax.get(url = apiUrl + "/" + getRoute(req))
  }

  private[this] def getRoute[A](req: Request[A]): String = req match {
    case GetDataIndex           => s"index"
    case GetDocument(id)        => s"doc/${id.domain}/${id.id}"
    case SearchDocuments(query) => s"search/${query.mkString(" ")}"
  }
}

class WebClientDocumentService(
  apiUrl: String
) extends InterpretedDocumentService[Future](
      WebClientDocumentServiceInterpreter(apiUrl)
    )

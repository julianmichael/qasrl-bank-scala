package qasrl.bank.service

import qasrl.bank._

import qasrl.data.Sentence

import cats._
import cats.implicits._
import cats.free.Free

import nlpdata.util.LowerCaseStrings._

trait DocumentService[M[_]] {
  def getDataIndex: M[DataIndex]
  def getDocument(id: DocumentId): M[Document]
  def searchDocuments(query: Search.Query): M[Set[DocumentId]]
}

object DocumentService {
  sealed trait Request[A]
  case object GetDataIndex extends Request[DataIndex]
  case class GetDocument(id: DocumentId) extends Request[Document]
  case class SearchDocuments(query: Search.Query) extends Request[Set[DocumentId]]
}

object FreeDocumentService extends DocumentService[Free[DocumentService.Request, ?]] {

  import DocumentService._

  type RequestFree[A] = Free[Request, A]

  def getDataIndex: RequestFree[DataIndex] =
    Free.liftF[Request, DataIndex](GetDataIndex)

  def getDocument(id: DocumentId): RequestFree[Document] =
    Free.liftF[Request, Document](GetDocument(id))

  def searchDocuments(query: Search.Query): RequestFree[Set[DocumentId]] =
    Free.liftF[Request, Set[DocumentId]](SearchDocuments(query))
}

class InterpretedDocumentService[M[_]](interpreter: DocumentService.Request ~> M)(
  implicit M: Monad[M]
) extends DocumentService[M] {

  override def getDataIndex: M[DataIndex] =
    FreeDocumentService.getDataIndex.foldMap(interpreter)
  override def getDocument(id: DocumentId): M[Document] =
    FreeDocumentService.getDocument(id).foldMap(interpreter)
  override def searchDocuments(query: Search.Query): M[Set[DocumentId]] =
    FreeDocumentService.searchDocuments(query).foldMap(interpreter)
}

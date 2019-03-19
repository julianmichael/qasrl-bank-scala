package qasrl.bank.service

import qasrl.bank.DataIndex
import qasrl.bank.Document
import qasrl.bank.DocumentId

import cats.~>
import cats.Id

import nlpdata.util.LowerCaseStrings._

case class BasicDocumentServiceInterpreter(
  index: DataIndex,
  documents: Map[DocumentId, Document],
  searchIndex: Search.Index
) extends (DocumentService.Request ~> Id) {
  import DocumentService._

  def apply[A](req: Request[A]): A = req match {
    case GetDataIndex    => index
    case GetDocument(id) => documents(id)
    case SearchDocuments(query) => Search.execute(query, searchIndex, documents)
  }
}

class BasicDocumentService(
  index: DataIndex,
  documents: Map[DocumentId, Document],
  searchIndex: Search.Index
) extends InterpretedDocumentService[Id](
      BasicDocumentServiceInterpreter(index, documents, searchIndex)
    )

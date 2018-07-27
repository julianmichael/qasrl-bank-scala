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
  searchIndex: Map[LowerCaseString, Set[DocumentId]]
) extends (DocumentService.Request ~> Id) {
  import DocumentService._

  def apply[A](req: Request[A]): A = req match {
    case GetDataIndex    => index
    case GetDocument(id) => documents(id)
    case SearchDocuments(query) =>
      if (query.isEmpty) {
        documents.keySet
      } else {
        query
          .map(w => searchIndex.get(w).getOrElse(Set.empty[DocumentId]))
          .reduce(_ intersect _)
      }
  }
}

class BasicDocumentService(
  index: DataIndex,
  documents: Map[DocumentId, Document],
  searchIndex: Map[LowerCaseString, Set[DocumentId]]
) extends InterpretedDocumentService[Id](
      BasicDocumentServiceInterpreter(index, documents, searchIndex)
    )

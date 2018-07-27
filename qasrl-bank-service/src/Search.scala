package qasrl.bank.service

import qasrl.bank.Document

import qasrl.data.Sentence

import nlpdata.util.LowerCaseStrings._
import nlpdata.util.Text

object Search {

  def getQueryMatchesInSentence(
    sentence: Sentence,
    query: Set[LowerCaseString]
  ): Set[Int] = {
    sentence.sentenceTokens.indices.filter { i =>
      val token = sentence.sentenceTokens(i)
      query.contains(token.lowerCase) || query.contains(Text.normalizeToken(token).lowerCase) ||
      sentence.verbEntries.get(i).fold(false) { verb =>
        verb.verbInflectedForms.allForms.toSet.intersect(query).nonEmpty
      }
    }.toSet
  }

  def createSearchIndex(documents: Iterator[Document]) = {
    def tokenDocPairs =
      for {
        doc      <- documents
        sent     <- doc.sentences.iterator
        tokIndex <- sent.sentenceTokens.indices.iterator
        tok <- (
          List(sent.sentenceTokens(tokIndex), Text.normalizeToken(sent.sentenceTokens(tokIndex))) ++
          sent.verbEntries
            .get(tokIndex)
            .fold(List.empty[String])(verb => verb.verbInflectedForms.allForms.map(_.toString))
        ).iterator
      } yield tok.lowerCase -> doc.metadata.id

    tokenDocPairs.toList.groupBy(_._1).map {
      case (tok, pairs) =>
        tok -> pairs.map(_._2).toSet
    }
  }
}

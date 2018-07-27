package qasrl.bank

import scala.collection.immutable.SortedSet
import scala.collection.immutable.SortedMap
import scala.util.Try

import java.nio.file.Path
import java.nio.file.Files

import cats.Id
import cats.implicits._

import qasrl.data.Dataset
import qasrl.data.Sentence

case class FullData(
  index: DataIndex,
  all: Dataset,
  documentsById: Map[DocumentId, Document],
  trainOrig: Dataset,
  devOrig: Dataset,
  testOrig: Dataset,
  trainExpanded: Dataset,
  devExpanded: Dataset,
  devDense: Dataset,
  testDense: Dataset
) {
  def small = Data(index, all, documentsById)
}

case class Data(
  index: DataIndex,
  all: Dataset,
  documentsById: Map[DocumentId, Document]
)

object Data {

  def filterExpandedToOrig(dataset: Dataset) = {
    val withoutQuestions =
      dataset.filterQuestionSources(qs => QuestionSource.fromString(qs).isTurker)
    val withoutAnswers = Dataset.questionLabels.modify(
      ql =>
        ql.copy(
          answerJudgments = ql.answerJudgments.filter(
            al => AnswerSource.fromString(al.sourceId).round.isOriginal
          )
      )
    )(withoutQuestions)
    withoutAnswers
  }

  def readDataset(path: Path): Dataset = {
    import java.io.FileInputStream
    import java.util.zip.GZIPInputStream
    val source = scala.io.Source.fromInputStream(
      new GZIPInputStream(new FileInputStream(path.toString))
    )
    import qasrl.data.JsonCodecs._
    Dataset(
      SortedMap(
        source.getLines.map { line =>
          val sentence = io.circe.jawn.decode[Sentence](line).right.get
          sentence.sentenceId -> sentence
        }.toSeq: _*
      )
    )
  }

  def writeDatasetUnzipped(path: Path, dataset: Dataset) = {
    import qasrl.data.JsonCodecs._
    import io.circe.generic.auto._
    import io.circe.syntax._
    val printer = io.circe.Printer.noSpaces
    Files.write(path, printer.pretty(dataset.asJson).getBytes("UTF-8"))
  }

  def writeDatasetJS(path: Path, dataset: Dataset) = {
    import qasrl.data.JsonCodecs._
    import io.circe.generic.auto._
    import io.circe.syntax._
    val printer = io.circe.Printer.noSpaces
    Files.write(path, ("var dataset = " + printer.pretty(dataset.asJson) + ";").getBytes("UTF-8"))
  }

  import io.circe.syntax._
  import qasrl.bank.JsonCodecs._

  def writeIndex(path: Path, index: DataIndex) = {
    val printer = io.circe.Printer.noSpaces
    Files.write(path, printer.pretty(index.asJson).getBytes("UTF-8"))
  }

  def writeIndexJS(path: Path, index: DataIndex) = {
    val printer = io.circe.Printer.noSpaces
    val res = "var dataMetaIndex = " + printer.pretty(index.asJson) + ";"
    Files.write(path, res.getBytes("UTF-8"))
  }

  def readIndexZipped(path: Path): DataIndex = {
    import java.io.FileInputStream
    import java.util.zip.GZIPInputStream
    val source = scala.io.Source.fromInputStream(
      new GZIPInputStream(new FileInputStream(path.toString))
    )
    // weirdness is so we actually close the file... yeah it's dumb, but I just don't care rn
    var jsonString: String = null
    source.getLines.foreach { line =>
      if (jsonString == null) jsonString = line
    }
    import qasrl.bank.JsonCodecs._
    io.circe.jawn.decode[DataIndex](jsonString) match {
      case Right(index) => index
      case Left(err)    => println(err); ???
    }
  }

  def readFromQasrlBank(qasrlBankPath: Path): Try[FullData] = Try {
    val trainExpanded = readDataset(qasrlBankPath.resolve("expanded").resolve("train.jsonl.gz"))
    val devExpanded = readDataset(qasrlBankPath.resolve("expanded").resolve("dev.jsonl.gz"))

    // avoid having to read more files since result is the same anyway
    val trainOrig = filterExpandedToOrig(trainExpanded)
    val devOrig = filterExpandedToOrig(devExpanded)
    val testOrig = readDataset(qasrlBankPath.resolve("orig").resolve("test.jsonl.gz"))

    val devDense = readDataset(qasrlBankPath.resolve("dense").resolve("dev.jsonl.gz"))
    val testDense = readDataset(qasrlBankPath.resolve("dense").resolve("test.jsonl.gz"))

    implicit val datasetMonoid = Dataset.datasetMonoid(Dataset.printMergeErrors)
    val all = trainExpanded |+| devExpanded |+| testOrig |+| devDense |+| testDense

    val sentenceIdToPart = (
      trainExpanded.sentences.keySet.map(s => SentenceId.fromString(s) -> DatasetPartition.Train) ++
      devExpanded.sentences.keySet.map(s => SentenceId.fromString(s)   -> DatasetPartition.Dev) ++
      testOrig.sentences.keySet.map(s => SentenceId.fromString(s)      -> DatasetPartition.Test)
    ).toMap

    val denseIds = (devDense.sentences.keySet ++ testDense.sentences.keySet)
      .map(SentenceId.fromString)

    val index = readIndexZipped(qasrlBankPath.resolve("index.json.gz"))

    val documentsById = {

      val sentencesByDocId = all.sentences.values.toSet
        .groupBy((s: Sentence) => SentenceId.fromString(s.sentenceId).documentId)

      val docIdToMeta = index.documents.values.reduce(_ union _).map(meta => meta.id -> meta).toMap

      val documents = sentencesByDocId.iterator.map {
        case (docId @ DocumentId(domain, idString), sentences) =>
          def makeDocumentMetadata(title: String) = DocumentMetadata(
            docId,
            sentenceIdToPart(SentenceId.fromString(sentences.head.sentenceId)),
            title
          )
          val metadata = docIdToMeta(docId)
          Document(metadata, SortedSet(sentences.toSeq: _*))
      }.toSeq

      val documentsById = documents.map(doc => doc.metadata.id -> doc).toMap

      documentsById
    }

    FullData(
      index,
      all,
      documentsById,
      trainOrig,
      devOrig,
      testOrig,
      trainExpanded,
      devExpanded,
      devDense,
      testDense
    )
  }
}

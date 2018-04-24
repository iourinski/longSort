package joom.longsort.runners

import java.io.{File, PrintWriter}

import joom.longsort.models.SortingConfig
import joom.longsort.sorters.{FastStreamSorter, LocalSorter, Sorter, StreamSorter}
import joom.longsort.utils.SortFunctions

import scala.io.Source

object LongFileSorter extends App {
  if (args.length != 3) {
    throw new IllegalArgumentException("you have to pass three arguments: file to sort, sort type, sorter type")
  }

  val rawFile = new File(args(0))

  if (!rawFile.canRead) {
    throw  new IllegalArgumentException(s"I can not read this file: ${args(0)}!")
  }

  val sortingConfig = SortingConfig.getConfig
  val sortFunction: (String, String) => Boolean = args(1) match {
    case "reverseNoCase" => SortFunctions.iDesc
    case "reverse" => SortFunctions.desc
    case "ascendingNoCase" => SortFunctions.iAsc
    case "ascending" => SortFunctions.asc
    case "length" => SortFunctions.byLength
    case _ => SortFunctions.asc
  }
  val lineCleaner: String => String = _.trim

  val resultName = rawFile.getParent + File.separator +  "sorted-" + rawFile.getName
  val resultWriter = new PrintWriter(new File(resultName))

  val sorter: Sorter = args(1) match {
    case "stream" =>
      val streamSorter = new StreamSorter (
        id = rawFile.getName,
        sortingFunction = sortFunction,
        lineCleaner = lineCleaner,
        config = sortingConfig
      )
      val reader = Source.fromFile (rawFile.getAbsoluteFile, sortingConfig.textEncoding).getLines ()
      while (reader.hasNext) {
        streamSorter.processLine (reader.next ())
      }
      streamSorter.sortLongFile()
      streamSorter
    case "fastStream" =>
      val fastStreamSorter = new FastStreamSorter (
        id = rawFile.getName,
        sortingFunction = sortFunction,
        lineCleaner = lineCleaner,
        config = sortingConfig
      )
      val reader = Source.fromFile (rawFile.getAbsoluteFile, sortingConfig.textEncoding).getLines ()
      while (reader.hasNext) {
        fastStreamSorter.processLine (reader.next ())
      }
      fastStreamSorter.sortLongFile()
      fastStreamSorter
    case _ =>
      val localSorter = new LocalSorter (
        id = rawFile.getName,
        filePath = rawFile.getAbsolutePath,
        lineCleaner = lineCleaner,
        sortingFunction = sortFunction,
        config = sortingConfig
      )
      localSorter.sortLongFile()
      localSorter
  }

  while (sorter.hasNextLine) {
    resultWriter.println(sorter.getLine)
  }

  resultWriter.close()
  println(s"sorted file is saved at $resultName")
}


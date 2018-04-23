package joom.longsort.runners

import java.io.{File, PrintWriter}

import joom.longsort.local.{LocalSorter, StreamSorter}
import joom.longsort.{Sorter, SortingConfig}

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
    case "reverseNoCase" => (x, y) => x.trim.toLowerCase > y.trim.toLowerCase
    case "reverse" => (x, y) => x.trim > y.trim
    case "ascendingNoCase" => (x, y) => x.trim.toLowerCase < y.trim.toLowerCase
    case "ascending" => (x, y) => x.trim < y.trim
    case "length" => (x, y) => x.length < y.length
    case _ => (x, y) => x.trim.toLowerCase < y.trim.toLowerCase
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


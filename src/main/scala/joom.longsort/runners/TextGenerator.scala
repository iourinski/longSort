package joom.longsort.runners

import java.io.{File, PrintWriter}

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random

object TextGenerator extends App {
  if (args.length != 3) {
    throw new IllegalArgumentException("I need three agruments: seed file, number lines and result path")
  }
  var seedFile = if(new File(args(0)).canRead) {
    new File(args(0))}
  else {
    new File(getClass.getClassLoader.getResource(args(0)).getPath)
  }

  if (!seedFile.canRead) {
    throw new IllegalArgumentException(
      s"I can not read ${args(0)}, you need to either pass full path" +
      s" or put it in resources folder"
    )
  }
  // assuming that we use utf-8 files for seed
  val dictionary = Source
    .fromFile(seedFile, "utf-8")
    .getLines()
    .toList
    .flatMap(_.split("\\W+"))
    .filter("\\S+".r.findAllIn(_).nonEmpty)
    .distinct

  val rnd = new Random(System.currentTimeMillis())
  val dictLength = dictionary.length
  val res = new File(args(2))
  val writer = new PrintWriter(res)
  for (i <- 0 to args(1).toInt) {
    val numWords = rnd.nextInt(15)  // say less than 15 words per line
    val line = ListBuffer[String]()
    for (j <- 0 to numWords) {
      val wordIndex = rnd.nextInt(dictLength)
      line.+=(dictionary(wordIndex))
    }
    writer.println(line.mkString(" "))
  }
  writer.close()
  println(s"the resulting file in at ${res.getAbsolutePath}")
}

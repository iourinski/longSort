package joom.longsort.models

import java.io.File

import scala.io.Source

class FileIterator(fileNames: List[PartitionInfo], encoding: String) extends PartitionIterator[String]{
  private val readers = fileNames
    .map(x => {
      val file = new File(x.sortFileName)
      file.deleteOnExit()
      Source.fromFile(file, encoding).getLines()
    })
    .filter(_.hasNext)

  def getInitBuffer(sortingFunction: (String, String) => Boolean): List[(Int, String)] =
    readers
      .zipWithIndex
      .map(x => (x._2, readers(x._2).next()))
      .sortWith((a, b) => sortingFunction(a._2, b._2))

  def hasContent: Boolean = readers.exists(_.hasNext)

  def getNext(readerIndex: Int): String = {
    if (readers(readerIndex).hasNext) {
      readers(readerIndex).next()
    } else {
      null
    }
  }

  def hasNext(readerIndex: Int): Boolean = readers(readerIndex).hasNext

}

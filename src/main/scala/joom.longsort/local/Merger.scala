package joom.longsort.local

import java.io.File

import scala.io.Source

class Merger(
  sortingFunction: (String, String) => Boolean = (a, b) => a < b,
  val fileNames: List[PartitionInfo],
  encoding: String
) {
  private lazy val readers = fileNames
    .map(x => {
      val file = new File(x.sortFileName)
      file.deleteOnExit()
      Source.fromFile(file, encoding).getLines()
    })
    .filter(_.hasNext)

  private var buffer = readers
    .zipWithIndex
    .map(x => (x._2, readers(x._2).next()))
    .sortWith((a, b) => sortingFunction(a._2, b._2))

  def hasNext: Boolean = buffer.nonEmpty

  def getNextLine: String = {
    if (buffer.isEmpty) {
      return null
    }
    val head = buffer.head
    buffer = buffer.tail
    if (readers.exists(_.hasNext)) {
      if (readers (head._1).hasNext) {
        var tmp = readers(head._1).next()
        if (sortingFunction(tmp, if (buffer.nonEmpty) buffer.head._2 else tmp)) {
          buffer = (head._1, tmp) :: buffer
        } else {
          buffer = (buffer :+ (head._1, tmp)).sortWith ((a, b) => sortingFunction (a._2, b._2))
        }
      }
    }
    head._2
  }
}

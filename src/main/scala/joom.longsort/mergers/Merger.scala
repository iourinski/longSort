package joom.longsort.mergers

import joom.longsort.models.{FileIterator, PartitionInfo}

class Merger(
  sortingFunction: (String, String) => Boolean = (a, b) => a < b,
  val fileNames: List[PartitionInfo],
  encoding: String
) extends UniversalMerger[String] {
  private lazy val fileReaders = new FileIterator(fileNames, encoding)

  private var buffer = fileReaders.getInitBuffer(sortingFunction)

  def hasNext: Boolean = buffer.nonEmpty

  def getNextLine: String = {
    if (buffer.isEmpty) {
      return null
    }
    val head = buffer.head
    buffer = buffer.tail
    if (fileReaders.hasContent) {
      if (fileReaders.hasNext(head._1)) {
        var tmp = fileReaders.getNext(head._1)
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

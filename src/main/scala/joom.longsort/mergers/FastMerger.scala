package joom.longsort.mergers

import joom.longsort.models.{FileIterator, LinkedList, PartitionInfo, UniversalNode}

class FastMerger (
  sortingFunction: (String, String) => Boolean = (a, b) => a < b,
  val fileNames: List[PartitionInfo],
  encoding: String
) extends UniversalMerger[String] {
  private val fileReaders = new FileIterator(fileNames, encoding)

  private val buffer = fileReaders.getInitBuffer(sortingFunction)
  private val localSortFunction: ((Int, String), (Int, String)) => Boolean = (a, b) => sortingFunction(a._2, b._2)
  private val linkListBuffer = new LinkedList[(Int, String)](localSortFunction)

  for (element <- buffer) {
    val node = new UniversalNode[(Int, String)](element)
    linkListBuffer.addNode(node)
  }

  def hasNext: Boolean = linkListBuffer.head != null

  def getNextLine: String = {
    val res = linkListBuffer.popHead
    var newLine = fileReaders.getNext(res.value._1)
    if (newLine == null && fileReaders.hasContent) {
      var runner = linkListBuffer.head
      while (runner.next != null && newLine == null) {
        newLine = fileReaders.getNext(runner.value._1)
        runner = runner.next
      }
      if (newLine == null) {
        newLine = fileReaders.getNext(runner.value._1)
      }
      if (newLine != null) {
        val newNode = new UniversalNode[(Int, String)](runner.value._1, newLine)
        linkListBuffer.addNode(newNode)
      }
    } else if (newLine != null) {
      val newNode = new UniversalNode[(Int, String)](res.value._1, newLine)
      linkListBuffer.addNode(newNode)
    }
    res.value._2
  }
}

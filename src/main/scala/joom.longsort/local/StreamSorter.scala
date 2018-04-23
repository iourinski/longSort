package joom.longsort.local

import java.io.{File, PrintWriter}

import joom.longsort.{Sorter, SortingConfig}

import scala.collection.mutable.ListBuffer
import scala.io.Source

class StreamSorter(
  id: String,
  lineCleaner: String => String = _.trim,
  sortingFunction: (String, String) => Boolean = (a, b) => a < b,
  config: SortingConfig
) extends Sorter {

  private var counter: Int = 0
  private var partitionNumber = 0
  private val ts = System.currentTimeMillis()

  private var partitions = ListBuffer[PartitionInfo](getPartitionNames(0))
  private var partitionWriter = new PrintWriter(new File(partitions.last.rawFileName))

  private lazy val merger = new Merger(sortingFunction, partitions.toList, config.textEncoding)

  private def getPartitionNames(num: Int): PartitionInfo = {
    val prefix = config.tmpDir + File.separator + id + "-" + ts.toString
    val rawName = prefix + s"-part-$partitionNumber"
    val sortName = prefix + s"-part-$partitionNumber-sorted"
    PartitionInfo(rawName, sortName)
  }
  override def getLine: String = merger.getNextLine

  override def hasNextLine: Boolean = merger.hasNext

  override def sortLongFile(): Unit = {
    partitionWriter.close()
    sortPartition()
  }

  def processLine(line: String): Unit = {
    counter = counter + 1
    if (counter > config.partitionSize) {
      partitionWriter.close()
      sortPartition()
      partitionNumber = partitionNumber + 1
      val newFiles = getPartitionNames(partitionNumber)
      partitions = partitions :+ newFiles
      partitionWriter = new PrintWriter(newFiles.rawFileName)
      counter = 0
    }
    if (config.lineFilter(line)) {
      partitionWriter.println(lineCleaner(line))
    }
  }

  private def sortPartition(): Unit = {
    val sortFile = new File(partitions.last.sortFileName)
    val sortWriter = new PrintWriter(sortFile)
    Source
      .fromFile(new File(partitions.last.rawFileName))
      .getLines()
      .toList
      .sortWith(sortingFunction)
      .foreach(sortWriter.println(_))
    sortWriter.close()
    val rawFile = new File(partitions.last.rawFileName)
    rawFile.deleteOnExit()
  }
}

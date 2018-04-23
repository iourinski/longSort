package joom.longsort.local

import java.io.{File, PrintWriter}

import joom.longsort.{Sorter, SortingConfig}

import scala.io.Source

case class PartitionInfo(rawFileName: String, sortFileName: String)

class LocalSorter (
  id: String,
  filePath: String,
  lineCleaner: String => String = _.trim,
  sortingFunction: (String, String) => Boolean = (a, b) => a < b,
  config: SortingConfig
) extends Sorter {
  lazy val file = new File(filePath)
  private lazy val numPartitions = getNumberPartitions
  private lazy val partitions = createPartitions(numPartitions)
  private lazy val merger = new Merger(sortingFunction, partitions, config.textEncoding)
  private var counter: Long = 0
  private lazy val myPartitionWriters = partitions.map(x => new PrintWriter(new File(x.rawFileName)))

  override def sortLongFile(): Unit = {
    writePartitions()
    sortPartitions()
  }

  override def hasNextLine: Boolean = merger.hasNext

  override def getLine: String = merger.getNextLine

  private def getNumberPartitions: Int = {
    val runtime = Runtime.getRuntime
    val fileLength = file.length()
    val availableMemory = runtime.freeMemory()
    ((fileLength * 2) / (availableMemory * config.memoryCoefficient)).toInt + 1
  }

  private def createPartitions(numFiles: Int): List[PartitionInfo] = {
    val resourceDir = config.tmpDir
    if (!new File(resourceDir).exists) {
      throw new IllegalArgumentException(s"Can't write to ${config.tmpDir}")
    }
    (0 until numFiles).map(
      x => {
        val rawFileName = resourceDir + File.separator + "part-" + x + "-" + file.getName
        val locFile = new File(rawFileName)
        val sortFileName = resourceDir + File.separator + "sort-" + x + "-" + file.getName
        lazy val sortLocFile = new File(sortFileName)
        PartitionInfo(
          rawFileName = rawFileName,
          sortFileName = sortFileName
        )
      }
    ).toList
  }

  private def writePartitions(): Unit = {
    val fileInputIterator = Source.fromFile(file).getLines()
    while (fileInputIterator.hasNext) {
      val line = fileInputIterator.next()
      if (config.lineFilter(line)) {
        myPartitionWriters ((counter % numPartitions).toInt).println (lineCleaner(line))
        counter = counter + 1
      }
    }
    myPartitionWriters.foreach(_.close())
  }

  private def sortPartitions(): Unit = {
    for (partition <- partitions) {
      val partFile = new File (partition.sortFileName)
      val sortWriter = new PrintWriter (partFile)
      Source
        .fromFile (partition.rawFileName, config.textEncoding)
        .getLines ()
        .toList
        .sortWith (sortingFunction)
        .foreach (sortWriter.println (_))
      sortWriter.close ()
      partFile.deleteOnExit()
      val rawFile = new File(partition.rawFileName)
      rawFile.deleteOnExit()
    }
  }
}

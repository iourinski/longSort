package joom.longsort
import com.typesafe.config._

case class SortingConfig(
  lineFilter: String => Boolean,
  textEncoding: String,
  partitionSize: Int,
  memoryCoefficient: Double,
  tmpDir: String
)

object SortingConfig {
  private val config = ConfigFactory.load()

  lazy val lineFilter: String => Boolean = {
    x => config.getString ("text.filtering.regex").r.findAllIn (x).nonEmpty
  }

  lazy val textEncoding: String = config.getString("text.encoding")

  lazy val partitionSize: Int  = config.getInt("partitions.number.lines")
  lazy val memoryCoefficient: Double = config.getDouble("partitions.memory.coefficient")
  lazy val tmpDir: String = config.getString("partitions.temp.dir")

  def getConfig: SortingConfig = SortingConfig(lineFilter, textEncoding, partitionSize, memoryCoefficient, tmpDir)

}

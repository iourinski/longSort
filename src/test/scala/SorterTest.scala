import joom.longsort.models.SortingConfig
import joom.longsort.sorters.{FastStreamSorter, LocalSorter, StreamSorter}
import joom.longsort.utils.SortFunctions
import org.scalatest.FunSuite

import scala.collection.mutable.ListBuffer
import scala.io.Source



class SorterTest extends  FunSuite {
  val config = SortingConfig.getConfig
  val randomFile = getClass.getClassLoader.getResource("randomText1.txt")
  val lineCleaner: String => String = _.trim

  test("all three sorters produce the same output") {
    val sortingFunctions = List(
      SortFunctions.asc, SortFunctions.iAsc, SortFunctions.desc, SortFunctions.iDesc, SortFunctions.byLength
    )

    val lineIterator = Source.fromFile(randomFile.getFile).getLines()
    val res = sortingFunctions
      .map(x => {
        val localSorter = new LocalSorter("local", randomFile.getFile, lineCleaner, x, config)
        val stream = new StreamSorter("stream", lineCleaner, x, config)
        val fastStream = new FastStreamSorter("fastStream", lineCleaner, x, config)
        (localSorter, stream, fastStream)
      })
      .map(x => {
        x._1.sortLongFile()
        while (lineIterator.hasNext) {
          val line = lineIterator.next()
          x._2.processLine(line)
          x._2.processLine(line)
        }
        x._2.sortLongFile()
        x._3.sortLongFile()
        val localSorted = new ListBuffer[String]()
        while (x._1.hasNextLine) localSorted.+=(x._1.getLine)
        val streamSorted = new ListBuffer[String]()
        while (x._2.hasNextLine) streamSorted.+=(x._2.getLine)
        val fastStreamSorted = new ListBuffer[String]()
        while (x._3.hasNextLine) fastStreamSorted.+=(x._3.getLine)
        (localSorted.toList, streamSorted.toList, fastStreamSorted.toList)
      })

    for (triple <- res) {
      val elements = triple._1.zip(triple._2).zip(triple._3)
        .exists(x => List(x._1._1, x._1._2, x._2).distinct.length != 1)
      assume(!elements)
    }
  }

  test("we can trim lines and ignore empty ones") {
    val sorter = new LocalSorter("local", randomFile.getFile, lineCleaner, SortFunctions.asc, config)
    val sortedWithTrim =
      """Entappuah goad rage bearest Arnon hasteneth plantings venison misused ransomed keys bulrush rested wash gap
        |Kadmiel edge Meshelemiah hurt Gileadites Behold heresy overwhelm
        |Laodicea Elonbethhanan Zilthai invalidity fastest roots License Hararite Tryphena Zorathites lentiles What
        |Shelumiel raise oars fowls DONATIONS Hagarenes adjured wrung terrible scatter Enhaddah Pamphylia Enrogel Jebusite
        |askest leather Berothai Eltekon doer hoped scarlet
        |branch strengtheneth Bileam promised purposing craft sorcery
        |covenants delighted concubines hospitality spears omers Draw Gomer calamity contained Ezri slumber boastings Hattil
        |discourage rejoicest Zorah Provideth maimed abated doings purposed Tiras drown plat
        |forgetteth skill rideth Mahath bid Zererath lift Salamis Agur Hadar triumph
        |gathereth Hazazontamar greatness marriage Hephzibah approach proper Methuselah ruins irons purses Aznothtabor
        |prosperous Jezerites Esrom flanks boiled mad Ziph hammers wanderers Maath
        |punished overtake much whensoever brotherhood saluteth
        |resurrection Shuphamites Joshaphat lies brayed Whereupon Baalim harden Megiddon eighteenth judges twenty pommels burnt jacinth
        |sacrilege 43 Succothbenoth differing Senir earring joint Harbonah Noadiah Titus seeketh
        |shut robber moveable delightsome
        |size Aroer fellowship ferry Immediately publicans They brokenhanded Messias prolonged instantly talents Elimelech proceed spiritual
        |tokens Ahasbai Confounded hope stayed previous foreordained hungry engrave meaning
        |warp meadows disinherit darkeneth Merathaim deviseth appeaseth""".stripMargin
    sorter.sortLongFile()
    val sorted = ListBuffer[String]()
    while(sorter.hasNextLine) sorted.+=(sorter.getLine)
    assert(sorted.mkString("\n") == sortedWithTrim)
  }
  test("if we sort the same file without a trim the result is different") {
    val noCleaner: String => String = x => x
    val sorter = new LocalSorter(
      id = "local",
      filePath = randomFile.getFile,
      lineCleaner = noCleaner,
      sortingFunction = SortFunctions.asc,
      config = config
    )
    sorter.sortLongFile()
    val sorted = ListBuffer[String]()
    val sortedWithoutTrim =
      """   Laodicea Elonbethhanan Zilthai invalidity fastest roots License Hararite Tryphena Zorathites lentiles What
        |   discourage rejoicest Zorah Provideth maimed abated doings purposed Tiras drown plat
        |Entappuah goad rage bearest Arnon hasteneth plantings venison misused ransomed keys bulrush rested wash gap
        |Kadmiel edge Meshelemiah hurt Gileadites Behold heresy overwhelm
        |Shelumiel raise oars fowls DONATIONS Hagarenes adjured wrung terrible scatter Enhaddah Pamphylia Enrogel Jebusite
        |askest leather Berothai Eltekon doer hoped scarlet
        |branch strengtheneth Bileam promised purposing craft sorcery
        |covenants delighted concubines hospitality spears omers Draw Gomer calamity contained Ezri slumber boastings Hattil
        |forgetteth skill rideth Mahath bid Zererath lift Salamis Agur Hadar triumph
        |gathereth Hazazontamar greatness marriage Hephzibah approach proper Methuselah ruins irons purses Aznothtabor
        |prosperous Jezerites Esrom flanks boiled mad Ziph hammers wanderers Maath
        |punished overtake much whensoever brotherhood saluteth
        |resurrection Shuphamites Joshaphat lies brayed Whereupon Baalim harden Megiddon eighteenth judges twenty pommels burnt jacinth
        |sacrilege 43 Succothbenoth differing Senir earring joint Harbonah Noadiah Titus seeketh
        |shut robber moveable delightsome
        |size Aroer fellowship ferry Immediately publicans They brokenhanded Messias prolonged instantly talents Elimelech proceed spiritual
        |tokens Ahasbai Confounded hope stayed previous foreordained hungry engrave meaning
        |warp meadows disinherit darkeneth Merathaim deviseth appeaseth""".stripMargin
    while(sorter.hasNextLine) sorted.+=(sorter.getLine)
    assert(sorted.mkString("\n") == sortedWithoutTrim)
  }
  test("we can sort only some particular lines, say prefixed by #") {
    val newConfig = config.copy(lineFilter = x => "^#".r.findAllIn(x).nonEmpty)
    val fileWithComments = getClass.getClassLoader.getResource("randomText2.txt")

    val sorter = new LocalSorter(
      id = "local",
      filePath = fileWithComments.getFile,
      lineCleaner = lineCleaner,
      sortingFunction = SortFunctions.asc,
      config = newConfig
    )
    sorter.sortLongFile()
    val sorted = ListBuffer[String]()
    val sortedComments =
      """# another comment
        |# or they can be the only lines to be sorted
        |# these are comments that may not be included in result""".stripMargin

    while(sorter.hasNextLine) sorted.+=(sorter.getLine)
    assert(sortedComments == sorted.mkString("\n"))
  }
}

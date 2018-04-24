import joom.longsort.mergers.{FastMerger, Merger}
import joom.longsort.models.{PartitionInfo, SortingConfig}
import joom.longsort.sorters.LocalSorter
import joom.longsort.utils.SortFunctions
import org.scalatest.FunSuite

import scala.collection.mutable.ListBuffer
import scala.io.Source

class MergerTest extends FunSuite {
  val config = SortingConfig.getConfig

  def getResourcePath(name: String): String = getClass.getClassLoader.getResource(name).getFile

  val file1 = getResourcePath("randomText1.txt")
  val file2 = getResourcePath("randomText2.txt")

  val file1sort = getResourcePath("sortText1.txt")
  val file2sort = getResourcePath("sortText2.txt")

  val sortFct = SortFunctions.byLength

  val lines1 = Source.fromFile(file1, "utf-8").getLines().toList.sortWith(sortFct)
  val lines2 = Source.fromFile(file2, "utf-8").getLines().toList.sortWith(sortFct)

  test("different mergers merge identically") {
    val sorter = new LocalSorter(
      id = "local",
      filePath = file2,
      lineCleaner = _.trim,
      sortingFunction = SortFunctions.byLength,
      config = config
    )
    sorter.sortLongFile()
    val partitions = List(
      PartitionInfo(file1, file1sort),
      PartitionInfo(file2, file2sort)
    )
    val merger = new Merger(sortFct, partitions, config.textEncoding)
    val fastMerger = new FastMerger(sortFct, partitions, config.textEncoding)

    val sortedText =
      """cornet
        |Zacher 000
        |prophecy profit
        |equipment Zemira
        |sicknesses softly
        |# another comment
        |shadowing Reward 63
        |proportion unholy Ard
        |paperwork breadth fathoms Shem
        |shut robber moveable delightsome
        |# or they can be the only lines to be sorted
        |askest leather Berothai Eltekon doer hoped scarlet
        |Both gather brotherhood brick placed mighties rebuketh
        |punished overtake much whensoever brotherhood saluteth
        |# these are comments that may not be included in result
        |dimness take whomsoever Hermonites dip Jarib Understand
        |Hearing despaired cliffs Theophilus recall struggled trieth
        |branch strengtheneth Bileam promised purposing craft sorcery
        |warp meadows disinherit darkeneth Merathaim deviseth appeaseth
        |prosper unsearchable Defend eschew loft execute wonderful thief
        |Kadmiel edge Meshelemiah hurt Gileadites Behold heresy overwhelm
        |Namely equity persecute Sinim prognosticators roebucks Eliphaz Soul Azal
        |prosperous Jezerites Esrom flanks boiled mad Ziph hammers wanderers Maath
        |revellings impoverished 051 Adina Are landing Adonikam dissemblers kindly
        |forgetteth skill rideth Mahath bid Zererath lift Salamis Agur Hadar triumph
        |silk viper Janohah Prepare Ezel Socoh Baali Jeshanah accepting Men saying mixture
        |tokens Ahasbai Confounded hope stayed previous foreordained hungry engrave meaning
        |discourage rejoicest Zorah Provideth maimed abated doings purposed Tiras drown plat
        |sacrilege 43 Succothbenoth differing Senir earring joint Harbonah Noadiah Titus seeketh
        |pit Sirs boar ointments afar Any trembled stretched Drusilla officers sour negligent litters non
        |Laodicea Elonbethhanan Zilthai invalidity fastest roots License Hararite Tryphena Zorathites lentiles What
        |Entappuah goad rage bearest Arnon hasteneth plantings venison misused ransomed keys bulrush rested wash gap
        |gathereth Hazazontamar greatness marriage Hephzibah approach proper Methuselah ruins irons purses Aznothtabor
        |Castor change abominably ospray Arcturus Caesarea brawlers praising compel axe advantage India Carmi 39 reapers
        |Shelumiel raise oars fowls DONATIONS Hagarenes adjured wrung terrible scatter Enhaddah Pamphylia Enrogel Jebusite
        |Sidonians Study surety provided prepareth Onam 168 brimstone softly Lahmi uncorruptness fellowheirs gutter swooned
        |covenants delighted concubines hospitality spears omers Draw Gomer calamity contained Ezri slumber boastings Hattil
        |resurrection Shuphamites Joshaphat lies brayed Whereupon Baalim harden Megiddon eighteenth judges twenty pommels burnt jacinth
        |size Aroer fellowship ferry Immediately publicans They brokenhanded Messias prolonged instantly talents Elimelech proceed spiritual""".stripMargin
    val mergerSorted = ListBuffer[String]()
    while (merger.hasNext) mergerSorted.+=(merger.getNextLine)
    val fastMergerSorted = ListBuffer[String]()
    while (fastMerger.hasNext) fastMergerSorted.+=(fastMerger.getNextLine)
    assert(fastMergerSorted.mkString("\n") == sortedText)
    assert(mergerSorted.mkString("\n") == sortedText)
  }
}

package joom.longsort.sorters

trait Sorter {
  def sortLongFile(): Unit
  def getLine: String
  def hasNextLine: Boolean
}

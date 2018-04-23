package joom.longsort

trait Sorter {
  def sortLongFile(): Unit
  def getLine: String
  def hasNextLine: Boolean
}

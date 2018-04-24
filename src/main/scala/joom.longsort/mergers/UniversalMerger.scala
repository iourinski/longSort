package joom.longsort.mergers

trait UniversalMerger[T] {
  def hasNext: Boolean
  def getNextLine: T
}

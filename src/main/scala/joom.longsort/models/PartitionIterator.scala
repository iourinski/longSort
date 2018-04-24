package joom.longsort.models

trait PartitionIterator[T] {
  def getInitBuffer(sortFunction: (T, T) => Boolean): List[(Int, T)]
  def hasContent: Boolean
  def getNext(readerIndex: Int): T
  def hasNext(readerIndex: Int): Boolean
}

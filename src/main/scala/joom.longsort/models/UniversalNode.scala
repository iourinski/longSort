package joom.longsort.models

class UniversalNode[T](val value: T) {
  var next: UniversalNode[T] = _

  def setNext(updatedNext: UniversalNode[T]): Unit = {
    next = updatedNext
  }
}

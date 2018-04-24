package joom.longsort.models

class LinkedList[T] (sortFunction: (T, T) => Boolean) {
  var head: UniversalNode[T] = _
  def addNode(node: UniversalNode[T]): Unit = {
    if (this.head == null) {
      head = node
    } else if (node.value != null) {
      var runner = head
      if (sortFunction(node.value, runner.value)) {
        node.setNext(this.head)
        this.head = node
      } else {
        while (runner.next != null && sortFunction (node.value, runner.value)) {
          runner = runner.next
        }
        node.setNext (runner.next)
        runner.setNext (node)
      }
    }
  }

  def popHead: UniversalNode[T] = {
    val tmp = head
    head = head.next
    tmp
  }
}

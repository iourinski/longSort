package joom.longsort.utils

object SortFunctions {
  val iDesc: (String, String) => Boolean = (x, y) => x.toLowerCase > y.trim.toLowerCase
  val desc: (String, String) => Boolean = (x, y) => x > y
  val iAsc: (String, String) => Boolean = (x, y) => x.toLowerCase < y.toLowerCase
  val asc: (String, String) => Boolean = (x, y) => x < y
  val byLength:(String, String) => Boolean = (x, y) => x.length < y.length
}

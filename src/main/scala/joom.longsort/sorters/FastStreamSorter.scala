package joom.longsort.sorters

import joom.longsort.mergers.FastMerger
import joom.longsort.models.SortingConfig

class FastStreamSorter (
  id: String,
  lineCleaner: String => String = _.trim,
  sortingFunction: (String, String) => Boolean = (a, b) => a < b,
  config: SortingConfig
) extends StreamSorter(id, lineCleaner, sortingFunction, config) {
  private lazy val merger = new FastMerger(sortingFunction, partitions.toList, config.textEncoding)
}

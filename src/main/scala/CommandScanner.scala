package escli

import scala.collection.AbstractIterator

class CommandScanner(source: Iterator[String]) extends AbstractIterator[String] {
   
    def hasNext = source.hasNext

    def next() = if (source.hasNext) {
      var res = ""
      while (!res.endsWith(";") && source.hasNext) {
        res = res + source.next()
      }
      res
    } else Iterator.empty.next()
  
}

object CommandScanner {
  def apply(source: Iterator[String]) = new CommandScanner(source)
}

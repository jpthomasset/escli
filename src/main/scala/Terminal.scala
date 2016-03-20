package escli

import jline.console.ConsoleReader
import jline.TerminalFactory
import scala.collection.AbstractIterator

class Terminal(val prompt: String) {

  val t = TerminalFactory.get()
  t.init()
  t.setEchoEnabled(true)
  
  val reader = new ConsoleReader()
  reader.setPrompt(prompt)
  
  def scan(): Iterator[String] = new AbstractIterator[String] {
    val source = Iterator
      .continually(reader.readLine())
      .takeWhile(_ != null)

    def hasNext = source.hasNext

    def next() = if (source.hasNext) {
      var res = ""
      while (!res.endsWith(";") && source.hasNext) {
        res = res + source.next()
      }
      res
    } else Iterator.empty.next()
  }

  
  def shutdown() = {
    TerminalFactory.get().restore()
  }
}

object Terminal extends Terminal("escli> ")

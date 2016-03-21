package escli

import jline.console.ConsoleReader
import jline.TerminalFactory


class Terminal(val prompt: String) {

  val t = TerminalFactory.get()
  t.init()
  t.setEchoEnabled(true)
  
  val reader = new ConsoleReader()
  reader.setPrompt(prompt)
  
  def scan(): Iterator[String] =
    Iterator
      .continually(reader.readLine())
      .takeWhile(_ != null)
}

object Terminal {
  def apply(prompt: String) = new Terminal(prompt)

  def shutdown() = {
    TerminalFactory.get().restore()
  }
}

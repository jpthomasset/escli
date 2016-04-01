package escli

import jline.console.ConsoleReader
import jline.TerminalFactory


class Terminal(val prompt: String) {

  val reader = new ConsoleReader()
  reader.setExpandEvents(false)
  reader.setBellEnabled(false)
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

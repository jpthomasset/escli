package escli

import jline.console.ConsoleReader
import jline.TerminalFactory

class CommandReader(val prompt:String) {

  val t = TerminalFactory.get()
  t.init()
  t.setEchoEnabled(true)
  

  val reader = new ConsoleReader()
  reader.setPrompt(prompt)
  
  def read(handler: (String) => Any) = {  
    Iterator
      .continually( reader.readLine())
      .takeWhile(_ != null)
      .foldLeft("")( (acc, line) => {
         if(line.endsWith(";")) {
           handler(acc + " " +  line)
           reader.setPrompt(prompt)
           ""
         } else {
           reader.setPrompt("  -> ")
          acc + " " + line
        }
      })
  }
}


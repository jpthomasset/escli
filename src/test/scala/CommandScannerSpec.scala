import org.scalatest._

import escli.CommandScanner


class CommandScannerSpec extends WordSpec with Matchers {

  "CommandScanner" should {
    "detect a single line command" in {
      assertResult("some command;") {
        CommandScanner(List("some command;").iterator).next()
      }
    }

    "assemble a multiline command as one command" in {
      assertResult("some command;") {
        CommandScanner(List("some", "command;").iterator).next()
      }
    }

    "detect multiple single line commands" in {
      assertResult(List("some command;", "some other command;")) {
        CommandScanner(List("some command;", "some other command;").iterator).toList
      }
    }

    "detect multiple single and multiple line commands" in {
      assertResult(List("some command;", "some other command;")) {
        CommandScanner(List("some command;", "some", "other", "command;").iterator).toList
      }
    }

    "throw an exception when the source is empty" in {
      an [NoSuchElementException] should be thrownBy CommandScanner(Iterator.empty).next()
     }
  }
}

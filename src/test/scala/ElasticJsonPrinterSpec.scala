import escli.ElasticJson._
import org.scalatest._
import spray.json._

import escli.ElasticJsonPrinter

class ElasticJsonPrinterSpec extends WordSpec with Matchers {

  "patternToRegex" should {
    "escape dot" in {
      ElasticJsonPrinter.patternToRegex(".").regex should be ("""\.""")
    }

    "replace simple star with regex pattern" in {
      ElasticJsonPrinter.patternToRegex("*").regex should be (""".*""")
      }
    }
  
  "print(hits, queryCols)" should {
    val sb = new StringBuilder()
    val printer = new ElasticJsonPrinter(sb.append(_))

    val jsObject = JsObject(("key1" -> JsString("very long value over 40 chars (bla bla bla bla bla bla bla)"))) 
    def hit(n:String) = Hit("index", "type", n, 1, jsObject) 
    val hits = Array(hit("1000"), hit("1001"))
      
    "output nothing when query is empty" in {
      printer.print(Array.empty[Hit], List.empty[String])
      sb.toString() should be("")
    }

    "output simple result" in {
      val expectedResult =
        "+--------+-------+------+-------------------------------------------------------------+\n" +
        "| _index | _type | _id  | key1                                                        |\n" +
        "+--------+-------+------+-------------------------------------------------------------+\n" +
        "| index  | type  | 1000 | very long value over 40 chars (bla bla bla bla bla bla bla) |\n" +
        "| index  | type  | 1001 | very long value over 40 chars (bla bla bla bla bla bla bla) |\n" +
        "+--------+-------+------+-------------------------------------------------------------+\n"

      sb.clear()
      printer.print(hits, List.empty[String])
      sb.toString() should be(expectedResult)
    }

    "output simple result based on column definition" in {
      val expectedResult =
        "+-------------------------------------------------------------+------+\n" +
        "| key1                                                        | key2 |\n" +
        "+-------------------------------------------------------------+------+\n" +
        "| very long value over 40 chars (bla bla bla bla bla bla bla) | null |\n" +
        "| very long value over 40 chars (bla bla bla bla bla bla bla) | null |\n" +
        "+-------------------------------------------------------------+------+\n"

      sb.clear()
      printer.print(hits, "key1" :: "key2" :: Nil)
      sb.toString() should be(expectedResult)
    }

    "ignore wildcard in column definition" in {
      val expectedResult =
        "+-------------------------------------------------------------+\n" +
        "| key1                                                        |\n" +
        "+-------------------------------------------------------------+\n" +
        "| very long value over 40 chars (bla bla bla bla bla bla bla) |\n" +
        "| very long value over 40 chars (bla bla bla bla bla bla bla) |\n" +
        "+-------------------------------------------------------------+\n"

      sb.clear()
      printer.print(hits, "k*" :: "o*" :: Nil)
      sb.toString() should be(expectedResult)
    }

    "match wildcard against meta columns" in {
      val expectedResult =
        "+--------+------+-------------------------------------------------------------+\n" +
        "| _index | _id  | key1                                                        |\n" +
        "+--------+------+-------------------------------------------------------------+\n" +
        "| index  | 1000 | very long value over 40 chars (bla bla bla bla bla bla bla) |\n" +
        "| index  | 1001 | very long value over 40 chars (bla bla bla bla bla bla bla) |\n" +
        "+--------+------+-------------------------------------------------------------+\n"

      sb.clear()
      printer.print(hits, "_i*" :: "k*" :: Nil)
      sb.toString() should be(expectedResult)
    }
  }

}


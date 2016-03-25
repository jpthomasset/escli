import escli.ElasticJson._
import org.scalatest._
import spray.json._

import escli.ElasticJsonPrinter

class ElasticJsonPrinterSpec extends WordSpec with Matchers {

  "jsString" should {

    val printer = new ElasticJsonPrinter(_ => {})

    "convert JsNull to 'null'" in {
      printer.jsString(JsNull) should be("null")
    }

    "convert JsTrue to 'true'" in {
      printer.jsString(JsTrue) should be("true")
    }

    "convert JsFalse to 'false'" in {
      printer.jsString(JsFalse) should be("false")
    }

    "convert JsNumber" in {
      printer.jsString(JsNumber(10.5)) should be("10.5")
    }

    "convert JsString" in {
      printer.jsString(JsString("abcd")) should be("abcd")
    }

    "convert JsArray" in {
      printer.jsString(JsArray(JsString("abcd"), JsString("cdef"))) should be("""["abcd","cdef"]""")
    }

    "convert JsObject(...)" in {
      printer.jsString(JsObject(("key1" -> JsString("value1")))) should be("""{"key1":"value1"}""")
    }
  }

  "jsSize" should {
    val printer = new ElasticJsonPrinter(_ => {})

    "Get size of a JsValue" in {
      val jsValue = JsString("Some value")
      printer.jsSize(jsValue) should equal(10)
    }

    "Trim a JsObject to 40 char max" in {
      val jsObject = JsObject(("key1" -> JsString("very long value over 40 chars (bla bla bla bla bla bla bla)")))
      printer.jsSize(jsObject) should equal(40)
    }
  }

  "columnSize" should {
    val printer = new ElasticJsonPrinter(_ => {})
    "return an empty map when hit does not contain any column" in {
      printer.columnSize(Hit("index", "type", "1", 1, JsArray())) should be (Map.empty[String, Int])
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
        "+-------------------------------------------------------------+\n" +
        "| key1                                                        |\n" +
        "+-------------------------------------------------------------+\n" +
        "| very long value over 40 chars (bla bla bla bla bla bla bla) |\n" +
        "| very long value over 40 chars (bla bla bla bla bla bla bla) |\n" +
        "+-------------------------------------------------------------+\n"

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
  }

}


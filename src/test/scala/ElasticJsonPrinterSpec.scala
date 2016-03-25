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

  "print(hits, queryCols)" should {
    val sb = new StringBuilder()
    val printer = new ElasticJsonPrinter(sb.append(_))

    "output nothing when query is empty" in {
      printer.print(Array.empty[Hit], List.empty[String])
      sb.toString() should be("")
    }
  }

}


import org.scalatest._
import spray.json._

import escli.JsonUtil._




class JsonUtilSpec extends WordSpec with Matchers {

  "stringOf" should {

    "convert JsNull to 'null'" in {
      stringOf(JsNull) should be("null")
    }

    "convert JsTrue to 'true'" in {
      stringOf(JsTrue) should be("true")
    }

    "convert JsFalse to 'false'" in {
      stringOf(JsFalse) should be("false")
    }

    "convert JsNumber" in {
      stringOf(JsNumber(10.5)) should be("10.5")
    }

    "convert JsString" in {
      stringOf(JsString("abcd")) should be("abcd")
    }

    "convert JsArray" in {
      stringOf(JsArray(JsString("abcd"), JsString("cdef"))) should be("""["abcd","cdef"]""")
    }

    "convert JsObject(...)" in {
      stringOf(JsObject(("key1" -> JsString("value1")))) should be("""{"key1":"value1"}""")
    }
  }
  }

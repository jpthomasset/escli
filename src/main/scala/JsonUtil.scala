package escli

import spray.json._

object JsonUtil {

    def stringOf(x: JsValue): String = x match {
    case JsObject(_) => x.toString()
    case JsArray(_) => x.toString()
    case JsNull => "null"
    case JsTrue => "true"
    case JsFalse => "false"
    case JsNumber(x) => x.toString()
    case JsString(x) => x
    case _ => throw new IllegalStateException
    }

  }

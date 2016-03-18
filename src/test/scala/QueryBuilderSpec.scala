import org.scalatest._

import escli.AST._
import escli.QueryBuilder

class QueryBuilderSpec extends WordSpec with Matchers {

  "QueryBuilder" should {
    "build an url path based on given index name" in {
      assertResult("/someindex/_search") {
        QueryBuilder.build(Source("someindex", None))
      }
    }

    "build an url path based on given index name with type info" in {
      assertResult("/someindex/doctype/_search") {
        QueryBuilder.build(Source("someindex", Some("doctype")))
      }
    }

    "build an array of string as field list" in {
      val b = QueryBuilder.build(Fields("field1" :: "field2" :: Nil))
      b should be defined

    }
  }
}

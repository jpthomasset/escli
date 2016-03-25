import org.scalatest._
import org.scalatest.OptionValues._

import escli.AST._
import escli.ElasticJson._
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
      b.value should be(Array("field1", "field2"))

    }

    "build an empty list as field list when all fields are queried" in {
      val b = QueryBuilder.build(AllFields())
      b should not be defined

    }

    "build a request based on a select query" in {
      assertResult(Some(Request("/someindex/_search", RequestBody(None, None, None)))) {
        QueryBuilder.build(Select(AllFields(), Source("someindex", None)))
      }
    }

    "build a request based on a select query with limit" in {
      assertResult(Some(Request("/someindex/_search", RequestBody(None, Some(2), None)))) {
        QueryBuilder.build(Select(AllFields(), Source("someindex", None), Some(Limit(2))))
      }
    }

    "ignore an empty query" in {
      assertResult(None) {
        QueryBuilder.build(Empty())
      }
    }
  }
}

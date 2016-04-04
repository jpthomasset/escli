import org.scalatest._

import spray.json._

import escli.ElasticJson._
import escli.ElasticJsonProtocol._

class ElasticJsonSpec extends WordSpec with Matchers {

  val jsonShard = """{"total":5,"successful":5,"failed":0}"""

  val source = """{
          "user" : "kimchy",
          "post_date" : "2009-11-15T14:12:12",
          "message" : "trying out Elasticsearch"
        }"""

  val jsonHit = """{"_index":"twitter","_type":"tweet","_id":"1","_score":1.0,"_source":""" + source + """}"""

  val jsonHits = """{"total":1,"max_score":1.0,"hits":[""" + jsonHit + """]}"""

  val jsonResponse = """{"took":1,"timed_out":false,"_shards":""" + jsonShard + """, "hits": """ + jsonHits + """}"""

  val jsonError = """{"error": "IndexMissingException[[account] missing]", "status": 404}"""

  "ElasticJsonProtocol parser" should {
    "parse a ShardInfo json" in {
      assertResult(ShardInfo(5, 5, 0)) {
        jsonShard.parseJson.convertTo[ShardInfo]
      }
    }

    "parse a Hit json" in {
      assertResult(Hit("twitter", "tweet", "1", 1.0, source.parseJson.asJsObject)) {
        jsonHit.parseJson.convertTo[Hit]
      }
    }

    "parse a Hits json" in {
      val hits = jsonHits.parseJson.convertTo[Hits]
      hits should have {
        'total(1)
        'max_score(1.0)
      }

      hits.hits.length should be(1)
    }

    "parse a SearchResponse json" in {
      val response = jsonResponse.parseJson.convertTo[SearchResponse]
      response should have {
        'took(1)
        'timed_out(false)
      }
    }

    "parse an ErrorResponse json" in {
      val response = jsonError.parseJson.convertTo[ErrorResponse]
      response should have {
        'error("IndexMissingException[[account] missing]")
        'status(404)
      }
    }

    "generate a term query" in {
      val query = TermQuery("field1", "term1")
      query.toJson.toString should be("""{"field1":"term1"}""")
    }

    "generate a generic TypedQuery for term query" in {
      val query = TypedQuery(TermQuery("field1", "term1"))
      query.toJson.toString should be ("""{"term":{"field1":"term1"}}""")
      }

    "generate a terms query" in {
      val query = TermsQuery("field1", List("term1", "term2", "term3", "term4"))
      query.toJson.toString should be("""{"field1":["term1","term2","term3","term4"]}""")
    }

    "generate a generic TypedQuery for terms query" in {
      val query = TypedQuery(TermsQuery("field1", List("term1", "term2", "term3", "term4")))
      query.toJson.toString should be ("""{"terms":{"field1":["term1","term2","term3","term4"]}}""")
      }

    "generate a range query" in {
      val query = RangeQuery("field1", Some(5.0), None, Some(15), None)
      query.toJson.toString should be("""{"field1":{"gte":5.0,"lte":15.0}}""")
    }

    "generate a generic TypedQuery for range query" in {
      val query = TypedQuery(RangeQuery("field1", Some(5.0), None, Some(15), None))
      query.toJson.toString should be ("""{"range":{"field1":{"gte":5.0,"lte":15.0}}}""")
      }

    "generate a bool query" in {
      val query = BoolQuery(
        Some(Array(TypedQuery(TermQuery("field1", "value1")))),
        None,
        Some(Array(TypedQuery(RangeQuery("field2", Some(12.345), None, None, None)))),
        None)

      query.toJson.toString should be ("""{"must":[{"term":{"field1":"value1"}}],"should":[{"range":{"field2":{"gte":12.345}}}]}""")
    }

    "generate a TypedQuery for bool query" in {
      val query = TypedQuery(BoolQuery(
        Some(Array(TypedQuery(TermQuery("field1", "value1")))),
        None,
        Some(Array(TypedQuery(RangeQuery("field2", Some(12.345), None, None, None)))),
        None))

      query.toJson.toString should be ("""{"bool":{"must":[{"term":{"field1":"value1"}}],"should":[{"range":{"field2":{"gte":12.345}}}]}}""")
      }
  }
}

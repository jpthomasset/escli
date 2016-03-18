import org.scalatest._

import spray.json._

import escli.ElasticJson._
import escli.ElasticJsonProtocol._

class ElasticJsonSpec extends WordSpec with Matchers {


  val jsonShard = """{"total":5,"successful":5,"failed":0}""" 

  val source  = """{
          "user" : "kimchy",
          "post_date" : "2009-11-15T14:12:12",
          "message" : "trying out Elasticsearch"
        }"""

  val jsonHit =  """{"_index":"twitter","_type":"tweet","_id":"1","_score":1.0,"_source":""" + source + """}"""

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
      assertResult(Hit("twitter", "tweet", "1", 1.0, source.parseJson)) {
        jsonHit.parseJson.convertTo[Hit]
      }
    }

    "parse a Hits json" in {
      val hits = jsonHits.parseJson.convertTo[Hits] 
      hits should have {
        'total (1)
        'max_score (1.0)
      }

      hits.hits.length should be (1) 
    }

    "parse a SearchResponse json" in {
      val response = jsonResponse.parseJson.convertTo[SearchResponse] 
      response should have {
        'took (1)
        'timed_out (false)
      }
    }

    "parse an ErrorResponse json" in {
      val response = jsonError.parseJson.convertTo[ErrorResponse] 
      response should have {
        'error ("IndexMissingException[[account] missing]")
        'status (404)
      }
    }
  }
}

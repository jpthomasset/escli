package escli

import spray.json.JsValue
import spray.json.DefaultJsonProtocol

object ElasticJson {
  case class ShardInfo(total: Int, successful: Int, failed: Int)
  case class Hit(_index: String, _type: String, _id: String, _score: Double, _source: JsValue)
  case class Hits(total: Long, max_score: Double, hits: Array[Hit])
  case class SearchResponse(took: Int, timed_out: Boolean, _shards: ShardInfo, hits: Hits)
}

object ElasticJsonProtocol extends DefaultJsonProtocol {
  import ElasticJson._
  
  implicit val shardInfoFormat = jsonFormat3(ShardInfo)
  implicit val hitFormat = jsonFormat5(Hit)
  implicit val hitsFormat = jsonFormat3(Hits)
  implicit val elasticResponseFormat = jsonFormat4(SearchResponse)
}

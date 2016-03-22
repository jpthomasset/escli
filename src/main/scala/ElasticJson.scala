package escli

import spray.json.JsValue
import spray.json.DefaultJsonProtocol

object ElasticJson {
  /** Response objects */
  case class ShardInfo(total: Int, successful: Int, failed: Int)
  case class Hit(_index: String, _type: String, _id: String, _score: Double, _source: JsValue)
  case class Hits(total: Long, max_score: Double, hits: Array[Hit])
  case class SearchResponse(took: Long , timed_out: Boolean, _shards: ShardInfo, hits: Hits)
  case class ErrorResponse(error: String, status: Int)

  /** Request objects */
  case class Request(path: String, body: RequestBody)
  case class RequestBody(from: Option[Int], size: Option[Int], _source: Option[Array[String]])
}

object ElasticJsonProtocol extends DefaultJsonProtocol {
  import ElasticJson._

  implicit val shardInfoFormat = jsonFormat3(ShardInfo)
  implicit val hitFormat = jsonFormat5(Hit)
  implicit val hitsFormat = jsonFormat3(Hits)
  implicit val searchResponseFormat = jsonFormat4(SearchResponse)
  implicit val errorResponseFormat = jsonFormat2(ErrorResponse)

  implicit val requestBodyFormat = jsonFormat3(RequestBody)
}

package escli

import spray.json._

object ElasticJson {

  sealed trait ElasticResponse

  /** Response objects */
  case class ShardInfo(total: Int, successful: Int, failed: Int)
  case class Hit(_index: String, _type: String, _id: String, _score: Double, _source: JsValue)
  case class Hits(total: Long, max_score: Double, hits: Array[Hit])
  case class SearchResponse(took: Long, timed_out: Boolean, _shards: ShardInfo, hits: Hits) extends ElasticResponse
  case class ErrorResponse(error: String, status: Int) extends ElasticResponse

  /** Request objects */
  case class Request(path: String, body: RequestBody)
  case class RequestBody(from: Option[Int], size: Option[Int], _source: Option[Array[String]])

  case class TermQuery(field: String, value: String)
  case class TermsQuery(field: String, values: List[String])
  case class RangeQuery(field: String, gte: Option[Double], gt: Option[Double], lte: Option[Double], lt: Option[Double])

}

object ElasticJsonProtocol extends DefaultJsonProtocol {
  import ElasticJson._

  implicit val shardInfoFormat = jsonFormat3(ShardInfo)
  implicit val hitFormat = jsonFormat5(Hit)
  implicit val hitsFormat = jsonFormat3(Hits)
  implicit val searchResponseFormat = jsonFormat4(SearchResponse)
  implicit val errorResponseFormat = jsonFormat2(ErrorResponse)

  implicit val requestBodyFormat = jsonFormat3(RequestBody)

  implicit def termQueryFormat = new RootJsonFormat[TermQuery] {
    def write(term: TermQuery) = (term.field -> term.value).toJson
    def read(value: JsValue) = value match {
      case JsObject(o) => TermQuery(o.head._1, o.head._2.convertTo[String])
      case x => deserializationError("Unexpected Term Query object: " + x)
    }
  }

  implicit def termsQueryFormat = new RootJsonFormat[TermsQuery] {
    def write(term: TermsQuery) = (term.field -> term.values).toJson
    def read(value: JsValue) = value match {
      case JsObject(o) => o.head._2 match {
        case JsArray(v) => TermsQuery(o.head._1, v.map(_.convertTo[String]).toList)
        case x => deserializationError("Invalid Terms Query values parameter: " + x)
      }
      case x => deserializationError("Unexpected Terms Query object: " + x)
    }
  }

  implicit def rangeQueryFormat = new RootJsonFormat[RangeQuery] {
    def write(range: RangeQuery) =
      (range.field -> Map(
        "gte" -> range.gte,
        "gt" -> range.gt,
        "lte" -> range.lte,
        "lt" -> range.lt
      )).toJson
    def read(value: JsValue) = value match {
      case JsObject(o) =>
        val field = o.head._1
        val map = o.head._2.asInstanceOf[JsObject]
        def getVal(key:String): Option[Double] = map.fields.get(key).map(_.convertTo[Double])
        
        RangeQuery(field, getVal("gte"), getVal("gt"), getVal("lte"), getVal("lt"))
      case x =>  deserializationError("Unexpected Range Query object: " + x)
    }
  }

}

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

  sealed trait QueryClause
  case class TermQuery(field: String, value: String) extends QueryClause
  case class TermsQuery(field: String, values: List[String]) extends QueryClause
  case class RangeQuery(field: String, gte: Option[Double], gt: Option[Double], lte: Option[Double], lt: Option[Double]) extends QueryClause

  case class BoolQuery(must: Option[Array[QueryClause]], filter: Option[Array[QueryClause]], should: Option[Array[QueryClause]], must_not: Option[Array[QueryClause]]) extends QueryClause



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
    def write(term: TermQuery) = Map(term.field -> term.value).toJson
    def read(value: JsValue) = value match {
      case JsObject(o) => TermQuery(o.head._1, o.head._2.convertTo[String])
      case x => deserializationError("Unexpected Term Query object: " + x)
    }
  }

  implicit def termsQueryFormat = new RootJsonFormat[TermsQuery] {
    def write(term: TermsQuery) = Map(term.field -> term.values).toJson
    def read(value: JsValue) = value match {
      case JsObject(o) => o.head._2 match {
        case JsArray(v) => TermsQuery(o.head._1, v.map(_.convertTo[String]).toList)
        case x => deserializationError("Invalid Terms Query values parameter: " + x)
      }
      case x => deserializationError("Unexpected Terms Query object: " + x)
    }
  }

  implicit def rangeQueryFormat = new RootJsonFormat[RangeQuery] {
    def toJsField(key: String, value: Option[Double]): Map[String, JsValue] = value match {
      case Some(x) => Map(key -> x.toJson)
      case None => Map.empty
    }

    def write(range: RangeQuery) = {
      val fields =JsObject(
        toJsField("gte", range.gte) ++
          toJsField("gt", range.gt) ++
          toJsField("lte", range.lte) ++
          toJsField("lt", range.lt))

      JsObject((range.field, fields))
    }

    def read(value: JsValue) = value match {
      case JsObject(o) =>
        val field = o.head._1
        val map = o.head._2.asInstanceOf[JsObject]
        def getVal(key: String): Option[Double] = map.fields.get(key).map(_.convertTo[Double])

        RangeQuery(field, getVal("gte"), getVal("gt"), getVal("lte"), getVal("lt"))
      case x => deserializationError("Unexpected Range Query object: " + x)
    }
  }

  implicit def queryClauseFormat = new RootJsonFormat[QueryClause] {
    def write(query: QueryClause) = query match {
      case x: TermQuery => Map("term" -> x).toJson
      case x: TermsQuery => Map("terms" -> x).toJson
      case x: RangeQuery => Map("range" -> x).toJson
      case x: BoolQuery => Map("bool" -> x).toJson
    }

    def read(value: JsValue) = value match {
      case JsObject(o) if o.size == 1 =>
        o.head._1 match {
          case "term" => o.head._2.convertTo[TermQuery]
          case "terms" => o.head._2.convertTo[TermsQuery]
          case "range" => o.head._2.convertTo[RangeQuery]
          case "bool" => o.head._2.convertTo[BoolQuery]
        }

      case x => deserializationError("Unexpected Bool Query object: " + x)
    }
  }

  implicit val boolQueryFormat: JsonFormat[BoolQuery] = lazyFormat(jsonFormat(BoolQuery, "must", "filter", "should", "must_not"))
}

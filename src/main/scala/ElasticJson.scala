package escli

import spray.json._
import JsonUtil._

object ElasticJson {

  sealed trait ElasticResponse

  /** Response objects */
  case class ShardInfo(total: Int, successful: Int, failed: Int)
  case class Hit(_index: String, _type: String, _id: String, _score: Double, _source: JsObject) {

    def getString(field: String): String = {
      field match {
        case "_index" => _index
        case "_type" => _type
        case "_id" => _id
        case _ => _source.fields.get(field).map(stringOf).getOrElse("null")
      }
    }

    def columnInfo(field: String): (String, Int) =
      field -> field.length().max(getString(field).length)

    def columnsInfo(): Map[String, Int] =
      (_source.fields.keys ++ List("_index", "_type", "_id"))
        .map(columnInfo)
        .toMap

  }

  case class Hits(total: Long, max_score: Double, hits: Array[Hit])
  case class SearchResponse(took: Long, timed_out: Boolean, _shards: ShardInfo, hits: Hits) extends ElasticResponse
  case class ErrorResponse(error: String, status: Int) extends ElasticResponse

  /** Request objects */
  case class Request(path: String, body: RequestBody)
  case class RequestBody(from: Option[Int], size: Option[Int], _source: Option[Array[String]], query: Option[TypedQuery])

  sealed trait QueryClause
  case class TypedQuery(clause: QueryClause)
  case class TermQuery(field: String, value: String) extends QueryClause
  case class TermsQuery(field: String, values: List[String]) extends QueryClause
  case class RangeQuery(field: String, gte: Option[Double], gt: Option[Double], lte: Option[Double], lt: Option[Double]) extends QueryClause

  case class BoolQuery(must: Option[Array[TypedQuery]], filter: Option[Array[TypedQuery]], should: Option[Array[TypedQuery]], must_not: Option[Array[TypedQuery]]) extends QueryClause

}

object ElasticJsonProtocol extends DefaultJsonProtocol {
  import ElasticJson._

  implicit val shardInfoFormat = jsonFormat3(ShardInfo)
  implicit val hitFormat = jsonFormat5(Hit)
  implicit val hitsFormat = jsonFormat3(Hits)
  implicit val searchResponseFormat = jsonFormat4(SearchResponse)
  implicit val errorResponseFormat = jsonFormat2(ErrorResponse)

  implicit def elasticResponseFormat = new RootJsonFormat[ElasticResponse] {
    def write(e: ElasticResponse) = e match {
      case o: SearchResponse => o.toJson
      case o: ErrorResponse => o.toJson
    }
    def read(value: JsValue) = ???
  }

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
      val fields = JsObject(
        toJsField("gte", range.gte) ++
          toJsField("gt", range.gt) ++
          toJsField("lte", range.lte) ++
          toJsField("lt", range.lt)
      )

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

  implicit def typedQueryFormat = new RootJsonFormat[TypedQuery] {
    def write(q: TypedQuery) = q.clause match {
      case x: TermQuery => Map("term" -> x).toJson
      case x: TermsQuery => Map("terms" -> x).toJson
      case x: RangeQuery => Map("range" -> x).toJson
      case x: BoolQuery => Map("bool" -> x).toJson
    }

    def read(value: JsValue) = value match {
      case JsObject(o) if o.size == 1 =>
        o.head._1 match {
          case "term" => TypedQuery(o.head._2.convertTo[TermQuery])
          case "terms" => TypedQuery(o.head._2.convertTo[TermsQuery])
          case "range" => TypedQuery(o.head._2.convertTo[RangeQuery])
          case "bool" => TypedQuery(o.head._2.convertTo[BoolQuery])
        }

      case x => deserializationError("Unexpected Bool Query object: " + x)
    }
  }

  implicit val boolQueryFormat: JsonFormat[BoolQuery] = lazyFormat(jsonFormat4(BoolQuery))

  implicit val requestBodyFormat = jsonFormat4(RequestBody)

}

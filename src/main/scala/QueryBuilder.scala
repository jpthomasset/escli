package escli

import AST._
import ElasticJson._

object QueryBuilder {
  def build(statement: Statement): Option[Request] = {
    statement match {
      case Select(list, source, where, limit) =>
        Some(Request(build(source), RequestBody(None, limit.map(_.size), build(list), build(where))))
      case _ => None
    }
  }

  def build(selectList: SelectList): Option[Array[String]] =
    selectList match {
      case AllFields() => None
      case Fields(fields) => Some(fields.toArray)
    }

  def build(source: Source): String =
    "/" + source.index + (source.documentType match {
      case Some(t) => "/" + t
      case None => ""
    }) + "/_search"

  def build(condition: Option[Condition]): Option[TypedQuery] =
    condition.map {
      case TermCondition(field, term) => TypedQuery(TermQuery(field, term))
      case TermsCondition(field, terms) => TypedQuery(TermsQuery(field, terms))
      case RangeCondition(field, min, max) => TypedQuery(RangeQuery(field, Some(min), None, Some(max), None))
      case ComparisonCondition(field, op, value) => op match {
        case AST.eq => TypedQuery(TermQuery(field, value.toString()))
        case AST.gt => TypedQuery(RangeQuery(field, None, Some(value), None, None))
        case AST.lt => TypedQuery(RangeQuery(field, None, None, None, Some(value)))
        case AST.gte => TypedQuery(RangeQuery(field, Some(value), None, None, None))
        case AST.lte => TypedQuery(RangeQuery(field, None, None, Some(value), None))
        }

      case _ => ???
    }

}

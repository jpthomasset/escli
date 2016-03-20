package escli

import AST._
import ElasticJson._

object QueryBuilder {
  def build(statement: Statement): Option[Request] = {
    statement match {
      case Select(list, source) =>
        Some(Request(build(source), RequestBody(None, None, build(list))))
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
      case Some(t) =>  "/" + t
      case None => ""
    }) + "/_search"

}

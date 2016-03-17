package escli

import scala.util.parsing.combinator._
import escli.AST._

/**
 *  Parser for a language close to SQL for Elastic querying
 */
class SimpleParser extends JavaTokenParsers {
  /** star in select statement */
  def star: Parser[AllFields] = "*" ^^^ (AllFields())

  /** field format */
  def field: Parser[String] = ident
  
  /** list of field in select statement */
  def fields: Parser[Fields] = repsep(field, ",") ^^ {
    case fieldList => Fields(fieldList)
  }

  def index: Parser[String] =
    """[a-zA-Z0-9:._\-*]+""".r

  def source: Parser[Source] = ((index ~ "with type" ~ ident) ^^ {
    case i ~ "with type" ~ t => Source(i, Some(t))
  }) | (index ^^ {
    case i => Source(i, None)
  })

  /** define what to select in a select statement */
  def selectList: Parser[SelectList] = star | fields

  /** select statement */
  def select: Parser[Select] = "select" ~ selectList ~ "from" ~ source  ^^ {
    case "select" ~ l ~ "from" ~ s => Select(l, s)
  }

  /** delete statement */
  def delete: Parser[Delete] = "delete from" ~ source  ^^ {
    case "delete from" ~ s => Delete(s)
  }

  def empty: Parser[Empty] = "" ^^^ Empty()

  def statement: Parser[Statement] = (select | delete | empty) <~ ";" 
}

object SimpleParser extends SimpleParser {

}

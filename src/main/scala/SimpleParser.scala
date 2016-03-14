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
    """\w[\w:.]*""".r

  def source: Parser[Source] = ((index ~ "with type" ~ ident) ^^ {
    case i ~ "with type" ~ t => Source(i, Some(t))
  }) | (ident ^^ {
    case i => Source(i, None)
  })

  /** define what to select in a select statement */
  def selectList: Parser[SelectList] = star | fields

  /** select statement */
  def select: Parser[Select] = "select" ~ selectList ~ "from" ~ source  ^^ {
    case "select" ~ l ~ "from" ~ s => Select(l, s)
  }
}

object SimpleParser extends SimpleParser {

}

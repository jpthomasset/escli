package escli

import scala.util.parsing.combinator._
import escli.AST._

/**
 *  Parser for a language close to SQL for Elastic querying
 */
class SimpleParser extends JavaTokenParsers {
  /** star in select statement */
  def star: Parser[AllFields] = "*" ^^^ (AllFields())

  /** list of field in select statement */
  def fields: Parser[Fields] = repsep(ident, ",") ^^ {
    case fieldList => Fields(fieldList)
  }

  /** select statement */
  def select: Parser[Select] = "select" ~ (star | fields)  ^^ {
    case "select" ~ f => Select(f)
  }
}

object SimpleParser extends SimpleParser {

}

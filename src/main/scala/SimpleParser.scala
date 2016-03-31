package escli

import scala.util.parsing.combinator._
import escli.AST._
import scala.util.parsing.input.CharSequenceReader

/**
 *  Parser for a language close to SQL for Elastic conditioning
 */
class SimpleParser extends JavaTokenParsers with PackratParsers {
  /** star in select statement */
  def star: Parser[AllFields] = "*" ^^^ (AllFields())

  /** field format */
  def field: Parser[String] = """[a-zA-Z0-9_](?:\.?[a-zA-Z0-9_*]+)*""".r

  /** field without star */
  def fieldwithoutstar: Parser[String] = """[a-zA-Z0-9_](?:\.?[a-zA-Z0-9_]+)*""".r

  /** list of field in select statement */
  def fields: Parser[Fields] = repsep(field, ",") ^^ {
    case fieldList => Fields(fieldList)
  }

  def index: Parser[String] =
    """[a-zA-Z0-9:._\-*]+""".r

  def source: Parser[Source] = ((index ~ "(?i)with type".r ~ ident) ^^ {
    case i ~ withType ~ t => Source(i, Some(t))
  }) | (index ^^ {
    case i => Source(i, None)
  })

  /** define what to select in a select statement */
  def selectList: Parser[SelectList] = star | fields

  def limit: Parser[Option[Limit]] = ("(?i)limit".r ~ wholeNumber ^^ {
    case limit ~ i => Some(Limit(i.toInt))
  }) | ("" ^^^ (None))

  def string: Parser[String] = "'([^']*)'".r ^^ { case s => s.substring(1, s.length() - 1) }

  def strings: Parser[List[String]] = repsep(string, ",") ^^ { case l => l }

  def term_condition: Parser[TermCondition] = fieldwithoutstar ~ "=" ~ string ^^ {
    case f ~ "=" ~ v => TermCondition(f, v)
  }

  def terms_condition: Parser[TermsCondition] = fieldwithoutstar ~ "(?i)IN".r ~ "(" ~ strings ~ ")" ^^ {
    case f ~ in ~ "(" ~ v ~ ")" => TermsCondition(f, v)
  }

  def operator: Parser[Operator] = ("=" | ">=" | "<=" | ">" | "<") ^^ {
    case "=" => AST.eq
    case ">=" => AST.gte
    case "<=" => AST.lte
    case ">" => AST.gt
    case "<" => AST.lt
  }

  def comparison_condition: Parser[ComparisonCondition] =
    fieldwithoutstar ~ operator ~ floatingPointNumber ^^ {
      case f ~ o ~ n => ComparisonCondition(f, o, n.toDouble)
    }

  def range_condition: Parser[RangeCondition] = fieldwithoutstar ~ "between" ~ floatingPointNumber ~ "and" ~ floatingPointNumber ^^ {
    case f ~ "between" ~ min ~ "and" ~ max => RangeCondition(f, min.toDouble, max.toDouble)
    }

  def predicate: Parser[Condition] = term_condition | terms_condition | comparison_condition | range_condition

  lazy val or_condition: PackratParser[OrCondition] = expression ~ "(?i)or".r ~ expression ^^ {
    case c1 ~ or ~ c2 => OrCondition(c1::c2::Nil)
    }

  lazy val and_condition: PackratParser[AndCondition] = expression ~ "(?i)and".r ~ expression ^^ {
    case c1 ~ and ~ c2 => AndCondition(c1::c2::Nil)
  }

  lazy val simple_expression: PackratParser[Condition] = or_condition | and_condition | predicate

  def expression: Parser[Condition] = simple_expression | "(" ~> simple_expression <~ ")"

  def where_clause: Parser[Option[Condition]] =
    ("(?i)where".r ~ expression ^^ { case where ~ expr => Some(expr) }) |
    ( "" ^^^ None)

  /** select statement */
  def select: Parser[Select] = "(?i)select".r ~ selectList ~ "(?i)from".r ~ source ~ where_clause ~ limit  ^^ {
    case select ~ lst ~ from ~ src ~ where ~ lmt => Select(lst, src, where, lmt)
  }

  /** delete statement */
  def delete: Parser[Delete] = "delete from" ~ source ^^ {
    case "delete from" ~ s => Delete(s)
  }

  def empty: Parser[Empty] = "" ^^^ Empty()

  def exit: Parser[Exit] = "exit" ^^^ Exit()

  def statement: Parser[Statement] = (select | delete | empty)

  def explain: Parser[Explain] = ("(?i)explain".r ~ "(?i)result".r ~ statement ^^ {
    case explain ~ result ~ s => Explain(s, true)
  }) | ("(?i)explain".r ~ statement ^^ {
    case explain ~ s => Explain(s, false)
  })

  def command: Parser[Command] = (exit | explain | statement) <~ ";"

  def packParse[T](p: Parser[T], in: CharSequence): ParseResult[T] = 
    phrase(p)(new PackratReader(new CharSequenceReader(in)))

  def parse(in: CharSequence): ParseResult[Command] = packParse(command, in)
}

object SimpleParser extends SimpleParser {

}

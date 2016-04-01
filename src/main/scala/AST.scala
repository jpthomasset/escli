package escli

/**
 * Abstract Syntax Tree classes for representing the query
 */
object AST {

  sealed trait SelectList

  case class AllFields() extends SelectList
  case class Fields(fields: List[String]) extends SelectList

  case class Source(index: String, documentType: Option[String])

  case class Limit(size: Int)

  sealed trait Operator

  case object eq extends Operator
  case object gt extends Operator
  case object lt extends Operator
  case object gte extends Operator
  case object lte extends Operator

  sealed trait Condition

  case class TermCondition(field: String, term: String) extends Condition
  case class TermsCondition(field: String, terms: List[String]) extends Condition
  case class ComparisonCondition(field: String, operator: Operator, number: Double) extends Condition
  case class RangeCondition(field: String, min: Double, max: Double) extends Condition
  case class OrCondition(conditions: List[Condition]) extends Condition
  case class AndCondition(conditions: List[Condition]) extends Condition

  sealed trait Command
  sealed trait Statement extends Command

  case class Select(selectList: SelectList, source: Source, where:Option[Condition] = None, limit: Option[Limit] = None) extends Statement
  case class Delete(source: Source) extends Statement
  case class Empty() extends Statement

  case class Exit() extends Command
  case class Print(s: String) extends Command
  case class Explain(s:Statement, withResult:Boolean = false) extends Command
}


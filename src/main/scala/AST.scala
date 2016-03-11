package escli


/**
 * Abstract Syntax Tree classes for representing the query
 */
object AST {

  sealed trait SelectList

  case class AllFields() extends SelectList
  case class Fields(val fields: List[String]) extends SelectList
  case class Select(val selectList: SelectList)

}

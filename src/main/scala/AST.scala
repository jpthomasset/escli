package escli

/**
 * Abstract Syntax Tree classes for representing the query
 */
object AST {

  sealed trait SelectList

  case class AllFields() extends SelectList
  case class Fields(fields: List[String]) extends SelectList

  case class Source(index: String, documentType: Option[String])

  sealed trait Statement

  case class Select(selectList: SelectList, source: Source) extends Statement
  case class Delete(source: Source) extends Statement
  case class Empty() extends Statement
}


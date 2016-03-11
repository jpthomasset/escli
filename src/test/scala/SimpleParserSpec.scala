import org.scalatest._

import escli.SimpleParser
import escli.AST._

class SimpleParserSpec extends WordSpec with Matchers {
  import SimpleParser._

  def assertParseResult[T](p: Parser[T], in: CharSequence, expected: T) = {
    val r = parse(p, in)
    r match {
      case Success(p, _) => assert(p == expected)
      case _ => fail()
    }
  }


  "A SimpleParser" should {
    "parse select with a star" in {
      assertParseResult(select, "select *", Select(AllFields()))
    }

    "parse select with one field" in {
      assertParseResult(select, "select oneField", Select(Fields("oneField" :: Nil)))
    }

    "parse select with multiple fields" in {
      assertParseResult(select,
        "select field1, field2, field3",
        Select(Fields("field1" :: "field2" :: "field3" :: Nil))
      )
    }

  }

}

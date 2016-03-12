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


  "A `selectList` parser" should {
    "parse a star" in {
      assertParseResult(selectList, "*", AllFields())
    }

    "parse a field" in {
      assertParseResult(selectList, "somefield", Fields("somefield" :: Nil))
    }

    "parse multiple fields" in {
      assertParseResult(selectList, "field1, field2, field3", Fields("field1" :: "field2" :: "field3" :: Nil))
    }
  }

  "A 'source' parser" should {
    "parse a simple index" in {
      assertParseResult(source, "someindexname", Source("someindexname", None))
    }

    "parse an index with type information" in {
      assertParseResult(source, "someindexname with type mytype", Source("someindexname", Some("mytype")))
    }
  }


  "A SimpleParser" should {
    "parse a simple query all from an index" in {
      assertParseResult(select, "select * from someindex", Select(AllFields(), Source("someindex", None)))
    }

    "parse select with multiple field and index" in {
      assertParseResult(select, "select field1, field2 from someindex",
        Select(Fields("field1" :: "field2" :: Nil), Source("someindex", None)))
    }

    "parse select with multiple fields and index with type" in {
      assertParseResult(select,
        "select field1, field2, field3 from someindex with type mytype",
        Select(Fields("field1" :: "field2" :: "field3" :: Nil), Source("someindex", Some("mytype"))))
    }

  }

}

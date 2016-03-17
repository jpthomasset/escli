import org.scalatest._

import escli.SimpleParser
import escli.AST._

class SimpleParserSpec extends WordSpec with Matchers {
  import SimpleParser._

  def assertParseResult[T](p: Parser[T], in: CharSequence, expected: T) = {
    val r = parse(p, in)
    r match {
      case Success(ast, _) => assertResult(expected)(ast)
      case _ => fail
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

  "An 'index' parser" should {

    "parse an index composed of word chars" in {
      assertParseResult(index, "someindexname", "someindexname")
    }

    "parse an index composed of alphanumeric chars" in {
      assertParseResult(index, "someindexname1234", "someindexname1234")
    }

    "parse an index containing some special chars ([:._-*]" in {
      assertParseResult(index, "some:index:*name_with-and.dot", "some:index:*name_with-and.dot")
    }
  }


  "A 'source' parser" should {
    "parse a simple index" in {
      assertParseResult(source, "someindexname", Source("someindexname", None))
    }

    "parse an index with colon" in {
      assertParseResult(source, "some:index:name", Source("some:index:name", None))
    }

    "parse an index with type information" in {
      assertParseResult(source, "someindexname with type mytype", Source("someindexname", Some("mytype")))
    }
  }


  "A 'select' parser" should {
    "parse a simple query from an index" in {
      assertParseResult(select, "select * from someindex", Select(AllFields(), Source("someindex", None)))
    }

    "parse a query with multiple field and index" in {
      assertParseResult(select, "select field1, field2 from someindex",
        Select(Fields("field1" :: "field2" :: Nil), Source("someindex", None)))
    }

    "parse a query with multiple fields and index with type" in {
      assertParseResult(select,
        "select field1, field2, field3 from someindex with type mytype",
        Select(Fields("field1" :: "field2" :: "field3" :: Nil), Source("someindex", Some("mytype"))))
    }
  }

  "A 'delete' parser" should {
    "parse a delete query" in {
      assertParseResult(delete, "delete from someindexname", Delete(Source("someindexname", None)))
    }
  }

  "An 'empty' parser" should {
    "parse an empty query" in {
      assertParseResult(SimpleParser.empty, "", Empty())
    }

    "parse a query with only spaces" in {
      assertParseResult(SimpleParser.empty, "   ", Empty())
    }

  }

  "A 'statement' parser" should {
    "parse a delete query" in {
      assertParseResult(statement, "delete from someindexname;", Delete(Source("someindexname", None)))
    }

    "parse a select query" in {
      assertParseResult(statement, "select * from someindex;", Select(AllFields(), Source("someindex", None)))
    }

    "parse an empty query" in {
      assertParseResult(statement, ";", Empty())
    }
    
  }

}

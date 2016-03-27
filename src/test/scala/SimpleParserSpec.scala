import org.scalatest._

import escli.SimpleParser
import escli.AST
import escli.AST._

class SimpleParserSpec extends WordSpec with Matchers {
  import SimpleParser._

  def assertParseResult[T](p: Parser[T], in: CharSequence, expected: T) = {
    val r = parse(p, in)
    r match {
      case Success(ast, _) => assertResult(expected)(ast)
      case x => fail(x.toString)
    }
  }

  "A field name parser" should {
    "allow alphanumeric chars" in {
      assertParseResult(field, "aabb.", "aabb")
    }

    "allow dot in the middle" in {
      assertParseResult(field, "aa.bb.", "aa.bb")
    }

    "ignore dot with no trailing name" in {
      assertParseResult(field, "aa.", "aa")
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

    "be case insensitive for 'with type' keyword" in {
      assertParseResult(source, "someindexname With tYPe mytype", Source("someindexname", Some("mytype")))
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
      assertParseResult(
        select,
        "select field1, field2, field3 from someindex with type mytype",
        Select(Fields("field1" :: "field2" :: "field3" :: Nil), Source("someindex", Some("mytype")))
      )
    }

    "should be case insensitive" in {
      assertParseResult(select, "Select * From someindex", Select(AllFields(), Source("someindex", None)))
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

  "A 'limit' parser" should {
    "parse a 'limit' keyword" in {
      assertParseResult(SimpleParser.limit, "limit 4", Some(Limit(4)))
    }

    "be case insensitive" in {
      assertParseResult(SimpleParser.limit, "LiMiT 4", Some(Limit(4)))
    }
  }

  "A 'statement' parser" should {
    "parse a delete query" in {
      assertParseResult(statement, "delete from someindexname;", Delete(Source("someindexname", None)))
    }

    "parse a select query" in {
      assertParseResult(statement, "select * from someindex;", Select(AllFields(), Source("someindex", None)))
    }

    "parse a select query with limit" in {
      assertParseResult(statement, "select * from someindex limit 12;", Select(AllFields(), Source("someindex", None), Some(Limit(12))))
    }

    "Parse an empty query" in {
      assertParseResult(statement, ";", Empty())
    }

    "parse an exit command" in {
      assertParseResult(statement, "exit;", Exit())
    }

  }

  "'string' and 'string' parsers" should {
    "parse a quoted string" in {
      assertParseResult(string, "'a string literal'", "a string literal")
    }

    "parse a list of quoted string" in {
      assertParseResult(strings, "'first', 'second', 'third'", List("first", "second", "third"))
    }
  }

  "A 'term' parser" should {
    "parse a term condition" in {
      assertParseResult(term_condition, "field = 'value'", TermCondition("field", "value"))
    }
  }

  "A 'terms' parser" should {
    "parse an IN clause with one value" in {
      assertParseResult(terms_condition, "field IN ('value')", TermsCondition("field", List("value")))
    }

    "parse an IN clause with multiple values" in {
      assertParseResult(
        terms_condition,
        "field in ('value1', 'value2', 'value3')",
        TermsCondition("field", List("value1", "value2", "value3"))
      )
    }

    "ignore case of 'IN' keyword" in {
      assertParseResult(terms_condition, "field iN ('value')", TermsCondition("field", List("value")))
    }
  }

  "An 'operator' parser" should {
    "parse operators" in {
      val op = Map(">" -> gt, "<" -> lt, ">=" -> gte, "<=" -> lte, "=" -> AST.eq)

      op.foreach {
        case (s, o) => assertParseResult(operator, s, o)
      }
    }
  }

  "A comparison parser" should {
    "allow all operators comparison" in {
      val op = Map(">" -> gt, "<" -> lt, ">=" -> gte, "<=" -> lte, "=" -> AST.eq)

      op.foreach {
        case (s, o) =>
          assertParseResult(
            comparison_condition,
            "field" + s + "42",
            ComparisonCondition("field", o, 42)
          )
      }
    }

  }

  "A 'range' parser" should {
    "parse a between clause" in {
      assertParseResult(range_condition, "field between -42 and 42", RangeCondition("field", -42, 42))
    }
  }

  "An 'expression' parser" should {
    "allow parenthesis around a condition" in {
      assertParseResult(expression, "(a=22)", ComparisonCondition("a", AST.eq, 22))
    }
  }

  "A boolean parser" should {
    "parse an 'or' condition" in {
      assertParseResult(
        or_condition,
        "a=22 or b='toto'",
        OrCondition(
          List(ComparisonCondition("a", AST.eq, 22), TermCondition("b", "toto"))
        )
      )
    }

    "parse an 'and' condition" in {
      assertParseResult(
        and_condition,
        "field1=42 AND field2='forty-two'",
        AndCondition(
          List(ComparisonCondition("field1", AST.eq, 42), TermCondition("field2", "forty-two"))
        )
      )
    }
  }

}

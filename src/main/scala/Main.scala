import scala.util.parsing.combinator._

case class Select(val fields: String*)

class SimpleParser extends JavaTokenParsers {
  def selectAll: Parser[Select] = "select" ~ "*" ^^^ (Select("*"))

  def select: Parser[Select] = "select" ~ repsep(ident, ",") ^^ {
    case "select" ~ f => Select(f: _*)
  }

}



object Main extends SimpleParser {
  def main(args: Array[String]) = {
    println("Hello world!")


     println(parse(select, "select toto,tata,titi, tete"))

  }
}

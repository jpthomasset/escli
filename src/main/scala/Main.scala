package escli

object Main extends SimpleParser {
  def main(args: Array[String]) = {

    println(parse(select, "select *"))

  }
}

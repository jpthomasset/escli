package escli

object Main extends SimpleParser {
  def main(args: Array[String]) = {

    val url = if(args.size > 0) args(0) else "http://127.0.0.1:9200/"

    println(s"Querying ${url}")
    println(parse(select, "select * from someindex"))

  }
}

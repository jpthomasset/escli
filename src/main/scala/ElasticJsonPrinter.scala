package escli

import spray.json._
import escli.ElasticJson._

object ElasticJsonPrinter {

  def columnSize(hit: Hit): Map[String, Int] = {
    hit._source match {
      case JsObject(o) => o.map(t => (t._1, t._1.length().max(jsSize(t._2))))
      case _ => Map.empty
    }
  }

  def jsSize(x: JsValue): Int = x match {
    case _: JsObject => x.toString().length().min(40)
    case _ => x.toString().length()
  }

  def print(r: SearchResponse): Unit = {
    print(r.hits.hits)
  }

  /** Output data in tabular format: 
    * +-----+-----+
    * |  A  |  B  |
    * +-----+-----+
    * | a1  | b1  |
    * | a2  | b2  |
    * +-----+-----+
    */
  def print(hits: Array[Hit]): Unit = {
    // get column size
    val columns = hits
      .map(columnSize)
      .reduce { (a, b) => a ++ b.map { case (k: String, v: Int) => k -> v.max(a.getOrElse(k, 0)) } }

    /** Utility to print a row of separator, header or data (Hit) */
    def printRow(f: (String, Int) => String, pad: String, sep: String) = {
      Console.print(sep)
      columns.foreach {
        case (col, size) =>
          val content = f(col, size).take(size)
          val padString = pad * (size - content.length() + 1)
          Console.print(pad + content + padString + sep)
      }
      println()
    }

    /** Print a separator: +---+---+ */
    def printSeparator() = printRow((col, size) => "", "-", "+")
    /** Print columns header: | A | B | */
    def printHeader() = printRow((col, size) => col, " ", "|")
    /** Print one row of data: | a | b | */
    def printHit(h: Hit) = printRow(
      (col, size) => h._source.asJsObject.fields.getOrElse(col, "").toString(),
      " ",
      "|"
    )

    printSeparator()
    printHeader()
    printSeparator()

    hits.foreach(printHit)

    printSeparator()

  }

}

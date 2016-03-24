package escli

import spray.json._
import escli.ElasticJson._

class ElasticJsonPrinter(output: String => Unit) {

  def columnSize(hit: Hit): Map[String, Int] = {
    hit._source match {
      case JsObject(o) => o.map(t => (t._1, t._1.length().max(jsSize(t._2))))
      case _ => Map.empty
    }
  }

  def jsString(x: JsValue) : String = x match {
    case JsObject(_) => x.toString()
      case JsArray(_)  => x.toString()
      case JsNull      => "null"
      case JsTrue      => "true"
      case JsFalse     => "false"
      case JsNumber(x) => x.toString()
      case JsString(x) => x
      case _           => throw new IllegalStateException
  }

  def jsSize(x: JsValue): Int = x match {
    case _: JsObject => jsString(x).length().min(40)
    case _ => jsString(x).length()
  }

  def print(rq:Request, rs: SearchResponse): Unit = {
    val cols = rq.body._source.getOrElse(Array.empty).toList
    print(rs.hits.hits, cols)
    output(s"\033[0;1mDisplayed ${rs.hits.hits.length} of ${rs.hits.total} documents (${rs.took} ms)\033[0;0m\n\n")
  }

  /**
   * Output data in tabular format:
   * +-----+-----+
   * |  A  |  B  |
   * +-----+-----+
   * | a1  | b1  |
   * | a2  | b2  |
   * +-----+-----+
   */
  def print(hits: Array[Hit], queryCols: List[String]): Unit = {
    // get column size
    val columns = hits
      .map(columnSize)
      .reduce { (a, b) => a ++ b.map { case (k: String, v: Int) => k -> v.max(a.getOrElse(k, 0)) } }

    val detectedCols = columns.map { case(col, _) => col}.filterNot(queryCols.toSet)

    val orderedCols = queryCols ++ detectedCols
       

    /** Utility to print a row of separator, header or data (Hit) */
    def printRow(f: (String) => String, pad: String, sep: String) = {
      output(sep)
      orderedCols.foreach(col => { 
        val size =  columns.getOrElse(col, col.length())
        val content = f(col).take(size)
        val padString = pad * (size - content.length() + 1)
        output(pad + content + padString + sep)
      })
      output("\n")
    }

    /** Print a separator: +---+---+ */
    def printSeparator() = printRow(col => "", "-", "+")
    /** Print columns header: | A | B | */
    def printHeader() = printRow(col => col, " ", "|")
    /** Print one row of data: | a | b | */
    def printHit(h: Hit) = printRow(
      col => jsString(h._source.asJsObject.fields.getOrElse(col, JsNull)),
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

object ElasticJsonPrinter {
  val StdOut = new ElasticJsonPrinter(Console.print)
}

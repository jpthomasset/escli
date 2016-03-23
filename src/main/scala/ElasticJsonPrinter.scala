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

  def jsSize(x: JsValue): Int = x match {
    case _: JsObject => x.toString().length().min(40)
    case _ => x.toString().length()
  }

  def print(rq:Request, rs: SearchResponse): Unit = {
    val cols = rq.body._source.getOrElse(Array.empty).toList
    print(rs.hits.hits, cols)
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
      col => h._source.asJsObject.fields.getOrElse(col, "").toString(),
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

package escli

import spray.json._
import escli.ElasticJson._

class ElasticJsonPrinter(output: String => Unit) {


  def print(rq: Request, rs: SearchResponse): Unit = {
    if (rs.hits.total > 0) {
      val cols = rq.body._source.getOrElse(Array.empty).toList
      print(rs.hits.hits, cols)
      output(s"\033[0;1mDisplayed ${rs.hits.hits.length} of ${rs.hits.total} documents (${rs.took} ms)\033[0;0m\n\n")
    } else {
      output(s"\033[0;1mEmpty set (${rs.took} ms)\033[0;0m\n\n")
    }

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
    if (hits.length > 0) {
      // get max column size of column across all hits
      val columns = hits
        .map(_.columnsInfo)
        .reduce { (a, b) => a ++ b.map { case (k: String, v: Int) => k -> v.max(a.getOrElse(k, 0)) } }

      val detectedCols = columns.map { case (col, _) => col }.filterNot(queryCols.toSet)

      // Filter out queryCols containing wildcards as the true column names
      // will be found in detectedCols
      val orderedCols = queryCols.filter(!_.contains("*")) ++ detectedCols

      /** Utility to print a row of separator, header or data (Hit) */
      def printRow(f: (String) => String, pad: String, sep: String) = {
        output(sep)
        orderedCols.foreach(col => {
          val size = columns.getOrElse(col, col.length())
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
        c => h.getString(c),
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

}

object ElasticJsonPrinter {
  val StdOut = new ElasticJsonPrinter(Console.print)
}

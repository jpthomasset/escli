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
      val hitsColumnsInfo = hits
        .map(_.columnsInfo)
        .reduce { (a, b) => a ++ b.map { case (k: String, v: Int) => k -> v.max(a.getOrElse(k, 0)) } }

      val detectedCols = hitsColumnsInfo.keySet

      // output columns are
      // if queryCols is empty
      //  => all detected columns key
      // else
      //  => all 'real' columns in queryCols + detected columns matching a regex in querycols
      val columns = if (queryCols.isEmpty) detectedCols else {
        queryCols.filter(!_.contains("*")) ++ queryCols.filter(_.contains("*")).flatMap(c => {
          val r = ElasticJsonPrinter.patternToRegex(c)
          detectedCols.filter(r.pattern.matcher(_).matches())
        })
      }

      /** Utility to print a row of separator, header or data (Hit) */
      def printRow(f: (String) => String, pad: String, sep: String) = {
        output(sep)
        columns.foreach(col => {
          val size = hitsColumnsInfo.getOrElse(col, col.length())
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
  def patternToRegex(pattern: String): scala.util.matching.Regex =
    pattern.replace(".", """\.""").replace("*", ".*").r

  val StdOut = new ElasticJsonPrinter(Console.print)
}

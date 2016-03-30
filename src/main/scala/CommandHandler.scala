package escli


import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import spray.json._

import escli.ElasticJson._
import escli.ElasticJsonProtocol._

class CommandHandler(val makeRequest: (Request) => Future[ElasticResponse])(implicit val ec:ExecutionContext) extends SimpleParser {

  def request(r: Request) = {

    val f =
      makeRequest(r)
        .map({
          case x: SearchResponse => ElasticJsonPrinter.StdOut.print(r, x)
          case x => println(x)
        })

    Await.result(f, 5.seconds)
  }



  def handleStatement(s: String): Boolean = {
    parse(command, s) match {
      case Success(r, _) => r match {
        case AST.Exit() => println("Exiting...") ; false;
        case AST.Explain(x) => QueryBuilder.build(x).map(r => println(r.body.toJson.prettyPrint)); true;
        case r:AST.Statement => QueryBuilder.build(r).map(request) ; true;
      }
      case e => println("Parse error: " + e) ; true
    }
  }

}

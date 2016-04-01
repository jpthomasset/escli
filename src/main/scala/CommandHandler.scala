package escli

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import spray.json._

import escli.ElasticJson._
import escli.ElasticJsonProtocol._
import escli.AST._

class CommandHandler(val makeRequest: (Request) => Future[ElasticResponse])(implicit val ec: ExecutionContext) {

  def request(r: Request) = {

    val f =
      makeRequest(r)
        .map({
          case x: SearchResponse => ElasticJsonPrinter.StdOut.print(r, x)
          case x => println(x)
        })

    Await.result(f, 5.seconds)
  }

  /** Handle the given command */
  def handle(c: Command): Boolean = c match {
    case AST.Print(s) => println(s); true;
    case AST.Exit() => println("Exiting..."); false;
    case AST.Explain(x, withResult) => {
      QueryBuilder
        .build(x)
        .foreach(r => {
          println("Query:\n------\n" + r.body.toJson.prettyPrint)
          if (withResult) {
            val f = makeRequest(r).map(resp => println("Response:\n---------\n" + resp.toJson.prettyPrint))
            Await.result(f, 5.seconds)
          }
        })

      true
    }
    case r: AST.Statement => QueryBuilder.build(r).map(request); true;
  }

}

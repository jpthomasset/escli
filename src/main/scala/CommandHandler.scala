package escli

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import spray.json._

import escli.ElasticJson._
import escli.ElasticJsonProtocol._
import escli.AST._

class CommandHandler(val makeRequest: (Request) => Future[ElasticResponse])(implicit val ec: ExecutionContext) {

  def print(rq: Request, rs: ElasticResponse) = {
    rs match {
      case x: SearchResponse => ElasticJsonPrinter.StdOut.print(rq, x)
      case x => println(x)
    }
  }

  def printRaw(rq: Request) = println("Query:\n------\n" + rq.body.toJson.prettyPrint)

  def printRaw(rs: ElasticResponse) = println("Response:\n---------\n" + rs.toJson.prettyPrint)

  def await(e: Future[Any]) = Await.result(e, 5.seconds)

  def exec(s: AST.Statement): Unit = {
    QueryBuilder.build(s)
      .map(rq => await { makeRequest(rq).map(rs => print(rq, rs)) })
  }

  def explain(s: AST.Statement, withResult: Boolean): Unit = {
    QueryBuilder.build(s)
      .map(r => { r })
      .filter(_ => withResult)
      .map(r => await { makeRequest(r).map(printRaw) })
  }

  /** Handle the given command */
  def handle(c: Command): Boolean = c match {
    case AST.Print(s) => println(s); true;
    case AST.Exit() => println("Exiting..."); false;
    case AST.Explain(s, withResult) => explain(s, withResult); true;
    case s: AST.Statement => exec(s); true
  }

}

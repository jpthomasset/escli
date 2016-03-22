package escli

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.MediaTypes._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import spray.json._

import escli.ElasticJson._
import escli.ElasticJsonProtocol._

class CommandHandler(val baseUrl: String)(implicit system: ActorSystem, materializer: ActorMaterializer)
  extends SimpleParser {

  implicit val ec = system.dispatcher

  def request(baseUrl: String, r: Request)(implicit system: ActorSystem, materializer: ActorMaterializer) = {
    import StatusCodes._

    val body = HttpEntity(`application/json`, r.body.toJson.toString)
    val httpRequest = HttpRequest(HttpMethods.POST, baseUrl + r.path, Nil, body)

    val f =
      Http().singleRequest(httpRequest)
        .recover {
          case t => HttpResponse(StatusCodes.InternalServerError, entity = t.getMessage)
        }
        .map(r => r.status match {
          case OK => Unmarshal(r.entity).to[SearchResponse]
          case NotFound => Unmarshal(r.entity).to[ErrorResponse]
          case BadRequest => Unmarshal(r.entity).to[ErrorResponse]
          case _ => Unmarshal(r.entity).to[String].map(ErrorResponse(_, -1))
          //Future.failed(new Exception("Unhandled response: " + r.status))
        })
        .flatMap(identity)
        .map({
          case x: SearchResponse => print(x)
          case x => println(x)
        })

    Await.result(f, 5.seconds)
  }

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

  def print(r: SearchResponse) {
    print(r.hits.hits)
  }

  def print(hits: Array[Hit]) {
    val columns = hits
      .map(columnSize)
      .reduce { (a, b) => a ++ b.map { case (k: String, v: Int) => k -> v.max(a.getOrElse(k, 0)) } }

    def printRow(f:(String, Int) => String, pad:String, sep:String) = {
      Console.print(sep)
      columns.foreach {
        case (col, size) =>
          val content = f(col, size).take(size)
          val padString = pad * (size - content.length() + 1)
          Console.print(pad + content + padString + sep)
      }
      println()
    }

    def printSeparator() = printRow((col, size) => "", "-", "+")
    def printHeader() = printRow((col, size) => col, " ", "|")
    def printHit(h:Hit) = printRow(
      (col, size) => h._source.asJsObject.fields.getOrElse(col, "").toString(),
      " ",
      "|")

    printSeparator()
    printHeader()
    printSeparator()

    hits.foreach(printHit)

    printSeparator()

  }

  def handleStatement(s: String) = {
    parse(statement, s) match {
      case Success(r, _) => r match {
        case AST.Exit() => println("Exiting...")
        case _ => QueryBuilder.build(r).map(request(baseUrl, _))
      }
      case e => println("Parse error: " + e)
    }
  }

  def shutdown() = {
    Http().shutdownAllConnectionPools() andThen { case _ => system.terminate() }
  }
}

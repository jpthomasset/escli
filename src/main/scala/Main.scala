package escli

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._ 
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
 
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import spray.json._

import escli.ElasticJson._
import escli.ElasticJsonProtocol._

import akka.http.scaladsl.model.MediaTypes._
import scala.io.StdIn.readLine

object Main extends SimpleParser {
  def main(args: Array[String]): Unit = {

    

    val url = if(args.size > 0) args(0) else "http://127.0.0.1:9200"

    println(s"Querying ${url}")

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    


    println("escli v0.1")
    Iterator
      .continually(readLine("escli> "))
      .takeWhile(_ != null)
      .foldLeft("")( (acc, line) => {
        if(line.endsWith(";")) {
          //println("Got " + acc + line)
          handleStatement(url, acc + line)
          ""
        } else {
          acc + " " + line
        }
      })

   
    Http().shutdownAllConnectionPools() andThen { case _ => system.terminate() }
  }

  def request(baseUrl: String, r: Request)(implicit system:ActorSystem,  materializer:ActorMaterializer) = {
    import StatusCodes._
    implicit val ec = system.dispatcher



    val body = HttpEntity(`application/json`, r.body.toJson.toString)
    val httpRequest = HttpRequest(HttpMethods.POST, baseUrl + r.path, Nil, body)

    val f =
      Http().singleRequest(httpRequest)
        .map(r => r.status match {
          case OK => Unmarshal(r.entity).to[SearchResponse]
          case NotFound => Unmarshal(r.entity).to[ErrorResponse]
          case BadRequest => Unmarshal(r.entity).to[ErrorResponse]
          case _ => Future.failed(new Exception("Unhandled response: " + r.status))
        })
        .flatMap(identity)
        .map(println)
        /*.map(Unmarshal(_).to[SearchResponse])
        .flatMap(identity)
        .map(r => println("Got " + r.hits.total + " responses.\n" + r.hits.hits))*/


    Await.result(f, 5.seconds)
    }

  def handleStatement(baseUrl: String, s:String)(implicit system:ActorSystem,  materializer:ActorMaterializer) = {
    parse(statement, s) match {
      case Success(r, _) =>
        val q = QueryBuilder.build(r)
        request(baseUrl, q)

      case e => println(e)
    }
  }

}

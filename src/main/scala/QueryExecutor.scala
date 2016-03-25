package escli

import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.MediaTypes._
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import spray.json._

import escli.ElasticJson._
import escli.ElasticJsonProtocol._

class QueryExecutor(val baseUrl: String, val makeRequest: (HttpRequest) => Future[HttpResponse])(implicit system: ActorSystem, materializer: ActorMaterializer) {
  implicit val ec = system.dispatcher

  def request(r: Request): Future[ElasticResponse] = {
    import StatusCodes._

    val body = HttpEntity(`application/json`, r.body.toJson.toString)
    val httpRequest = HttpRequest(HttpMethods.POST, baseUrl + r.path, Nil, body)

    makeRequest(httpRequest)
      .recover {
        case t => HttpResponse(StatusCodes.InternalServerError, entity = t.getMessage)
      }
      .map(r => r.status match {
        case OK => Unmarshal(r.entity).to[SearchResponse]
        case NotFound => Unmarshal(r.entity).to[ErrorResponse]
        case BadRequest => Unmarshal(r.entity).to[ErrorResponse]
        case _ => Unmarshal(r.entity).to[String].map(ErrorResponse(_, -1))
      })
      .flatMap(identity)
  }

}

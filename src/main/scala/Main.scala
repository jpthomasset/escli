package escli

// import akka.http.scaladsl.Http
// import akka.http.scaladsl.model._
// import akka.stream.ActorMaterializer
// import akka.actor.ActorSystem
 
// import scala.concurrent.{Future, Await}
// import scala.concurrent.duration._

import scala.io.StdIn.readLine

object Main extends SimpleParser {
  def main(args: Array[String]): Unit = {

    

    val url = if(args.size > 0) args(0) else "http://127.0.0.1:9200"

    println(s"Querying ${url}")

    // implicit val system = ActorSystem()
    // implicit val materializer = ActorMaterializer()
    // implicit val ec = system.dispatcher

    // val f: Future[HttpResponse] =
    //   Http().singleRequest(HttpRequest(uri = url + "/_search"))


    // f onSuccess {
    //   case r =>
    //     println("Result " + r)
    //     system.terminate()
    // }
    // // println(parse(select, "select * from someindex"))


    // Await.result(f, 5.seconds)


    println("escli v0.1")
    Iterator
      .continually(readLine("escli> "))
      .takeWhile(_ != null)
      .foldLeft("")( (acc, line) => {
        if(line.endsWith(";")) {
          //println("Got " + acc + line)
          handleStatement(acc + line)
          ""
        } else {
          acc + " " + line
        }
      })

  }

  def handleStatement(s:String) = {
    println(parse(statement, s))
  }
}

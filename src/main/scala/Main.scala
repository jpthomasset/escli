package escli

import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.http.scaladsl.Http


object Main {
  def main(args: Array[String]): Unit = {

    val baseUrl = if (args.size > 0) args(0) else "http://127.0.0.1:9200"

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    val qexec = new QueryExecutor(baseUrl, Http().singleRequest(_))
    def parse(s:String) = CommandParser
      .parse(s)
      .recover { case x => AST.Print(x.getMessage) }
       
    val handler = new CommandHandler(qexec.request)
    val scanner = CommandScanner(Terminal("escli> ").scan())

    try {
      // as the source is an iterator, we must
      // constantly 'pull' data. This is the reason to
      // use a foreach here
      scanner.map(parse(_).get)
        .map(handler.handle)
        .takeWhile(identity) // Continue while handler return true
        .foreach(_ => {}) // Pull from iterator  

    }
    finally {
      Terminal.shutdown()
      Http().shutdownAllConnectionPools() andThen { case _ => system.terminate() }
    }
  }

}

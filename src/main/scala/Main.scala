package escli

import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.http.scaladsl.Http


object Main extends SimpleParser {
  def main(args: Array[String]): Unit = {

    val baseUrl = if (args.size > 0) args(0) else "http://127.0.0.1:9200"

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    val qexec = new QueryExecutor(baseUrl, Http().singleRequest(_))
    val handler = new CommandHandler(qexec.request)

    try {
      CommandScanner(Terminal("escli> ").scan())
        .map(w => handler.handleStatement(w))
        .takeWhile(identity)
        .foreach(_ => {})

    }
    finally {
      Terminal.shutdown()
      Http().shutdownAllConnectionPools() andThen { case _ => system.terminate() }
    }
  }

}

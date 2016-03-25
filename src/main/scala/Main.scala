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

    val handler = new CommandHandler(baseUrl, Http().singleRequest(_))

    try {
      CommandScanner(Terminal("escli> ").scan())
        .foreach(handler.handleStatement)
    } finally {
      Terminal.shutdown()
      Http().shutdownAllConnectionPools() andThen { case _ => system.terminate() }
    }
  }

}

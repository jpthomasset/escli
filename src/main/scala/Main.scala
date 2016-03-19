package escli

import akka.stream.ActorMaterializer
import akka.actor.ActorSystem

object Main extends SimpleParser {
  def main(args: Array[String]): Unit = {

    val baseUrl = if (args.size > 0) args(0) else "http://127.0.0.1:9200"

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val handler = new CommandHandler(baseUrl)

    val reader = new CommandReader("escli> ")
    reader.read(handler.handleStatement)

    handler.shutdown()
  }

}

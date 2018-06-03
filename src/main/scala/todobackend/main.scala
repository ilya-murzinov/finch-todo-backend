package todobackend

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.circe._

object Main extends Config with Filters {
  import scala.concurrent.ExecutionContext.Implicits.global

  val api: Service[Request, Response] =
    corsFilter.andThen(new Endpoints(externalUrl).apiEndpoint.toService)

  def main(args: Array[String]): Unit = {
    println(s"Setting up API server with host=$host, port=$port, externalUrl=$externalUrl...")
    val server = Http.server.serve(internalUrl, api)
    Await.ready(server)
  }
}

package todo.backend.minimal

import todo.backend.core.TodoMainApp

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.circe._

object Main extends TodoMainApp with Endpoints {
  val api: Service[Request, Response] =
    withCorsHeaders(endpoint(externalUrl)).toService

  val server = Http.server.serve(internalUrl, api)

  Await.ready(server)
}

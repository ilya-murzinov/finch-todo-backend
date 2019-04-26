package todobackend

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.IO._
import com.twitter.finagle.Http
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.circe._
import repository._

object Main extends IOApp with Config with Filters {

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- IO.delay(println(s"Setting up API server with host=$host, port=$port, externalUrl=$externalUrl..."))
    repo <- emptyState.map(new Repo(_))
    api = corsFilter.andThen(new Endpoints(externalUrl, repo).apiEndpoint.toService)
    server <- IO.delay(Http.server.serve(internalUrl, api))
    _ <- IO.delay(Await.ready(server))
  } yield ExitCode.Success
}

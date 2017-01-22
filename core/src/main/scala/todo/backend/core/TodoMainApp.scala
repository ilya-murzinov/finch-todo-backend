package todo.backend.core

import io.finch._

trait TodoMainApp extends App with Config {
  private[this] val host = serverConfig.getString("host")
  private[this] val port = serverConfig.getString("port")
  protected[todo] val internalUrl: String = s"$host:$port"
  protected[todo] val externalUrl = serverConfig.getString("externalUrl")

  protected[todo] def withCorsHeaders[T] = { endpoint: Endpoint[T] =>
    endpoint
      .withHeader(("Access-Control-Allow-Origin", "*"))
      .withHeader(
        ("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH")
      )
      .withHeader(("Access-Control-Max-Age", "3600"))
      .withHeader(("Access-Control-Allow-Headers", """Content-Type,
          |Cache-Control,
          |Content-Language,
          |Expires,
          |Last-Modified,
          |Pragma,
          |X-Requested-With,
          |Origin,
          |Accept
        """.stripMargin.filter(_ >= ' ')))
  }
}

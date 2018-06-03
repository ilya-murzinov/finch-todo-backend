package todobackend

trait Config {
  protected val host: String = prop("http.host", "localhost")
  protected val port: Int = prop("http.port", "8081").toInt
  protected val internalUrl: String = s"$host:$port"
  protected val externalUrl: String = prop("http.externalUrl", s"http://$host:$port")

  private[this] def prop(key: String, default: String): String =
    Option(System.getProperty(key)).filter(_.nonEmpty).getOrElse(default)
}
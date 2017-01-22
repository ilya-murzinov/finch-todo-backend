package todo.backend.core

import com.typesafe.config.ConfigFactory

trait Config {
  private val env = Option(System.getProperty("env")).getOrElse("dev")
  private[this] val config = ConfigFactory.load()
  val serverConfig = config.getConfig(env).getConfig("http")
}

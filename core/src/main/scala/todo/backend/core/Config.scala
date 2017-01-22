package todo.backend.core

import com.typesafe.config.ConfigFactory

trait Config {
  private[this] val config = ConfigFactory.load()
  val serverConfig = config.getConfig("http")
}

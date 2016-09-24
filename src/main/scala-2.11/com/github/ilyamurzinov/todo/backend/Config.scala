package com.github.ilyamurzinov.todo.backend

import com.typesafe.config.ConfigFactory

/**
  * @author Murzinov Ilya [murz42@gmail.com]
  */
trait Config {
  private[this] val config = ConfigFactory.load()
  val dbConfig = config.getConfig("mongo")
  val serverConfig = config.getConfig("http")
}

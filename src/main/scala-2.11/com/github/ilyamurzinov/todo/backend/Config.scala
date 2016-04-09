package com.github.ilyamurzinov.todo.backend

import com.typesafe.config.ConfigFactory

/**
  * @author Murzinov Ilya [murz42@gmail.com]
  */
trait Config {
  private[this] val env = Option(System.getProperty("env")).getOrElse("dev")
  private[this] val config = ConfigFactory.load().getConfig(env)
  val dbConfig = config.getConfig("mongo")
  val serverConfig = config.getConfig("http")
}

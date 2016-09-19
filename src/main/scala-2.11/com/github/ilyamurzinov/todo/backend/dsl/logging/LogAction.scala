package com.github.ilyamurzinov.todo.backend.dsl.logging

import com.github.ilyamurzinov.todo.backend.dsl._

import cats.free.{Free, Inject}
import cats.free.Free.liftF
import java.util.UUID

sealed trait LogAction[T]

case class Info(msg: String) extends LogAction[Unit]

class LogI[F[_]](implicit I: Inject[LogAction, F]) {
  def infoI(msg: String): Free[F, Unit] = Free.inject[LogAction, F](Info(msg))
}

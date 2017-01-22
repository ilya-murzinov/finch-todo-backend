package todo.backend.free.dsl.logging

import cats.free.{Free, Inject}

sealed trait LogAction[T]

case class Info(msg: String) extends LogAction[Unit]

class LogI[F[_]](implicit I: Inject[LogAction, F]) {
  def infoI(msg: String): Free[F, Unit] = Free.inject[LogAction, F](Info(msg))
}

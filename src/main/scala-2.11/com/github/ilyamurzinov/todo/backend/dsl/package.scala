package com.github.ilyamurzinov.todo.backend

import cats.free.{Free, Inject}
import cats.data.Coproduct
import com.github.ilyamurzinov.todo.backend.dsl.logging._
import com.github.ilyamurzinov.todo.backend.dsl.logic._
import com.github.ilyamurzinov.todo.backend.dsl.storage._

package object dsl {
  type LogF[T] = Free[LogAction, T]
  type TodoF[T] = Free[TodoAction, T]
  type StorageF[T] = Free[StorageAction, T]
  type TodoApp[T] = Coproduct[TodoAction, LogAction, T]

  implicit def logI[F[_]](implicit I: Inject[LogAction, F]): LogI[F] = new LogI[F]
  implicit def todoI[F[_]](implicit I: Inject[TodoAction, F]): TodoI[F] = new TodoI[F]
}

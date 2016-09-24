package com.github.ilyamurzinov.todo

import com.github.ilyamurzinov.todo.backend.dsl.Todo

import cats.Show
import io.circe.Encoder
import io.circe.Json
import io.circe.generic.auto._

package object backend {
  implicit def showTodo(implicit e: Encoder[Todo], s: Show[Json]): Show[Todo] =
    new Show[Todo] {
      override def show(t: Todo) = s.show(e(t))
    }
}

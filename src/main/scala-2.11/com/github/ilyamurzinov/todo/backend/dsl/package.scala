package com.github.ilyamurzinov.todo.backend

import cats.free.Free
import com.github.ilyamurzinov.todo.backend.dsl.logic._
import com.github.ilyamurzinov.todo.backend.dsl.storage._

package object dsl {
  type TodoF[T] = Free[TodoAction, T]
  type StorageF[T] = Free[StorageAction, T]
}

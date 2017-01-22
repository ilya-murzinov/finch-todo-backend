package todo.backend

import cats.Show
import io.circe.Encoder
import io.circe.Json

package object core {
  implicit def showTodo(implicit e: Encoder[Todo], s: Show[Json]): Show[Todo] =
    new Show[Todo] {
      override def show(t: Todo) = s.show(e(t))
    }
}

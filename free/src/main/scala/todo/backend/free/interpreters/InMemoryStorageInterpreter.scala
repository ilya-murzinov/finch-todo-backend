package todo.backend.free.interpreters

import java.util.UUID

import todo.backend.core._
import todo.backend.free.dsl._
import todo.backend.free.dsl.storage._

import cats.~>
import io.catbird.util._
import com.twitter.util.Future

class InMemoryStorageInterpreter extends (StorageF ~> Future) {
  private[this] var storage: Map[UUID, Todo] = Map()

  override def apply[A](action: StorageF[A]): Future[A] =
    action.foldMap(storageInterpreter)

  private[this] val storageInterpreter: StorageAction ~> Future =
    new (StorageAction ~> Future) {
      override def apply[A](action: StorageAction[A]): Future[A] =
        action match {
          case NoAction => Future.value(())
          case GetAllTodos => Future.value(storage.values.toList)
          case GetTodo(id) => Future.value(storage.get(id))
          case SaveTodo(todo: Todo) => {
            storage = storage + (todo.id -> todo)
            Future.value(())
          }
          case DeleteTodo(id) => {
            storage = storage - id
            Future.value(())
          }
          case DeleteAllTodos => {
            storage = Map()
            Future.value(())
          }
        }
    }
}

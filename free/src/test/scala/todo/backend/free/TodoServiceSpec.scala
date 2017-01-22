package todo.backend.free

import todo.backend.free.dsl.logic._
import todo.backend.free.dsl.logging._
import todo.backend.free.interpreters.TodoTestInterpreter

import cats._
import cats.instances.AllInstances
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.prop.PropertyChecks

class TodoServiceSpec
    extends WordSpec
    with MustMatchers
    with PropertyChecks
    with AllInstances {
  "TodoService" must {
    "get all todos" in {
      val s = new TodoService()
      val i = new TodoTestInterpreter()
      s.getAllTodos.foldMap(i)
      i.todoActionStorage must contain only GetAllTodos
      i.logActionStorage must contain only Info("Getting all Todos")
    }
  }
}

package com.github.ilyamurzinov.todo.backend.interpreters

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.logic._
import com.github.ilyamurzinov.todo.backend.dsl.logging._

import cats.~>
import cats.Id

class TodoTestInterpreter extends (TodoApp ~> Id) {
  var todoActionStorage = List[TodoAction[_]]()
  var logActionStorage = List[LogAction[_]]()

  val f: TodoAction ~> Id = new (TodoAction ~> Id) {
    override def apply[A](action: TodoAction[A]): Id[A] = {
      todoActionStorage = todoActionStorage :+ action
      action match {
        case GetAllTodos => List[Todo]()
        case logic.GetTodo(id) => None
        case logic.SaveTodo(todo) => todo
        case logic.PatchTodo(id, f) => None
        case logic.DeleteTodo(id) => None
        case logic.DeleteAllTodos => List[Todo]()
      }
    }
  }

  val g: LogAction ~> Id = new (LogAction ~> Id) {
    override def apply[A](action: LogAction[A]): Id[A] = {
      logActionStorage = logActionStorage :+ action
      action match {
        case Info(msg: String) => ()
      }
    }
  }

  override def apply[A](action: TodoApp[A]): Id[A] = action.fold[Id](f, g)
}

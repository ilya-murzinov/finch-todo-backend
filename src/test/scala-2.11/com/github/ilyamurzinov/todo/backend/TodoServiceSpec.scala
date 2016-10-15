package com.github.ilyamurzinov.todo.backend

import com.github.ilyamurzinov.todo.backend.dsl.logic._
import com.github.ilyamurzinov.todo.backend.dsl.logging._
import com.github.ilyamurzinov.todo.backend.interpreters.TodoTestInterpreter

import java.util.UUID

import cats.Alternative
import cats.Eq
import cats._
import cats.instances.AllInstances
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.prop.Checkers
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

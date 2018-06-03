package todobackend

import io.finch.Input
import io.circe.generic.auto._
import io.finch.circe._
import org.scalatest._
import org.scalatest.prop.PropertyChecks

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class TodoEndpointsTests extends PropSpec with PropertyChecks with Matchers {
  import gens._

  val externalUrl = "http://example.com"
  val root = "/todos"
  val endpoints = new Endpoints(externalUrl)

  property("GET /todos/:id should return todo") {
    forAll { todo: TodoItem =>
      repo.saveTodo(todo)
      val input = Input.get(s"$root/${todo.id}")
      val response = endpoints.getEndpoint(input).awaitValueUnsafe()
      repo.deleteAllTodos()

      response.get shouldBe todo
    }
  }

  property("GET /todos should return todos") {
    forAll { todos: List[TodoItem] =>
      todos.foreach(repo.saveTodo)
      val input = Input.get(root)
      val response = endpoints.getTodosEndpoint(input).awaitValueUnsafe()
      repo.deleteAllTodos()

      response.get.toSet shouldBe todos.toSet
    }
  }

  property("POST /todos should add todo") {
    forAll { (todos: List[TodoItem], newTodo: TodoItem) =>
      todos.foreach(repo.saveTodo)
      val input = Input.post(root).withBody(newTodo)
      val response = endpoints.postTodo(input).awaitValueUnsafe()
      repo.deleteAllTodos()

      response.get shouldBe newTodo
    }
  }

  property("PATCH /todos/:id should update todo") {
    case class Update(completed: Boolean)

    forAll { (todos: List[TodoItem], newTodo: TodoItem) =>
      (todos :+ newTodo).foreach(repo.saveTodo)
      val input = Input.patch(s"$root/${newTodo.id}").withBody(Update(!newTodo.completed))
      val response = endpoints.patchTodo(input).awaitValueUnsafe()
      val updatedTodos = Await.result(repo.getAllTodos, Duration.Inf)
      val updatedTodo = newTodo.copy(completed = !newTodo.completed)
      repo.deleteAllTodos()

      response.get shouldBe updatedTodo
      updatedTodos.toSet shouldBe todos.toSet + updatedTodo
    }
  }

  property("DELETE /todos/:id should delete todo") {
    forAll { (todos: List[TodoItem], newTodo: TodoItem) =>
      (todos :+ newTodo).foreach(repo.saveTodo)
      val input = Input.delete(s"$root/${newTodo.id}")
      val response = endpoints.deleteTodo(input).awaitValueUnsafe()
      val updatedTodos = Await.result(repo.getAllTodos, Duration.Inf)
      repo.deleteAllTodos()

      response.get shouldBe newTodo
      updatedTodos.toSet shouldBe todos.toSet
    }
  }

  property("DELETE /todos should delete todos") {
    forAll { todos: List[TodoItem] =>
      todos.foreach(repo.saveTodo)
      val input = Input.delete(s"$root")
      val response = endpoints.deleteTodos(input).awaitValueUnsafe()
      val updatedTodos = Await.result(repo.getAllTodos, Duration.Inf)
      repo.deleteAllTodos()

      response.get.toSet shouldBe todos.toSet
      updatedTodos shouldBe List()
    }
  }
}
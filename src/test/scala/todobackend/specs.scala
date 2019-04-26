package todobackend

import cats.effect.IO
import cats.implicits._
import io.circe.generic.auto._
import io.finch.Input._
import io.finch.circe._
import org.scalatest._
import org.scalatestplus.scalacheck._
import repository._

import scala.concurrent.ExecutionContext

class TodoEndpointsTests
  extends PropSpec
    with ScalaCheckPropertyChecks
    with Matchers {
  import gens._

  implicit val cs = IO.contextShift(ExecutionContext.global)

  val state = emptyState.unsafeRunSync()
  val repo = new Repo(state)
  val externalUrl = "http://example.com"
  val root = "/todos"
  val endpoints = new Endpoints(externalUrl, repo)

  property("GET /todos/:id should return todo") {
    forAll { todo: TodoItem =>
      val response = (for {
        _ <- repo.save(todo)
        input = get(s"$root/${todo.id}")
        r <- endpoints.getTodo(input).output.get
        _ <- repo.deleteAll()
      } yield r).unsafeRunSync()

      response.value shouldBe todo
    }
  }

  property("GET /todos should return todos") {
    forAll { todos: List[TodoItem] =>
      val response = (for {
        _ <- todos.map(repo.save).sequence
        input = get(root)
        r <- endpoints.getTodosEndpoint(input).output.get
        _ <- repo.deleteAll()
      } yield r).unsafeRunSync()

      response.value.toSet shouldBe todos.toSet
    }
  }

  property("POST /todos should add todo") {
    forAll { (todos: List[TodoItem], expected: CreateTodoItemRequest) =>
      val response = (for {
        _ <- todos.map(repo.save).sequence
        input = post(root).withBody(expected)
        r <- endpoints.postTodo(input).output.get
        _ <- repo.deleteAll()
      } yield r).unsafeRunSync()

      val actual = response.value
      actual.title shouldBe expected.title
      actual.order shouldBe expected.order.getOrElse(0)
      actual.completed shouldBe expected.completed.getOrElse(false)
    }
  }

  property("PATCH /todos/:id should update todo") {
    case class Update(completed: Boolean)

    forAll { (todos: List[TodoItem], otherTodo: TodoItem) =>
      val (response, updatedTodos) = (for {
        _ <- (todos :+ otherTodo).map(repo.save).sequence
        input = patch(s"$root/${otherTodo.id}").withBody(Update(!otherTodo.completed))
        r <- endpoints.patchTodo(input).output.get
        updated <- repo.getAll
        _ <- repo.deleteAll()
      } yield (r, updated)).unsafeRunSync()

      val updatedTodo = otherTodo.copy(completed = !otherTodo.completed)

      response.value shouldBe updatedTodo
      updatedTodos.toSet shouldBe todos.toSet + updatedTodo
    }
  }

  property("DELETE /todos/:id should delete todo") {
    forAll { (todos: List[TodoItem], otherTodo: TodoItem) =>
      val (response, updatedTodos) = (for {
        _ <- (todos :+ otherTodo).map(repo.save).sequence
        input = delete(s"$root/${otherTodo.id}")
        r <- endpoints.deleteTodo(input).output.get
        updated <- repo.getAll
        _ <- repo.deleteAll()
      } yield (r, updated)).unsafeRunSync()

      response.value shouldBe otherTodo
      updatedTodos.toSet shouldBe todos.toSet
    }
  }

  property("DELETE /todos should delete todos") {
    forAll { todos: List[TodoItem] =>
      val (response, updatedTodos) = (for {
        _ <- todos.map(repo.save).sequence
        input = delete(s"$root")
        r <- endpoints.deleteTodos(input).output.get
        updated <- repo.getAll
      } yield (r, updated)).unsafeRunSync()

      response.value.toSet shouldBe todos.toSet
      updatedTodos shouldBe List()
    }
  }
}

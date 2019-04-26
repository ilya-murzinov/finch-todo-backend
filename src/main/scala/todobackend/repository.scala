package todobackend

import java.util.UUID

import cats.implicits._
import cats.effect.{IO, Sync}
import cats.effect.concurrent.Ref
import repository._

object repository {

  type State = Map[UUID, Ref[IO, TodoItem]]

  val emptyState: IO[Ref[IO, State]] = Ref[IO].of(Map.empty[UUID, Ref[IO, TodoItem]])
}

class Repo(storage: Ref[IO, State])(implicit S: Sync[IO]) {

  def getAll: IO[List[TodoItem]] = for {
    values <- storage.get.map(_.values.toList)
    all <- values.map(_.get).sequence
  } yield all

  def get(id: UUID): IO[Either[TodoNotFound, TodoItem]] = for {
    map <- storage.get
    item <- map.get(id).map(_.get).sequence
  } yield item.toRight(TodoNotFound(id))

  def save(item: TodoItem): IO[TodoItem] = for {
    newRef <- Ref.of(item)
    _ <- storage.update(_ + (item.id -> newRef))
  } yield item

  def update(id: UUID, updateFn: TodoItem => TodoItem): IO[Either[TodoNotFound, TodoItem]] = for {
    map <- storage.get
    item <- map.get(id).map(ref => ref.modify(i => updateFn(i) -> updateFn(i))).sequence
  } yield item.toRight(TodoNotFound(id))

  def delete(id: UUID): IO[Either[TodoNotFound, TodoItem]] = for {
    map <- storage.get
    item <- map.get(id).map(i => storage.modify(m => (m - id) -> i)).sequence
    value <- item.map(_.get).sequence
  } yield value.toRight(TodoNotFound(id))

  def deleteAll(): IO[List[TodoItem]] =
    storage
      .modify(all => collection.Map.empty[UUID, Ref[IO, TodoItem]] -> all)
      .flatMap(_.values.toList.map(_.get).sequence)
}

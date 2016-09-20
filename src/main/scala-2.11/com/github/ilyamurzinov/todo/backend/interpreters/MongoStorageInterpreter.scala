package com.github.ilyamurzinov.todo.backend.interpreters

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.storage._
import com.github.ilyamurzinov.todo.backend.Config

import cats.~>
import com.twitter.bijection.twitter_util._
import com.twitter.util.Future
import reactivemongo.api._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{
  BSONDocument,
  BSONDocumentReader,
  BSONDocumentWriter
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Implementation of todo repository backed by MongoDB
  *
  * @author Murzinov Ilya [murz42@gmail.com]
  */
object MongoStorageInterpreter extends Config {
  implicit val reader: BSONDocumentReader[Todo] =
    new BSONDocumentReader[Todo] {
      override def read(bson: BSONDocument): Todo =
        bson.getAs[String]("_id").map(UUID.fromString) match {
          case Some(id) =>
            Todo(
              id = id,
              title = bson.getAs[String]("title").getOrElse(""),
              completed = bson.getAs[Boolean]("completed").getOrElse(false),
              order = bson.getAs[Int]("order").getOrElse(0),
              url = bson.getAs[String]("url").getOrElse("")
            )
          case None => throw new IllegalStateException("Todo without id!")
        }
    }

  implicit val writer: BSONDocumentWriter[Todo] =
    new BSONDocumentWriter[Todo] {
      override def write(t: Todo): BSONDocument = {
        BSONDocument(
          "_id" -> t.id.toString,
          "title" -> t.title,
          "completed" -> t.completed,
          "order" -> t.order,
          "url" -> t.url
        )
      }
    }

  val dbName = dbConfig.getString("dbName")
  val uri = dbConfig.getString("uri")

  val driver: MongoDriver = new MongoDriver
  val connection: MongoConnection = MongoConnection.parseURI(uri) match {
    case Success(parsedURI) => driver.connection(parsedURI)
    case Failure(ex) => throw new IllegalArgumentException(uri)
  }
  val db: DefaultDB = connection(dbName)
  val collection: BSONCollection = db("todos")

  private[this] def byId(id: UUID) = BSONDocument("_id" -> id.toString)

  val interpreter: StorageAction ~> Future = new (StorageAction ~> Future) {
    override def apply[A](action: StorageAction[A]): Future[A] =
      action match {
        case GetAllTodos => {
          UtilBijections
            .twitter2ScalaFuture[List[Todo]]
            .invert(
              collection.find(BSONDocument()).cursor[Todo]().collect[List]()
            )
        }
        case GetTodo(id: UUID) => {
          UtilBijections
            .twitter2ScalaFuture[Option[Todo]]
            .invert(
              collection.find(byId(id)).one[Todo]
            )
        }
        case SaveTodo(todo: Todo) => {
          collection.insert[Todo](todo)
          Future.value(())
        }
        case DeleteTodo(id) => {
          collection.remove(byId(id))
          Future.value(())
        }
        case DeleteAllTodos => {
          UtilBijections
            .twitter2ScalaFuture[Unit]
            .invert(
              collection.drop()
            )
        }
      }
  }
}

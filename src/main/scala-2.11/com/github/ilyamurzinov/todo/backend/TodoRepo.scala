package com.github.ilyamurzinov.todo.backend

import java.util.UUID

import com.twitter.bijection.twitter_util._
import com.twitter.util.Future
import reactivemongo.api._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Implementation of todo repository backed by MongoDB
  *
  * @author Murzinov Ilya [murz42@gmail.com]
  */
object TodoRepo extends Config {
  import com.github.ilyamurzinov.todo.backend.Todo._

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

  def get(id: UUID): Future[Option[Todo]] = {
    UtilBijections.twitter2ScalaFuture[Option[Todo]].invert(
      collection.find(byId(id)).one[Todo]
    )
  }

  def list(): Future[List[Todo]] = {
    UtilBijections.twitter2ScalaFuture[List[Todo]].invert(
      collection.find(BSONDocument()).cursor[Todo]().collect[List]()
    )
  }

  def save(t: Todo): Unit = {
    collection.insert[Todo](t)
  }

  def delete(id: UUID): Unit = {
    collection.remove(byId(id))
  }
}

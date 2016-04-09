package com.github.ilyamurzinov.todo.backend

import java.util.UUID

import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

case class Todo(id: UUID, title: String, completed: Boolean, order: Int, url: String)

object Todo {
  implicit val reader: BSONDocumentReader[Todo] = new BSONDocumentReader[Todo] {
    override def read(bson: BSONDocument): Todo =
      bson.getAs[String]("_id").map(UUID.fromString) match {
        case Some(id) => Todo(
          id = id,
          title = bson.getAs[String]("title").getOrElse(""),
          completed = bson.getAs[Boolean]("completed").getOrElse(false),
          order = bson.getAs[Int]("order").getOrElse(0),
          url = bson.getAs[String]("url").getOrElse("")
        )
        case None => throw new IllegalStateException("Todo without id!")
      }
  }

  implicit val writer: BSONDocumentWriter[Todo] = new BSONDocumentWriter[Todo] {
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
}

case class TodoNotFound(id: UUID) extends Exception {
  override def getMessage: String = s"Todo(${id.toString}) not found."
}

package com.github.ilyamurzinov.todo

import io.circe.{Encoder, Json}

package object backend {
  implicit val encodeException: Encoder[Exception] = Encoder.instance(e =>
    Json.obj(
      "type" -> Json.string(e.getClass.getSimpleName),
      "message" -> Json.string(e.getMessage)
    )
  )
}

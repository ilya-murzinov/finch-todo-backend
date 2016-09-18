package com.github.ilyamurzinov.todo.backend.dsl

import java.util.UUID

case class Todo(id: UUID,
                title: String,
                completed: Boolean,
                order: Int,
                url: String)

case class TodoNotFound(id: UUID)

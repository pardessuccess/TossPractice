package com.pardess.toss.data

import com.pardess.toss.data.dto.request.TodoRequest
import com.pardess.toss.data.dto.response.TodoResponse
import com.pardess.toss.domain.model.Todo

internal fun TodoResponse.toModel() = Todo(
    userId = this.userId,
    id = this.id,
    title = this.title,
    completed = this.completed
)

internal fun Todo.toRequest() = TodoRequest(
    userId = this.userId,
    id = this.id,
    title = this.title,
    completed = this.completed
)
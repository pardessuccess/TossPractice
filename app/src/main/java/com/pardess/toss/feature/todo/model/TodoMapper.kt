package com.pardess.toss.feature.todo.model

import com.pardess.toss.domain.entity.Todo

fun Todo.toUiModel() = TodoUiModel(
    id = id,
    title = title,
    completed = completed
)

fun TodoUiModel.toDomainModel(userId: Int): Todo {
    return Todo(
        userId = userId,
        id = id,
        title = title,
        completed = completed
    )
}
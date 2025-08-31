package com.pardess.toss.domain.repository

import com.pardess.toss.data.toModel
import com.pardess.toss.data.toRequest
import com.pardess.toss.domain.model.Todo
import kotlin.collections.map

interface TodoRepository {
    suspend fun getTodoList(): Result<List<Todo>>
    suspend fun getTodoById(id: Int): Result<Todo>
    suspend fun createTodo(todo: Todo): Result<Todo>
    suspend fun updateTodo(todo: Todo): Result<Todo>
    suspend fun deleteTodo(todoId: Int): Result<Unit>
}
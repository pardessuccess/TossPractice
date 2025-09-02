package com.pardess.toss.domain.repository

import com.pardess.toss.domain.entity.Todo

interface TodoRepository {
    suspend fun getTodoList(): Result<List<Todo>>
    suspend fun getTodoById(id: Int): Result<Todo>
    suspend fun createTodo(todo: Todo): Result<Todo>
    suspend fun updateTodo(todo: Todo): Result<Todo>
    suspend fun deleteTodo(todoId: Int): Result<Unit>
}
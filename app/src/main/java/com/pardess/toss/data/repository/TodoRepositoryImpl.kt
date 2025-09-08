package com.pardess.toss.data.repository

import com.pardess.toss.data.network.TossApiService
import com.pardess.toss.data.mapper.toModel
import com.pardess.toss.data.mapper.toRequest
import com.pardess.toss.data.util.executeApiCall
import com.pardess.toss.domain.entity.Todo
import com.pardess.toss.domain.repository.TodoRepository
import javax.inject.Inject


class TodoRepositoryImpl @Inject constructor(
    private val tossApiService: TossApiService,
) : TodoRepository {

    override suspend fun getTodoList(): Result<List<Todo>> {
        return executeApiCall {
            tossApiService.getTodoList()
        }.map { list ->
            list.map { it.toModel() }
        }
    }

    override suspend fun getTodoById(id: Int): Result<Todo> {
        return executeApiCall {
            tossApiService.getTodoById(id)
        }.map { it.toModel() }
    }

    override suspend fun createTodo(todo: Todo): Result<Todo> {
        return executeApiCall {
            tossApiService.create(todo.toRequest())
        }.map { it.toModel() }
    }

    override suspend fun updateTodo(todo: Todo): Result<Todo> {
        return executeApiCall {
            tossApiService.update(id = todo.id, todo = todo.toRequest())
        }.map { it.toModel() }
    }

    override suspend fun deleteTodo(todoId: Int): Result<Unit> {
        return executeApiCall {
            tossApiService.delete(todoId)
        }
    }
}
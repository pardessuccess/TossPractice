package com.pardess.toss.data.network

import com.pardess.toss.data.dto.request.TodoRequest
import com.pardess.toss.data.dto.response.TodoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TossApiService {

    @GET("todos")
    suspend fun getTodoList(): Response<List<TodoResponse>>

    @GET("todos/{id}")
    suspend fun getTodoById(@Path("id") id: Int): Response<TodoResponse>

    @POST("todos")
    suspend fun create(@Body todo: TodoRequest): Response<TodoResponse>

    @PUT("todos/{id}")
    suspend fun update(@Path("id") id: Int, @Body todo: TodoRequest): Response<TodoResponse>

    @DELETE("todos/{id}")
    suspend fun delete(@Path("id") id: Int): Response<Unit>

}
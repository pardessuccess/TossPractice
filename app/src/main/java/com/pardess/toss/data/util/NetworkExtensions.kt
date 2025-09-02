package com.pardess.toss.data.util

import com.pardess.toss.domain.exception.TodoException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

suspend inline fun <T> executeApiCall(
    crossinline apiCall: suspend () -> Response<T>
): Result<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            when {
                response.isSuccessful -> {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(TodoException.EmptyBodyError)
                }

                else -> {
                    Result.failure(
                        TodoException.NetworkError(
                            code = response.code(),
                            errorMessage = response.errorBody()?.string()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(TodoException.UnknownError(e.message ?: "Unknown error"))
        }
    }
}
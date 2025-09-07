package com.pardess.toss.data.util

import com.pardess.toss.domain.exception.TodoException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> executeApiCall(
    apiCall: suspend () -> Response<T>
): Result<T> = withContext(Dispatchers.IO) {
    try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body)
            } else {
                Result.failure(TodoException.EmptyBodyError)
            }
        } else {
            // 에러 메시지 추출 - 이미 IO 컨텍스트이므로 직접 실행
            val errorMsg = response.errorBody()?.use { errorBody ->
                errorBody.string()
            }

            // HTTP 상태 코드별 세분화된 예외 처리
            Result.failure(
                when (response.code()) {
                    401 -> TodoException.UnauthorizedError
                    403 -> TodoException.ForbiddenError
                    404 -> TodoException.NotFoundError
                    422 -> TodoException.ValidationError(errorMsg)
                    429 -> TodoException.TooManyRequestsError
                    in 500..599 -> TodoException.ServerError(errorMsg)
                    else -> TodoException.NetworkError(
                        code = response.code(),
                        errorMessage = errorMsg
                    )
                }
            )
        }
    } catch (e: CancellationException) {
        // 코루틴 취소는 그대로 전파
        throw e
    } catch (e: IOException) {
        // 네트워크 연결 문제
        Result.failure(TodoException.ConnectionError(e.message))
    } catch (e: Exception) {
        // 기타 예상치 못한 에러
        Result.failure(TodoException.UnknownError(e.message ?: "Unknown error"))
    }
}

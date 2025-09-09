package com.pardess.toss.data.util

import com.pardess.toss.domain.exception.TossException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

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
                Result.failure(TossException.EmptyBodyError)
            }
        } else {
            // 에러 메시지 추출 - 이미 IO 컨텍스트이므로 직접 실행
            val errorMsg = response.errorBody()?.use { errorBody ->
                errorBody.string()
            }

            // HTTP 상태 코드별 세분화된 예외 처리
            Result.failure(
                when (response.code()) {
                    401 -> TossException.UnauthorizedError
                    403 -> TossException.ForbiddenError
                    404 -> TossException.NotFoundError
                    422 -> TossException.ValidationError(errorMsg)
                    429 -> TossException.TooManyRequestsError
                    in 500..599 -> TossException.ServerError(errorMsg)
                    else -> TossException.NetworkError(
                        code = response.code(),
                        errorMessage = errorMsg
                    )
                }
            )
        }
    } catch (e: IOException) {
        // 네트워크 연결 문제
        Result.failure(TossException.ConnectionError(e.message))
    } catch (e: Exception) {
        // 기타 예상치 못한 에러
        Result.failure(TossException.UnknownError(e.message ?: "Unknown error"))
    }
}

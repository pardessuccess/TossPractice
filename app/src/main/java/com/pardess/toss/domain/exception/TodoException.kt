package com.pardess.toss.domain.exception

// 도메인 레이어로 이동 권장
sealed class TodoException : Exception() {
    // 성공했지만 body가 비어있는 경우
    object EmptyBodyError : TodoException() {
        override val message = "Response body is empty"
    }

    // HTTP 401 - 인증 실패
    object UnauthorizedError : TodoException() {
        override val message = "Authentication required"
    }

    // HTTP 403 - 권한 없음
    object ForbiddenError : TodoException() {
        override val message = "Access forbidden"
    }

    // HTTP 404 - 리소스를 찾을 수 없음
    object NotFoundError : TodoException() {
        override val message = "Resource not found"
    }

    // HTTP 422 - 유효성 검증 실패
    data class ValidationError(val details: String?) : TodoException() {
        override val message = "Validation failed: ${details ?: "Invalid input"}"
    }

    // HTTP 429 - 요청 제한 초과
    object TooManyRequestsError : TodoException() {
        override val message = "Too many requests. Please try again later"
    }

    // HTTP 5xx - 서버 에러
    data class ServerError(val errorMessage: String?) : TodoException() {
        override val message = "Server error: ${errorMessage ?: "Internal server error"}"
    }

    // 기타 HTTP 에러
    data class NetworkError(val code: Int, val errorMessage: String?) : TodoException() {
        override val message = "Network error ($code): ${errorMessage ?: "Unknown error"}"
    }

    // 네트워크 연결 문제
    data class ConnectionError(val details: String?) : TodoException() {
        override val message = "Connection failed: ${details ?: "Check your internet connection"}"
    }

    // 예상치 못한 에러
    data class UnknownError(override val message: String) : TodoException()
}

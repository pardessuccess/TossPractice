package com.pardess.toss.domain.exception

// 도메인 레이어로 이동 권장
sealed class TodoException : Exception() {
    data class NetworkError(val code: Int, val errorMessage: String?) : TodoException()
    data object EmptyBodyError : TodoException()
    data class UnknownError(override val message: String) : TodoException()
}

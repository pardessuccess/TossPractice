package com.pardess.toss.domain.model

import kotlinx.serialization.SerialName

data class Todo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

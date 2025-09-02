package com.pardess.toss.domain.entity

data class Todo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

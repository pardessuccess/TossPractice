package com.pardess.toss.feature.model

import androidx.compose.runtime.Immutable

@Immutable
data class TodoUiModel(
    val id: Int,
    val title: String,
    val completed: Boolean
)

package com.pardess.toss.feature.todo.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pardess.toss.domain.model.Todo
import com.pardess.toss.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoCreateViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoCreateUiState())
    val uiState: StateFlow<TodoCreateUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun createTodo(onSuccess: () -> Unit) {
        val title = _uiState.value.title.trim()

        if (title.isEmpty()) {
            _uiState.update { it.copy(error = "제목을 입력해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }

            val newTodo = Todo(
                id = 0, // 서버에서 자동 생성
                title = title,
                completed = false,
                userId = 0
            )

            todoRepository.createTodo(newTodo)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { exception ->
                    Log.d("TodoCreateViewModel", "createTodo: $exception")
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            error = exception.message ?: "할일 생성에 실패했습니다"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class TodoCreateUiState(
    val title: String = "",
    val isCreating: Boolean = false,
    val error: String? = null
)
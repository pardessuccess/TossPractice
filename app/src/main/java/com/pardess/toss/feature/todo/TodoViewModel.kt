package com.pardess.toss.feature.todo


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pardess.toss.domain.model.Todo
import com.pardess.toss.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        loadTodos()
    }

    fun loadTodos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            todoRepository.getTodoList()
                .onSuccess { todos ->
                    _uiState.value = _uiState.value.copy(
                        todos = todos,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    Log.d("TodoViewModel", "loadTodos: $exception")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "알 수 없는 오류가 발생했습니다"
                    )
                }
        }
    }

    fun toggleTodoComplete(todoId: Int) {
        viewModelScope.launch {
            val todo = _uiState.value.todos.find { it.id == todoId } ?: return@launch
            val updatedTodo = todo.copy(completed = !todo.completed)

            todoRepository.updateTodo(updatedTodo)
                .onSuccess {
                    // 로컬 상태 업데이트
                    val updatedTodos = _uiState.value.todos.map {
                        if (it.id == todoId) updatedTodo else it
                    }
                    _uiState.value = _uiState.value.copy(todos = updatedTodos)
                }
                .onFailure { exception ->
                    Log.d("TodoViewModel", "toggleTodoComplete: $exception")
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "할일 업데이트에 실패했습니다"
                    )
                }
        }
    }

    fun deleteTodo(todoId: Int) {
        viewModelScope.launch {
            todoRepository.deleteTodo(todoId)
                .onSuccess {
                    val updatedTodos = _uiState.value.todos.filter { it.id != todoId }
                    _uiState.value = _uiState.value.copy(todos = updatedTodos)
                }
                .onFailure { exception ->
                    Log.d("TodoViewModel", "deleteTodo: $exception")
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "할일 삭제에 실패했습니다"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
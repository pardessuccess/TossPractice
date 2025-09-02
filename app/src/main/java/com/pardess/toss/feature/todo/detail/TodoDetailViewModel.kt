package com.pardess.toss.feature.todo.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pardess.toss.domain.model.Todo
import com.pardess.toss.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val todoId: Int = checkNotNull(savedStateHandle["todoId"])

    private val _uiState = MutableStateFlow(TodoDetailUiState())
    val uiState: StateFlow<TodoDetailUiState> = _uiState.asStateFlow()

    init {
        loadTodoDetail()
    }

    fun handleAction(action: TodoDetailAction) {
        when(action){
            TodoDetailAction.DeleteDetail -> {
                deleteTodo {  }
            }
            TodoDetailAction.LoadDetail -> {
                loadTodoDetail()
            }
            TodoDetailAction.ToggleComplete -> {
                toggleComplete()
            }
        }
    }

    fun loadTodoDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            todoRepository.getTodoById(todoId)
                .onSuccess { todo ->
                    _uiState.update {
                        it.copy(
                            todo = todo,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    Log.d("TodoDetailViewModel", "loadTodoDetail: $exception")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "할일을 불러오는데 실패했습니다"
                        )
                    }
                }
        }
    }

    fun toggleComplete() {
        val currentTodo = _uiState.value.todo ?: return

        viewModelScope.launch {
            val updatedTodo = currentTodo.copy(completed = !currentTodo.completed)

            todoRepository.updateTodo(updatedTodo)
                .onSuccess {
                    _uiState.update { currentState ->
                        currentState.copy(todo = updatedTodo)
                    }
                }
                .onFailure { exception ->
                    Log.d("TodoDetailViewModel", "toggleComplete: $exception")
                    _uiState.update {
                        it.copy(error = exception.message ?: "할일 업데이트에 실패했습니다")
                    }
                }
        }
    }

    fun deleteTodo(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            todoRepository.deleteTodo(todoId)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { exception ->
                    Log.d("TodoDetailViewModel", "deleteTodo: $exception")
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            error = exception.message ?: "할일 삭제에 실패했습니다"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class TodoDetailUiState(
    val todo: Todo? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)

sealed interface TodoDetailAction {
    data object LoadDetail : TodoDetailAction
    data object DeleteDetail : TodoDetailAction
    data object ToggleComplete : TodoDetailAction
}
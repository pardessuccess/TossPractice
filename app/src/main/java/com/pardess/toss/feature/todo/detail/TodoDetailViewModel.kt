package com.pardess.toss.feature.todo.detail

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pardess.toss.domain.entity.Todo
import com.pardess.toss.domain.repository.TodoRepository
import com.pardess.toss.feature.model.TodoUiModel
import com.pardess.toss.feature.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _sideEffect = Channel<TodoDetailSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadTodoDetail()
    }

    fun handleAction(action: TodoDetailAction) {
        when (action) {
            TodoDetailAction.DeleteDetail -> {
                deleteTodo()
            }

            TodoDetailAction.LoadDetail -> {
                loadTodoDetail()
            }

            TodoDetailAction.ToggleComplete -> {
                toggleComplete()
            }

            TodoDetailAction.ClearError -> {
                clearError()
            }
        }
    }

    private fun loadTodoDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            todoRepository.getTodoById(todoId)
                .onSuccess { todo ->
                    _uiState.update {
                        it.copy(
                            todo = todo.toUiModel(),
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

    private fun toggleComplete() {
        viewModelScope.launch {
            val currentTodoUiModel = _uiState.value.todo ?: return@launch
            todoRepository.getTodoById(currentTodoUiModel.id)
                .onSuccess { originalTodo ->
                    val updatedTodo = originalTodo.copy(completed = !originalTodo.completed)

                    todoRepository.updateTodo(updatedTodo)
                        .onSuccess {
                            // UI 상태는 TodoUiModel로 업데이트
                            _uiState.update { currentState ->
                                currentState.copy(
                                    todo = currentTodoUiModel.copy(completed = updatedTodo.completed)
                                )
                            }
                        }
                        .onFailure { exception ->
                            Log.d("TodoDetailViewModel", "toggleComplete: $exception")
                            _uiState.update {
                                it.copy(error = exception.message ?: "할일 업데이트에 실패했습니다")
                            }
                        }
                }
                .onFailure { exception ->
                    Log.d("TodoDetailViewModel", "getTodoById failed: $exception")
                    _uiState.update {
                        it.copy(error = "할일 정보를 가져오는데 실패했습니다")
                    }
                }
        }
    }

    private fun deleteTodo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            todoRepository.deleteTodo(todoId)
                .onSuccess {
                    _sideEffect.send(TodoDetailSideEffect.DeleteSuccess)
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

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

@Immutable
data class TodoDetailUiState(
    val todo: TodoUiModel? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)

sealed interface TodoDetailAction {
    data object LoadDetail : TodoDetailAction
    data object DeleteDetail : TodoDetailAction
    data object ToggleComplete : TodoDetailAction
    data object ClearError: TodoDetailAction
}

sealed interface TodoDetailSideEffect {
    data object DeleteSuccess : TodoDetailSideEffect
}
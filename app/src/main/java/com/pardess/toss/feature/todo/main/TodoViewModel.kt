package com.pardess.toss.feature.todo.main

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<TodoSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        handleAction(TodoAction.LoadTodos)
    }

    fun handleAction(action: TodoAction) {
        when (action) {
            is TodoAction.LoadTodos -> loadTodos()
            is TodoAction.ToggleTodoComplete -> toggleTodoComplete(action.todoId)
            is TodoAction.DeleteTodo -> deleteTodo(action.todoId)
            is TodoAction.ClearError -> clearError()
        }
    }

    private fun loadTodos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            todoRepository.getTodoList()
                .onSuccess { todos ->
                    _uiState.update {
                        it.copy(
                            todos = todos.map { it.toUiModel() },
                            isLoading = false,
                            error = null
                        )
                    }
                    _sideEffect.send(TodoSideEffect.TodosLoaded)
                }
                .onFailure { exception ->
                    Log.d("TodoViewModel", "loadTodos: $exception")
                    val errorMessage = exception.message ?: "알 수 없는 오류가 발생했습니다"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                    _sideEffect.send(TodoSideEffect.ShowError(errorMessage))
                }
        }
    }

    private fun toggleTodoComplete(todoId: Int) {
        viewModelScope.launch {
            todoRepository.getTodoById(todoId)
                .onSuccess { todo ->
                    val updatedTodo = todo.copy(completed = !todo.completed)

                    todoRepository.updateTodo(updatedTodo)
                        .onSuccess {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    todos = currentState.todos.map {
                                        if (it.id == todoId) updatedTodo.toUiModel() else it
                                    }
                                )
                            }
                            _sideEffect.send(
                                TodoSideEffect.TodoStatusChanged(
                                    todoId = todoId,
                                    completed = updatedTodo.completed
                                )
                            )
                        }
                        .onFailure { exception ->
                            Log.d("TodoViewModel", "toggleTodoComplete: $exception")
                            val errorMessage = exception.message ?: "할일 업데이트에 실패했습니다"
                            _uiState.update {
                                it.copy(error = errorMessage)
                            }
                            _sideEffect.send(TodoSideEffect.ShowError(errorMessage))
                        }
                }.onFailure { exception ->
                    Log.d("TodoViewModel", "toggleTodoComplete: $exception")
                    val errorMessage = exception.message ?: "할일 업데이트에 실패했습니다"
                    _uiState.update {
                        it.copy(error = errorMessage)
                    }
                    _sideEffect.send(TodoSideEffect.ShowError(errorMessage))
                }
        }
    }

    private fun deleteTodo(todoId: Int) {
        viewModelScope.launch {
            val deletedTodo = _uiState.value.todos.find { it.id == todoId }

            todoRepository.deleteTodo(todoId)
                .onSuccess {
                    _uiState.update { currentState ->
                        currentState.copy(
                            todos = currentState.todos.filter { it.id != todoId }
                        )
                    }
                    deletedTodo?.let {
                        _sideEffect.send(TodoSideEffect.TodoDeleted(it.title))
                    }
                }
                .onFailure { exception ->
                    Log.d("TodoViewModel", "deleteTodo: $exception")
                    val errorMessage = exception.message ?: "할일 삭제에 실패했습니다"
                    _uiState.update {
                        it.copy(error = errorMessage)
                    }
                    _sideEffect.send(TodoSideEffect.ShowError(errorMessage))
                }
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

@Immutable
data class TodoUiState(
    val todos: List<TodoUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Actions
sealed interface TodoAction {
    data object LoadTodos : TodoAction
    data class ToggleTodoComplete(val todoId: Int) : TodoAction
    data class DeleteTodo(val todoId: Int) : TodoAction
    data object ClearError : TodoAction
}

// Side Effects
sealed interface TodoSideEffect {
    data object TodosLoaded : TodoSideEffect
    data class TodoStatusChanged(val todoId: Int, val completed: Boolean) : TodoSideEffect
    data class TodoDeleted(val todoTitle: String) : TodoSideEffect
    data class ShowError(val message: String) : TodoSideEffect
}
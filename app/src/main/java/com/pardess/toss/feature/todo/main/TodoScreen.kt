package com.pardess.toss.feature.todo.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pardess.toss.feature.todo.model.TodoUiModel

@Composable
fun TodoRoute(
    onTodoClick: (Int) -> Unit,
    onCreateClick: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Side Effect 처리
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                is TodoSideEffect.TodosLoaded -> {
                    // 필요시 추가 처리 (예: Analytics 로깅)
                }
                is TodoSideEffect.TodoStatusChanged -> {
                    val message = if (sideEffect.completed) {
                        "할일을 완료했습니다"
                    } else {
                        "할일을 미완료로 변경했습니다"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                is TodoSideEffect.TodoDeleted -> {
                    Toast.makeText(
                        context,
                        "${sideEffect.todoTitle}을(를) 삭제했습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is TodoSideEffect.ShowError -> {
                    // Snackbar로 표시되므로 Toast는 생략
                }
            }
        }
    }

    TodoScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
        onTodoClick = onTodoClick,
        onCreateClick = onCreateClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    uiState: TodoUiState,
    onAction: (TodoAction) -> Unit,
    onTodoClick: (Int) -> Unit,
    onCreateClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("할일 목록") }
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                FloatingActionButton(
                    onClick = onCreateClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "새 할일 추가"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.todos.isEmpty() && uiState.error == null -> {
                    EmptyTodoContent(
                        onCreateClick = onCreateClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.todos,
                            key = { it.id }
                        ) { todo ->
                            TodoItem(
                                todo = todo,
                                onToggle = { onAction(TodoAction.ToggleTodoComplete(todo.id)) },
                                onDelete = { onAction(TodoAction.DeleteTodo(todo.id)) },
                                onClick = { onTodoClick(todo.id) }
                            )
                        }
                    }
                }
            }

            // 에러 스낵바
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { onAction(TodoAction.LoadTodos) }) {
                            Text("다시 시도")
                        }
                    },
                    dismissAction = {
                        TextButton(onClick = { onAction(TodoAction.ClearError) }) {
                            Text("닫기")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun TodoItem(
    todo: TodoUiModel,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.completed,
                onCheckedChange = { onToggle() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyTodoContent(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "할일이 없습니다",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onCreateClick
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("첫 번째 할일 추가하기")
        }
    }
}
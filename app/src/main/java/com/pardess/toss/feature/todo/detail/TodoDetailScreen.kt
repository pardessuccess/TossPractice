package com.pardess.toss.feature.todo.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pardess.toss.domain.entity.Todo
import com.pardess.toss.feature.model.TodoUiModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TodoDetailRoute(
    onBackClick: () -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: TodoDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                TodoDetailSideEffect.DeleteSuccess -> onDeleteSuccess()
            }
        }
    }

    TodoDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onAction = viewModel::handleAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    uiState: TodoDetailUiState,
    onAction: (TodoDetailAction) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("할일 상세") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    if (uiState.todo != null && !uiState.isDeleting) {
                        IconButton(onClick = { onAction(TodoDetailAction.DeleteDetail) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
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

                uiState.todo != null -> {
                    TodoDetailContent(
                        todo = uiState.todo,
                        onToggleComplete = { onAction(TodoDetailAction.ToggleComplete) },
                        isDeleting = uiState.isDeleting
                    )
                }
            }

            // 에러 스낵바
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { onAction(TodoDetailAction.LoadDetail) }) {
                            Text("다시 시도")
                        }
                    },
                    dismissAction = {
                        TextButton(onClick = { onAction(TodoDetailAction.ClearError) }) {
                            Text("닫기")
                        }
                    }
                ) {
                    Text(error)
                }
            }

            // 삭제 중 인디케이터
            if (uiState.isDeleting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun TodoDetailContent(
    todo: TodoUiModel,
    onToggleComplete: () -> Unit,
    isDeleting: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = todo.completed,
                        onCheckedChange = { onToggleComplete() },
                        enabled = !isDeleting
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.headlineSmall,
                        textDecoration = if (todo.completed) TextDecoration.LineThrough else null
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ID: ${todo.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "상태: ${if (todo.completed) "완료" else "미완료"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (todo.completed) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
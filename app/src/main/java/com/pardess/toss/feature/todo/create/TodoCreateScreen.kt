package com.pardess.toss.feature.todo.create


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TodoCreateRoute(
    onBackClick: () -> Unit,
    onCreateSuccess: () -> Unit,
    viewModel: TodoCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                TodoCreateSideEffect.CreateSuccess -> {
                    onCreateSuccess()
                }
            }
        }
    }

    TodoCreateScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        handleAction = viewModel::handleAction,
        onDismissError = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoCreateScreen(
    uiState: TodoCreateUiState,
    onBackClick: () -> Unit,
    handleAction: (TodoCreateAction) -> Unit,
    onDismissError: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새 할일") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = {
                        handleAction(TodoCreateAction.UpdateTitle(it))
                    },
                    label = { Text("할일 제목") },
                    placeholder = { Text("무엇을 해야 하나요?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    enabled = !uiState.isCreating,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            handleAction(TodoCreateAction.CreateTodo)
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { handleAction(TodoCreateAction.CreateTodo) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isCreating && uiState.title.isNotBlank()
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("생성하기")
                    }
                }
            }

            // 에러 스낵바
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    dismissAction = {
                        TextButton(onClick = onDismissError) {
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

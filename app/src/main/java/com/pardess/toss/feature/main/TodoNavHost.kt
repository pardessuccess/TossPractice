package com.pardess.toss.feature.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pardess.toss.feature.todo.TodoRoute
import com.pardess.toss.feature.todo.create.TodoCreateRoute
import com.pardess.toss.feature.todo.detail.TodoDetailRoute

@Composable
fun TodoNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = "todo_list"
    ) {
        composable("todo_list") {
            TodoRoute(
                onTodoClick = { todoId ->
                    navController.navigate("todo_detail/$todoId")
                },
                onCreateClick = {
                    navController.navigate("todo_create")
                }
            )
        }

        composable(
            route = "todo_detail/{todoId}",
            arguments = listOf(navArgument("todoId") { type = NavType.IntType })
        ) {
            TodoDetailRoute(
                onBackClick = { navController.popBackStack() },
                onDeleteSuccess = { navController.popBackStack() }
            )
        }

        composable("todo_create") {
            TodoCreateRoute(
                onBackClick = { navController.popBackStack() },
                onCreateSuccess = { navController.popBackStack() }
            )
        }
    }
}
package com.example.todolist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todolist.presentation.ui.screen.EditTaskScreen
import com.example.todolist.presentation.ui.screen.TodoListScreen
import com.example.todolist.presentation.viewmodel.TodoListViewModel

object TodoDestination {
    const val TASK_LIST = "task_list"
    const val EDIT_TASK = "edit_task/{taskId}"
    const val TASK_ID_ARG = "taskId"
    const val NEW_TASK_ID = -1L

    fun editTaskRoute(taskId: Long?): String {
        return "edit_task/${taskId ?: NEW_TASK_ID}"
    }
}

@Composable
fun TodoNavGraph(
    viewModel: TodoListViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = TodoDestination.TASK_LIST,
        modifier = modifier
    ) {
        composable(route = TodoDestination.TASK_LIST) {
            TodoListScreen(
                tasks = uiState.tasks,
                isCompletedColorEnabled = uiState.isCompletedColorEnabled,
                onCompletedColorToggle = viewModel::onCompletedColorToggle,
                onTaskCheckedChange = viewModel::onTaskCheckedChanged,
                onTaskEditClick = { taskId ->
                    navController.navigate(TodoDestination.editTaskRoute(taskId))
                },
                onTaskDeleteClick = viewModel::onDeleteTask,
                onAddTaskClick = {
                    navController.navigate(TodoDestination.editTaskRoute(null))
                }
            )
        }

        composable(
            route = TodoDestination.EDIT_TASK,
            arguments = listOf(
                navArgument(TodoDestination.TASK_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val rawTaskId = backStackEntry.arguments?.getLong(TodoDestination.TASK_ID_ARG)
                ?: TodoDestination.NEW_TASK_ID
            val taskId = rawTaskId.takeIf { it != TodoDestination.NEW_TASK_ID }

            EditTaskScreen(
                taskId = taskId,
                loadTaskById = viewModel::getTask,
                onSaveTask = viewModel::onSaveTask,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

package com.example.todolist.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.todolist.domain.model.TodoTask
import com.example.todolist.presentation.ui.component.TodoTaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    tasks: List<TodoTask>,
    isCompletedColorEnabled: Boolean,
    onCompletedColorToggle: (Boolean) -> Unit,
    onTaskCheckedChange: (taskId: Long, isCompleted: Boolean) -> Unit,
    onTaskEditClick: (Long) -> Unit,
    onTaskDeleteClick: (Long) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Todo List") },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Цвет завершённых",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = isCompletedColorEnabled,
                            onCheckedChange = onCompletedColorToggle
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClick) {
                Text("+")
            }
        }
    ) { innerPadding ->
        if (tasks.isEmpty()) {
            EmptyTodoState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onAddTaskClick = onAddTaskClick
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = tasks,
                    key = { task -> task.id }
                ) { task ->
                    TodoTaskItem(
                        task = task,
                        isCompletedColorEnabled = isCompletedColorEnabled,
                        onCheckedChange = { isCompleted ->
                            onTaskCheckedChange(task.id, isCompleted)
                        },
                        onEditClick = { onTaskEditClick(task.id) },
                        onDeleteClick = { onTaskDeleteClick(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTodoState(
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Пока нет задач",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onAddTaskClick) {
                Text("Добавить первую задачу")
            }
        }
    }
}

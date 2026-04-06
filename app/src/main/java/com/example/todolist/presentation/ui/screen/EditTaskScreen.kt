package com.example.todolist.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.domain.model.TodoTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: Long?,
    loadTaskById: suspend (Long) -> TodoTask?,
    onSaveTask: (taskId: Long?, title: String, description: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable(taskId) { mutableStateOf("") }
    var description by rememberSaveable(taskId) { mutableStateOf("") }
    var isInitialized by rememberSaveable(taskId) { mutableStateOf(taskId == null) }

    LaunchedEffect(taskId) {
        if (taskId == null) return@LaunchedEffect
        val existingTask = loadTaskById(taskId)
        if (existingTask != null) {
            title = existingTask.title
            description = existingTask.description
        }
        isInitialized = true
    }

    val isTitleValid = title.trim().isNotBlank()
    val screenTitle = if (taskId == null) "Новая задача" else "Редактирование"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onSaveTask(taskId, title, description)
                            onNavigateBack()
                        },
                        enabled = isTitleValid
                    ) {
                        Text("Сохранить")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!isInitialized) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название задачи") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    label = { Text("Описание (опционально)") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNavigateBack) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = {
                            onSaveTask(taskId, title, description)
                            onNavigateBack()
                        },
                        enabled = isTitleValid
                    ) {
                        Text("Сохранить задачу")
                    }
                }
            }
        }
    }
}

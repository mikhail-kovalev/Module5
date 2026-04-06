package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.module5.diary.DiaryFeatureScreen
import com.example.module5.gallery.GalleryFeatureScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.navigation.TodoNavGraph
import com.example.todolist.presentation.viewmodel.TodoListViewModel
import com.example.todolist.ui.theme.TodoListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoListTheme {
                Module5Home()
            }
        }
    }
}

private enum class ModuleScreen {
    HOME,
    TASK_1_DIARY,
    TASK_2_3_GALLERY,
    TASK_4_TODO
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Module5Home() {
    var currentScreen by rememberSaveable { mutableStateOf(ModuleScreen.HOME) }
    val todoListViewModel: TodoListViewModel = viewModel()

    BackHandler(enabled = currentScreen != ModuleScreen.HOME) {
        currentScreen = ModuleScreen.HOME
    }

    when (currentScreen) {
        ModuleScreen.HOME -> {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("Module 5") })
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Выберите задание",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = { currentScreen = ModuleScreen.TASK_1_DIARY },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Задание 1: Дневник")
                    }
                    Button(
                        onClick = { currentScreen = ModuleScreen.TASK_2_3_GALLERY },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Задания 2-3: Галерея")
                    }
                    Button(
                        onClick = { currentScreen = ModuleScreen.TASK_4_TODO },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Задание 4: TodoList")
                    }
                }
            }
        }

        ModuleScreen.TASK_1_DIARY -> {
            DiaryFeatureScreen(modifier = Modifier.fillMaxSize())
        }

        ModuleScreen.TASK_2_3_GALLERY -> {
            GalleryFeatureScreen(modifier = Modifier.fillMaxSize())
        }

        ModuleScreen.TASK_4_TODO -> {
            TodoNavGraph(
                viewModel = todoListViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

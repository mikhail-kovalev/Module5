package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
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
                val todoListViewModel: TodoListViewModel = viewModel()
                TodoNavGraph(
                    viewModel = todoListViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

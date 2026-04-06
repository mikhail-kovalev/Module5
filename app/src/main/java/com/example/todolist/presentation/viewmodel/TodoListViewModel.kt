package com.example.todolist.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.local.TodoDatabase
import com.example.todolist.data.preferences.TodoPreferencesDataSource
import com.example.todolist.data.repository.TaskRepositoryImpl
import com.example.todolist.data.repository.UiSettingsRepositoryImpl
import com.example.todolist.domain.model.TodoTask
import com.example.todolist.domain.repository.TaskRepository
import com.example.todolist.domain.repository.UiSettingsRepository
import com.example.todolist.domain.usecase.DeleteTaskUseCase
import com.example.todolist.domain.usecase.GetTaskUseCase
import com.example.todolist.domain.usecase.InitializeTasksUseCase
import com.example.todolist.domain.usecase.ObserveCompletedColorUseCase
import com.example.todolist.domain.usecase.ObserveTasksUseCase
import com.example.todolist.domain.usecase.SaveTaskUseCase
import com.example.todolist.domain.usecase.SetCompletedColorUseCase
import com.example.todolist.domain.usecase.ToggleTaskCompletedUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodoListUiState(
    val tasks: List<TodoTask> = emptyList(),
    val isCompletedColorEnabled: Boolean = false
)

class TodoListViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository: TaskRepository
    private val uiSettingsRepository: UiSettingsRepository

    private val initializeTasksUseCase: InitializeTasksUseCase
    private val observeTasksUseCase: ObserveTasksUseCase
    private val saveTaskUseCase: SaveTaskUseCase
    private val deleteTaskUseCase: DeleteTaskUseCase
    private val toggleTaskCompletedUseCase: ToggleTaskCompletedUseCase
    private val getTaskUseCase: GetTaskUseCase
    private val observeCompletedColorUseCase: ObserveCompletedColorUseCase
    private val setCompletedColorUseCase: SetCompletedColorUseCase

    val uiState: StateFlow<TodoListUiState>

    init {
        val preferencesDataSource = TodoPreferencesDataSource(application)
        val database = TodoDatabase.getInstance(application)

        taskRepository = TaskRepositoryImpl(
            taskDao = database.taskDao(),
            assetManager = application.assets,
            preferencesDataSource = preferencesDataSource
        )
        uiSettingsRepository = UiSettingsRepositoryImpl(preferencesDataSource)

        initializeTasksUseCase = InitializeTasksUseCase(taskRepository)
        observeTasksUseCase = ObserveTasksUseCase(taskRepository)
        saveTaskUseCase = SaveTaskUseCase(taskRepository)
        deleteTaskUseCase = DeleteTaskUseCase(taskRepository)
        toggleTaskCompletedUseCase = ToggleTaskCompletedUseCase(taskRepository)
        getTaskUseCase = GetTaskUseCase(taskRepository)
        observeCompletedColorUseCase = ObserveCompletedColorUseCase(uiSettingsRepository)
        setCompletedColorUseCase = SetCompletedColorUseCase(uiSettingsRepository)

        uiState = combine(
            observeTasksUseCase(),
            observeCompletedColorUseCase()
        ) { tasks, completedColorEnabled ->
            TodoListUiState(
                tasks = tasks,
                isCompletedColorEnabled = completedColorEnabled
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodoListUiState()
        )

        viewModelScope.launch {
            initializeTasksUseCase()
        }
    }

    fun onTaskCheckedChanged(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            toggleTaskCompletedUseCase(taskId, isCompleted)
        }
    }

    fun onDeleteTask(taskId: Long) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }

    fun onSaveTask(taskId: Long?, title: String, description: String) {
        viewModelScope.launch {
            saveTaskUseCase(taskId, title, description)
        }
    }

    fun onCompletedColorToggle(enabled: Boolean) {
        viewModelScope.launch {
            setCompletedColorUseCase(enabled)
        }
    }

    suspend fun getTask(taskId: Long): TodoTask? {
        return getTaskUseCase(taskId)
    }
}

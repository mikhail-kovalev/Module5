package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.TodoTask
import com.example.todolist.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class ObserveTasksUseCase(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<TodoTask>> {
        return taskRepository.observeTasks()
    }
}

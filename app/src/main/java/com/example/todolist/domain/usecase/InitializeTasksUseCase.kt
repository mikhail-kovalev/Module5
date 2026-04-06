package com.example.todolist.domain.usecase

import com.example.todolist.domain.repository.TaskRepository

class InitializeTasksUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke() {
        taskRepository.importTasksFromJsonIfNeeded()
    }
}

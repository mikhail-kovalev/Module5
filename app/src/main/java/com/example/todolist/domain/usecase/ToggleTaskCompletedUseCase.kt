package com.example.todolist.domain.usecase

import com.example.todolist.domain.repository.TaskRepository

class ToggleTaskCompletedUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, isCompleted: Boolean) {
        taskRepository.setTaskCompleted(taskId, isCompleted)
    }
}

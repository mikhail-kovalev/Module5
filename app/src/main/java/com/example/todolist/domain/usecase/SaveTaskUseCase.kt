package com.example.todolist.domain.usecase

import com.example.todolist.domain.repository.TaskRepository

class SaveTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long?, title: String, description: String) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) return
        val normalizedDescription = description.trim()

        if (taskId == null) {
            taskRepository.addTask(
                title = normalizedTitle,
                description = normalizedDescription
            )
        } else {
            taskRepository.updateTask(
                taskId = taskId,
                title = normalizedTitle,
                description = normalizedDescription
            )
        }
    }
}

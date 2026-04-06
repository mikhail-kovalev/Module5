package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.TodoTask
import com.example.todolist.domain.repository.TaskRepository

class GetTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long): TodoTask? {
        return taskRepository.getTaskById(taskId)
    }
}

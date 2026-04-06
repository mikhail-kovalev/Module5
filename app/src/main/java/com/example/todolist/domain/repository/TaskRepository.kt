package com.example.todolist.domain.repository

import com.example.todolist.domain.model.TodoTask
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<TodoTask>>
    suspend fun getTaskById(taskId: Long): TodoTask?
    suspend fun addTask(title: String, description: String)
    suspend fun updateTask(taskId: Long, title: String, description: String)
    suspend fun deleteTask(taskId: Long)
    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean)
    suspend fun importTasksFromJsonIfNeeded()
}

package com.example.todolist.domain.model

data class TodoTask(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

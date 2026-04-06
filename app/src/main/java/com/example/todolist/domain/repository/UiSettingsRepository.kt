package com.example.todolist.domain.repository

import kotlinx.coroutines.flow.Flow

interface UiSettingsRepository {
    fun observeCompletedColorEnabled(): Flow<Boolean>
    suspend fun setCompletedColorEnabled(enabled: Boolean)
}

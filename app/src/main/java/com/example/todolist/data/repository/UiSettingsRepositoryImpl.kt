package com.example.todolist.data.repository

import com.example.todolist.data.preferences.TodoPreferencesDataSource
import com.example.todolist.domain.repository.UiSettingsRepository
import kotlinx.coroutines.flow.Flow

class UiSettingsRepositoryImpl(
    private val preferencesDataSource: TodoPreferencesDataSource
) : UiSettingsRepository {

    override fun observeCompletedColorEnabled(): Flow<Boolean> {
        return preferencesDataSource.observeCompletedColorEnabled()
    }

    override suspend fun setCompletedColorEnabled(enabled: Boolean) {
        preferencesDataSource.setCompletedColorEnabled(enabled)
    }
}

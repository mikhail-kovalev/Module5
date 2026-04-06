package com.example.todolist.domain.usecase

import com.example.todolist.domain.repository.UiSettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveCompletedColorUseCase(
    private val uiSettingsRepository: UiSettingsRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return uiSettingsRepository.observeCompletedColorEnabled()
    }
}

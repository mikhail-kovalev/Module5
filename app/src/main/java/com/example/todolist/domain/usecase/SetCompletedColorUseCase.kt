package com.example.todolist.domain.usecase

import com.example.todolist.domain.repository.UiSettingsRepository

class SetCompletedColorUseCase(
    private val uiSettingsRepository: UiSettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        uiSettingsRepository.setCompletedColorEnabled(enabled)
    }
}

package com.example.module5.diary

data class DiaryEntry(
    val fileName: String,
    val title: String,
    val body: String,
    val timestamp: Long
) {
    val displayTitle: String
        get() = title.ifBlank { "Без заголовка" }

    val previewText: String
        get() {
            val normalized = body.trim().replace('\n', ' ')
            if (normalized.isBlank()) return "Пустая запись"
            if (normalized.length <= 40) return normalized
            return normalized.take(40) + "..."
        }
}

package com.example.module5.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val appFilesDir: File = application.filesDir

    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    init {
        loadAllEntriesFromDisk()
    }

    fun createEntry(title: String, body: String) {
        val cleanTitle = title.trim()
        val cleanBody = body.trimEnd()
        if (cleanTitle.isBlank() && cleanBody.isBlank()) return

        val timestamp = System.currentTimeMillis()
        val targetFile = createUniqueTargetFile(timestamp = timestamp, title = cleanTitle)
        val fileName = targetFile.name
        val serialized = serializeEntry(title = cleanTitle, body = cleanBody)

        viewModelScope.launch(Dispatchers.IO) {
            val writeResult = runCatching {
                targetFile.writeText(serialized, Charsets.UTF_8)
            }

            if (writeResult.isSuccess) {
                val newEntry = DiaryEntry(
                    fileName = fileName,
                    title = cleanTitle,
                    body = cleanBody,
                    timestamp = timestamp
                )
                _entries.update { current -> listOf(newEntry) + current }
            }
        }
    }

    fun updateEntry(fileName: String, title: String, body: String) {
        val cleanTitle = title.trim()
        val cleanBody = body.trimEnd()
        val targetFile = File(appFilesDir, fileName)

        viewModelScope.launch(Dispatchers.IO) {
            if (!targetFile.exists()) return@launch

            val writeResult = runCatching {
                targetFile.writeText(
                    serializeEntry(title = cleanTitle, body = cleanBody),
                    Charsets.UTF_8
                )
            }
            if (writeResult.isFailure) return@launch

            val updatedEntry = DiaryEntry(
                fileName = fileName,
                title = cleanTitle,
                body = cleanBody,
                timestamp = extractTimestampFromFileName(
                    fileName = fileName,
                    fallback = targetFile.lastModified()
                )
            )

            _entries.update { current ->
                val index = current.indexOfFirst { it.fileName == fileName }
                if (index == -1) {
                    listOf(updatedEntry) + current
                } else {
                    current.toMutableList().apply { this[index] = updatedEntry }
                }
            }
        }
    }

    fun deleteEntry(fileName: String) {
        val targetFile = File(appFilesDir, fileName)
        viewModelScope.launch(Dispatchers.IO) {
            val deleted = !targetFile.exists() || targetFile.delete()
            if (deleted) {
                _entries.update { current ->
                    current.filterNot { it.fileName == fileName }
                }
            }
        }
    }

    private fun loadAllEntriesFromDisk() {
        viewModelScope.launch(Dispatchers.IO) {
            val loaded = appFilesDir
                .listFiles()
                ?.asSequence()
                ?.filter { it.isFile && it.extension.equals("txt", ignoreCase = true) }
                ?.mapNotNull { readEntryFromFile(it) }
                ?.sortedByDescending { it.timestamp }
                ?.toList()
                .orEmpty()
            _entries.value = loaded
        }
    }

    private fun readEntryFromFile(file: File): DiaryEntry? {
        val text = runCatching {
            file.readText(Charsets.UTF_8)
        }.getOrNull() ?: return null

        val (title, body) = parseEntry(text)
        return DiaryEntry(
            fileName = file.name,
            title = title,
            body = body,
            timestamp = extractTimestampFromFileName(
                fileName = file.name,
                fallback = file.lastModified()
            )
        )
    }

    private fun createUniqueTargetFile(timestamp: Long, title: String): File {
        var attempt = 0
        while (true) {
            val candidateName = buildFileName(timestamp = timestamp, title = title, attempt = attempt)
            val candidateFile = File(appFilesDir, candidateName)
            if (!candidateFile.exists()) return candidateFile
            attempt += 1
        }
    }

    private fun buildFileName(timestamp: Long, title: String, attempt: Int): String {
        val normalizedTitle = title
            .lowercase(Locale.getDefault())
            .replace("[^\\p{L}\\p{Nd}]+".toRegex(), "_")
            .trim('_')
            .take(40)
        val baseName = if (normalizedTitle.isBlank()) {
            "$timestamp"
        } else {
            "${timestamp}_${normalizedTitle}"
        }
        val uniqueSuffix = if (attempt == 0) "" else "_$attempt"

        return "$baseName$uniqueSuffix.txt"
    }

    private fun extractTimestampFromFileName(fileName: String, fallback: Long): Long {
        val rawTimestamp = fileName
            .substringBefore('_')
            .substringBefore('.')
        return rawTimestamp.toLongOrNull() ?: fallback
    }

    private fun serializeEntry(title: String, body: String): String {
        return buildString {
            append(title)
            append('\n')
            append(body)
        }
    }

    private fun parseEntry(content: String): Pair<String, String> {
        val separatorIndex = content.indexOf('\n')
        if (separatorIndex < 0) return "" to content
        val title = content.substring(startIndex = 0, endIndex = separatorIndex)
        val body = content.substring(separatorIndex + 1)
        return title to body
    }
}

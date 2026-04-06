package com.example.todolist.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.todoDataStore by preferencesDataStore(name = "todo_preferences")

class TodoPreferencesDataSource(
    private val context: Context
) {

    fun observeCompletedColorEnabled(): Flow<Boolean> {
        return context.todoDataStore.data.map { preferences ->
            preferences[COMPLETED_COLOR_ENABLED_KEY] ?: false
        }
    }

    suspend fun setCompletedColorEnabled(enabled: Boolean) {
        context.todoDataStore.edit { preferences ->
            preferences[COMPLETED_COLOR_ENABLED_KEY] = enabled
        }
    }

    suspend fun isInitialImportDone(): Boolean {
        return context.todoDataStore.data.first()[INITIAL_IMPORT_DONE_KEY] ?: false
    }

    suspend fun setInitialImportDone(done: Boolean) {
        context.todoDataStore.edit { preferences ->
            preferences[INITIAL_IMPORT_DONE_KEY] = done
        }
    }

    private companion object {
        val COMPLETED_COLOR_ENABLED_KEY = booleanPreferencesKey("completed_color_enabled")
        val INITIAL_IMPORT_DONE_KEY = booleanPreferencesKey("initial_import_done")
    }
}

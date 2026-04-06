package com.example.todolist.data.repository

import android.content.res.AssetManager
import com.example.todolist.data.local.TaskDao
import com.example.todolist.data.local.TaskEntity
import com.example.todolist.data.local.toDomain
import com.example.todolist.data.model.SeedTaskDto
import com.example.todolist.data.preferences.TodoPreferencesDataSource
import com.example.todolist.domain.model.TodoTask
import com.example.todolist.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val assetManager: AssetManager,
    private val preferencesDataSource: TodoPreferencesDataSource
) : TaskRepository {

    override fun observeTasks(): Flow<List<TodoTask>> {
        return taskDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(taskId: Long): TodoTask? {
        return withContext(Dispatchers.IO) {
            taskDao.getById(taskId)?.toDomain()
        }
    }

    override suspend fun addTask(title: String, description: String) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            taskDao.insert(
                TaskEntity(
                    title = title,
                    description = description,
                    isCompleted = false,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    override suspend fun updateTask(taskId: Long, title: String, description: String) {
        withContext(Dispatchers.IO) {
            val existing = taskDao.getById(taskId) ?: return@withContext
            taskDao.update(
                existing.copy(
                    title = title,
                    description = description,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun deleteTask(taskId: Long) {
        withContext(Dispatchers.IO) {
            taskDao.deleteById(taskId)
        }
    }

    override suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean) {
        withContext(Dispatchers.IO) {
            val existing = taskDao.getById(taskId) ?: return@withContext
            taskDao.update(
                existing.copy(
                    isCompleted = isCompleted,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun importTasksFromJsonIfNeeded() {
        withContext(Dispatchers.IO) {
            if (preferencesDataSource.isInitialImportDone()) return@withContext

            if (taskDao.count() == 0) {
                val rawJson = loadSeedJson().orEmpty()
                val tasksFromJson = parseSeedTasks(rawJson)
                if (tasksFromJson.isNotEmpty()) {
                    val now = System.currentTimeMillis()
                    val entities = tasksFromJson.mapIndexed { index, task ->
                        TaskEntity(
                            title = task.title,
                            description = task.description,
                            isCompleted = task.isCompleted,
                            createdAt = now - index,
                            updatedAt = now - index
                        )
                    }
                    taskDao.insertAll(entities)
                }
            }

            preferencesDataSource.setInitialImportDone(true)
        }
    }

    private fun loadSeedJson(): String? {
        return runCatching {
            assetManager.open(SEED_FILE_NAME).bufferedReader().use { reader ->
                reader.readText()
            }
        }.getOrNull()
    }

    private fun parseSeedTasks(rawJson: String): List<SeedTaskDto> {
        if (rawJson.isBlank()) return emptyList()

        return runCatching {
            val jsonArray = JSONArray(rawJson)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(index) ?: continue
                    val title = jsonObject.optString("title").trim()
                    if (title.isBlank()) continue
                    add(
                        SeedTaskDto(
                            title = title,
                            description = jsonObject.optString("description").trim(),
                            isCompleted = jsonObject.optBoolean("isCompleted", false)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val SEED_FILE_NAME = "tasks_seed.json"
    }
}

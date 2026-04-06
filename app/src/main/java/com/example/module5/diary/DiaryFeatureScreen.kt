package com.example.module5.diary

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SCREEN_LIST = "list"
private const val SCREEN_EDITOR = "editor"

@Composable
fun DiaryFeatureScreen(
    modifier: Modifier = Modifier,
    diaryViewModel: DiaryViewModel = viewModel()
) {
    val entries by diaryViewModel.entries.collectAsState()
    var currentScreen by rememberSaveable { mutableStateOf(SCREEN_LIST) }
    var selectedFileName by rememberSaveable { mutableStateOf("") }
    var editorSessionId by rememberSaveable { mutableIntStateOf(0) }

    if (currentScreen == SCREEN_EDITOR) {
        BackHandler { currentScreen = SCREEN_LIST }
        val editedEntry = entries.firstOrNull { it.fileName == selectedFileName }
        key(editorSessionId) {
            DiaryEditorScreen(
                modifier = modifier,
                entry = editedEntry,
                onBack = { currentScreen = SCREEN_LIST },
                onSave = { title, body ->
                    if (editedEntry == null) {
                        diaryViewModel.createEntry(title = title, body = body)
                    } else {
                        diaryViewModel.updateEntry(
                            fileName = editedEntry.fileName,
                            title = title,
                            body = body
                        )
                    }
                    currentScreen = SCREEN_LIST
                }
            )
        }
    } else {
        DiaryListScreen(
            modifier = modifier,
            entries = entries,
            onCreateNew = {
                selectedFileName = ""
                editorSessionId += 1
                currentScreen = SCREEN_EDITOR
            },
            onOpenEntry = { entry ->
                selectedFileName = entry.fileName
                editorSessionId += 1
                currentScreen = SCREEN_EDITOR
            },
            onDeleteEntry = { entry ->
                diaryViewModel.deleteEntry(entry.fileName)
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DiaryListScreen(
    entries: List<DiaryEntry>,
    onCreateNew: () -> Unit,
    onOpenEntry: (DiaryEntry) -> Unit,
    onDeleteEntry: (DiaryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Задание 1: Дневник") },
                actions = {
                    TextButton(onClick = onCreateNew) { Text("+ Новая") }
                }
            )
        }
    ) { innerPadding ->
        if (entries.isEmpty()) {
            EmptyDiaryState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = entries,
                    key = { entry -> entry.fileName }
                ) { entry ->
                    DiaryEntryCard(
                        entry = entry,
                        onOpen = onOpenEntry,
                        onDelete = onDeleteEntry
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDiaryState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "У вас пока нет записей",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Нажмите +, чтобы создать первую",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiaryEntryCard(
    entry: DiaryEntry,
    onOpen: (DiaryEntry) -> Unit,
    onDelete: (DiaryEntry) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onOpen(entry) },
                onLongClick = { isMenuExpanded = true }
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = entry.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.previewText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDiaryDate(entry.timestamp),
                    style = MaterialTheme.typography.labelMedium
                )
                TextButton(onClick = { isMenuExpanded = true }) {
                    Text("Меню")
                }
            }
        }
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Удалить") },
                onClick = {
                    isMenuExpanded = false
                    onDelete(entry)
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DiaryEditorScreen(
    entry: DiaryEntry?,
    onBack: () -> Unit,
    onSave: (title: String, body: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable { mutableStateOf(entry?.title.orEmpty()) }
    var body by rememberSaveable { mutableStateOf(entry?.body.orEmpty()) }
    val canSave = entry != null || title.isNotBlank() || body.isNotBlank()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (entry == null) "Новая запись" else "Редактирование") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } },
                actions = {
                    TextButton(
                        onClick = { onSave(title.trim(), body.trimEnd()) },
                        enabled = canSave
                    ) {
                        Text("Сохранить")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Заголовок (опционально)") }
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = { Text("Ваша запись...") }
            )
        }
    }
}

private fun formatDiaryDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("ru", "RU"))
    return formatter.format(Date(timestamp))
}

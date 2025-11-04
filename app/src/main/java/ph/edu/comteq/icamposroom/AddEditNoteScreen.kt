package ph.edu.comteq.icamposroom.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ph.edu.comteq.icamposroom.Note
import ph.edu.comteq.icamposroom.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: NoteViewModel,
    noteId: Int,
    onNavigateUp: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }

    val scope = rememberCoroutineScope()
    val isNewNote = noteId == 0

    // Fetch the note if it's an existing one
    LaunchedEffect(noteId) {
        if (!isNewNote) {
            scope.launch {
                noteToEdit = viewModel.getNoteById(noteId)
                noteToEdit?.let {
                    title = it.title
                    content = it.content
                    category = it.category
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewNote) "Add Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Show Delete icon only if it's an existing note
                    if (!isNewNote) {
                        IconButton(onClick = {
                            scope.launch {
                                noteToEdit?.let {
                                    viewModel.delete(it)
                                }
                                onNavigateUp() // Go back after deleting
                            }
                        }) {
                            Icon(Icons.Default.Delete, "Delete Note")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    val currentTime = System.currentTimeMillis()
                    if (isNewNote) {
                        // Insert new note
                        viewModel.insert(
                            Note(
                                title = title.ifBlank { "Untitled" },
                                content = content,
                                category = category,
                                createdAt = currentTime,
                                updatedAt = currentTime
                            )
                        )
                    } else {
                        // Update existing note
                        noteToEdit?.let {
                            viewModel.update(
                                it.copy(
                                    title = title.ifBlank { "Untitled" },
                                    content = content,
                                    category = category,
                                    updatedAt = currentTime
                                )
                            )
                        }
                    }
                    onNavigateUp() // Go back after saving
                }
            }) {
                Icon(Icons.Default.Done, "Save Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("Untitled") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category (e.g., Work, Personal)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Take remaining space
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
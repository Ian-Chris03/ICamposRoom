package ph.edu.comteq.icamposroom.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ph.edu.comteq.icamposroom.NoteCard
import ph.edu.comteq.icamposroom.NoteViewModel
import ph.edu.comteq.icamposroom.NoteWithTags

@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier,
    onAddNoteClicked: () -> Unit,
    onNoteClicked: (Int) -> Unit // Renamed from onEditNote for consistency with MainActivity
) {
    val notesWithTags by viewModel.notesWithTags.collectAsState(initial = null)

    // Main content: List of notes with tags
    when {
        notesWithTags == null -> { // Loading
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        notesWithTags!!.isEmpty() -> { // Empty
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notes yet. Tap + to add one!", // Original message, now fits NavHost's FloatingActionButton
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        else -> { // Data loaded
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(notesWithTags!!) { noteWithTags ->
                    NoteCard(
                        note = noteWithTags.note,
                        tags = noteWithTags.tags,
                        // Make the whole card clickable for editing
                        modifier = Modifier.clickable {
                            onNoteClicked(noteWithTags.note.id)
                        },
                        // These actions are now handled by NoteCard's internal dropdown
                        onEdit = { onNoteClicked(noteWithTags.note.id) },
                        onDelete = { viewModel.deleteNoteById(noteWithTags.note.id) } // Direct call to ViewModel for delete
                    )
                }
            }
        }
    }
}

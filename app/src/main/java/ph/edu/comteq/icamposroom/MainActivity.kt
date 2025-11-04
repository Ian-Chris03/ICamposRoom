package ph.edu.comteq.icamposroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ph.edu.comteq.icamposroom.screens.AddEditNoteScreen
import ph.edu.comteq.icamposroom.screens.NoteListScreen
import ph.edu.comteq.icamposroom.ui.theme.ICamposRoomTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NoteViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ICamposRoomTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var searchQuery by remember { mutableStateOf("") }
                    var isSearchActive by remember { mutableStateOf(false) }

                    Scaffold(
                        topBar = {
                            if (isSearchActive) {
                                SearchBar(
                                    modifier = Modifier.fillMaxWidth(),
                                    query = searchQuery,
                                    onQueryChange = {
                                        searchQuery = it
                                        viewModel.searchNotes(it)
                                    },
                                    onSearch = {
                                        isSearchActive = false
                                    },
                                    active = true,
                                    onActiveChange = { shouldExpand ->
                                        if (!shouldExpand) {
                                            isSearchActive = false
                                            searchQuery = ""
                                            viewModel.searchNotes("")
                                        }
                                    },
                                    placeholder = { Text("Search notes...") },
                                    leadingIcon = {
                                        IconButton(onClick = {
                                            isSearchActive = false
                                            searchQuery = ""
                                            viewModel.searchNotes("")
                                        }) {
                                            Icon(
                                                Icons.Default.ArrowBack,
                                                contentDescription = "Close search"
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = {
                                                searchQuery = ""
                                                viewModel.searchNotes("")
                                            }) {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = "Clear search"
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    val searchResults by viewModel.notesWithTags.collectAsState(initial = emptyList())
                                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                        if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                                            Text("No notes found for \"$searchQuery\"")
                                        } else {
                                            NoteListContent(
                                                notesWithTags = searchResults,
                                                onNoteClicked = { noteId ->
                                                    navController.navigate("add_edit_note/$noteId")
                                                    isSearchActive = false
                                                },
                                                onDeleteNote = { noteId ->
                                                    viewModel.deleteNoteById(noteId)
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                TopAppBar(
                                    title = { Text("ICamposRoom") },
                                    actions = {
                                        IconButton(onClick = { isSearchActive = true }) {
                                            Icon(Icons.Filled.Search, "Search notes")
                                        }
                                    }
                                )
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = {
                                navController.navigate("add_edit_note/0")
                            }) {
                                Icon(Icons.Filled.Add, "Add new note")
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "note_list",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("note_list") {
                                NoteListScreen(
                                    viewModel = viewModel,
                                    onAddNoteClicked = {
                                        navController.navigate("add_edit_note/0")
                                    },
                                    onNoteClicked = { noteId ->
                                        navController.navigate("add_edit_note/$noteId")
                                    }
                                )
                            }

                            composable(
                                route = "add_edit_note/{noteId}",
                                arguments = listOf(navArgument("noteId") {
                                    type = NavType.IntType
                                })
                            ) { backStackEntry ->
                                val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                                AddEditNoteScreen(
                                    viewModel = viewModel,
                                    noteId = noteId,
                                    onNavigateUp = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteListContent(
    notesWithTags: List<NoteWithTags>,
    onNoteClicked: (Int) -> Unit,
    onDeleteNote: (Int) -> Unit
) {
    if (notesWithTags.isEmpty()) {
        Text("No notes to display.")
    } else {
        LazyColumn {
            items(notesWithTags) { noteWithTags ->
                NoteCard(
                    note = noteWithTags.note,
                    tags = noteWithTags.tags,
                    modifier = Modifier.fillMaxWidth(),
                    onEdit = { onNoteClicked(noteWithTags.note.id) },
                    onDelete = { onDeleteNote(noteWithTags.note.id) }
                )
            }
        }
    }
}
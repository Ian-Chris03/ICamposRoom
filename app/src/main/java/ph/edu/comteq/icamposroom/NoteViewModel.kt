package ph.edu.comteq.icamposroom

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class NoteViewModel(application: Application) : AndroidViewModel(application) {

    // Get an instance of the database and then the DAO from it
    private val noteDao: NoteDao = AppDatabase.getDatabase(application).noteDao()

    // Track what user is searching for
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Show all notes OR notes that match the query
    @OptIn(ExperimentalCoroutinesApi::class)
    val allNotes: Flow<List<Note>> = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            noteDao.getAllNotes()  // Show everything
        } else {
            noteDao.searchNotes(query)  // Show only matches
        }
    }

    // NEW: All notes WITH their tags
    @OptIn(ExperimentalCoroutinesApi::class)
    val notesWithTags: Flow<List<NoteWithTags>> = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            noteDao.getAllNotesWithTags()
        } else {
            noteDao.searchNotesWithTags(query)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<List<NoteWithTags>> = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            flowOf(emptyList()) // Return empty list if query is blank
        } else {
            noteDao.searchNotesWithTags(query)
        }
    }


    // Called when user types in search box
    fun searchNotes(query: String) {
        _searchQuery.value = query
    }
    
    fun saveNote(note: Note) = viewModelScope.launch {
        if (note.id == 0) {
            insert(note)
        } else {
            update(note)
        }
    }

    fun insert(note: Note) = viewModelScope.launch {
        noteDao.insertNote(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        noteDao.updateNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        noteDao.deleteNote(note)
    }

    fun deleteNoteById(noteId: Int) = viewModelScope.launch {
        noteDao.deleteNoteById(noteId)
    }

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun getNoteWithTags(noteId: Int): NoteWithTags? {
        return noteDao.getNoteWithTags(noteId)
    }

    // ==================== TAG FUNCTIONS ====================

    fun insertTag(tag: Tag) = viewModelScope.launch {
        noteDao.insertTag(tag)
    }

    fun updateTag(tag: Tag) = viewModelScope.launch {
        noteDao.updateTag(tag)
    }

    fun deleteTag(tag: Tag) = viewModelScope.launch {
        noteDao.deleteTag(tag)
    }

    // ==================== NOTE-TAG RELATIONSHIP FUNCTIONS ====================

    // Add a tag to a note
    fun addTagToNote(noteId: Int, tagId: Int) = viewModelScope.launch {
        noteDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
    }

    // Remove a tag from a note
    fun removeTagFromNote(noteId: Int, tagId: Int) = viewModelScope.launch {
        noteDao.deleteNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
    }

    // Get all notes that have a specific tag
    fun getNotesWithTag(tagId: Int): Flow<List<Note>> {
        return noteDao.getNotesWithTag(tagId)
    }
}

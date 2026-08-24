package com.example.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notes.data.InMemoryNoteStore
import com.example.notes.data.NoteStore
import com.example.notes.model.Note
import com.example.notes.model.NoteColor
import com.example.notes.model.NoteFilter
import com.example.notes.model.SampleNotes
import com.example.notes.model.matching
import com.example.notes.model.tagCounts

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val query: String = "",
    val filter: NoteFilter = NoteFilter.All,
    val activeTag: String? = null,
    /** Set when a note was just archived or deleted, so the UI can offer an undo. */
    val undo: UndoAction? = null,
) {
    val visibleNotes: List<Note> get() = notes.matching(query, filter, activeTag)
    val tags: List<Pair<String, Int>> get() = notes.tagCounts()
    val pinnedCount: Int get() = notes.count { it.isPinned && !it.isArchived }
    val openTaskCount: Int get() = notes.filterNot { it.isArchived }.sumOf { note ->
        note.tasks.count { !it.isDone }
    }
}

/** A reversible destructive edit: the note as it was, plus what to call the action. */
data class UndoAction(val label: String, val note: Note)

class NotesViewModel(
    private val store: NoteStore = InMemoryNoteStore(),
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    var state by mutableStateOf(NotesUiState())
        private set

    init {
        val loaded = store.load() ?: SampleNotes.seed(clock()).also(store::save)
        state = state.copy(notes = loaded)
    }

    fun setQuery(query: String) {
        state = state.copy(query = query)
    }

    fun setFilter(filter: NoteFilter) {
        state = state.copy(filter = filter)
    }

    /** Tapping the active tag again clears the tag filter. */
    fun toggleTag(tag: String) {
        state = state.copy(activeTag = if (state.activeTag == tag) null else tag)
    }

    fun createNote(): Note {
        val now = clock()
        val note = Note(
            id = (state.notes.maxOfOrNull { it.id } ?: 0L) + 1,
            title = "",
            body = "",
            createdAt = now,
            updatedAt = now,
        )
        commit(state.notes + note)
        return note
    }

    fun updateNote(id: Long, title: String? = null, body: String? = null, color: NoteColor? = null) {
        val existing = noteById(id) ?: return
        val updated = existing.copy(
            title = title ?: existing.title,
            body = body ?: existing.body,
            color = color ?: existing.color,
        )
        if (updated == existing) return
        replace(updated.copy(updatedAt = clock()))
    }

    fun setTags(id: Long, tags: Set<String>) {
        val existing = noteById(id) ?: return
        if (existing.tags == tags) return
        replace(existing.copy(tags = tags, updatedAt = clock()))
    }

    fun togglePinned(id: Long) {
        val existing = noteById(id) ?: return
        replace(existing.copy(isPinned = !existing.isPinned, updatedAt = clock()))
    }

    /** Flips the nth `- [ ]` line of the body without disturbing the rest of the text. */
    fun toggleTask(id: Long, taskIndex: Int) {
        val existing = noteById(id) ?: return
        var seen = -1
        val lines = existing.body.lines().map { line ->
            val task = com.example.notes.model.Task.parse(line) ?: return@map line
            seen++
            if (seen != taskIndex) return@map line
            val marker = if (task.isDone) "- [ ] " else "- [x] "
            line.takeWhile { it.isWhitespace() } + marker + task.text
        }
        replace(existing.copy(body = lines.joinToString("\n"), updatedAt = clock()))
    }

    fun setArchived(id: Long, archived: Boolean) {
        val existing = noteById(id) ?: return
        val updated = existing.copy(isArchived = archived, isPinned = existing.isPinned && !archived)
        commit(state.notes.map { if (it.id == id) updated.copy(updatedAt = clock()) else it })
        state = state.copy(
            undo = if (archived) UndoAction("Note archived", existing) else null,
        )
    }

    fun deleteNote(id: Long) {
        val existing = noteById(id) ?: return
        commit(state.notes.filterNot { it.id == id })
        state = state.copy(undo = UndoAction("Note deleted", existing))
    }

    /** Puts the last archived or deleted note back exactly as it was. */
    fun undo() {
        val pending = state.undo ?: return
        val restored = state.notes.none { it.id == pending.note.id }
        val notes = if (restored) {
            state.notes + pending.note
        } else {
            state.notes.map { if (it.id == pending.note.id) pending.note else it }
        }
        commit(notes)
        state = state.copy(undo = null)
    }

    fun dismissUndo() {
        state = state.copy(undo = null)
    }

    fun noteById(id: Long): Note? = state.notes.firstOrNull { it.id == id }

    private fun replace(note: Note) {
        commit(state.notes.map { if (it.id == note.id) note else it })
    }

    private fun commit(notes: List<Note>) {
        state = state.copy(notes = notes)
        store.save(notes)
    }

    companion object {
        fun factory(store: NoteStore): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = NotesViewModel(store) as T
        }
    }
}

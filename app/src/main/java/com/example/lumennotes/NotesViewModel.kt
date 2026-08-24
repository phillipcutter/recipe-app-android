package com.example.lumennotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lumennotes.data.NoteStore
import com.example.lumennotes.model.Note
import com.example.lumennotes.model.noteOrder
import java.util.UUID

class NotesViewModel(app: Application) : AndroidViewModel(app) {

    private val store = NoteStore(app)

    var notes by mutableStateOf(store.load().sortedWith(noteOrder))
        private set

    var query by mutableStateOf("")

    /** Set when a note is deleted so the UI can offer an undo. */
    var lastDeleted by mutableStateOf<Note?>(null)
        private set

    val visibleNotes: List<Note>
        get() = notes.filter { it.matches(query) }

    val pinnedCount: Int
        get() = notes.count { it.pinned }

    fun find(id: String?): Note? = notes.firstOrNull { it.id == id }

    fun upsert(id: String?, title: String, body: String, accent: Int, pinned: Boolean): String {
        val noteId = id ?: UUID.randomUUID().toString()
        val updated = Note(
            id = noteId,
            title = title,
            body = body,
            accent = accent,
            pinned = pinned,
            updatedAt = System.currentTimeMillis(),
        )
        commit(notes.filterNot { it.id == noteId } + updated)
        return noteId
    }

    fun togglePin(id: String) {
        commit(notes.map { if (it.id == id) it.copy(pinned = !it.pinned) else it })
    }

    fun delete(id: String) {
        lastDeleted = find(id)
        commit(notes.filterNot { it.id == id })
    }

    fun undoDelete() {
        lastDeleted?.let { restored -> commit(notes + restored) }
        lastDeleted = null
    }

    fun clearUndo() {
        lastDeleted = null
    }

    private fun commit(next: List<Note>) {
        notes = next.sortedWith(noteOrder)
        store.save(notes)
    }
}

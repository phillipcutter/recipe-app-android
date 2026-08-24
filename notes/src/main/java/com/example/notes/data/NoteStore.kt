package com.example.notes.data

import android.content.Context
import com.example.notes.model.Note

/** Where notes live between launches. Swappable so the ViewModel stays testable. */
interface NoteStore {
    fun load(): List<Note>?
    fun save(notes: List<Note>)
}

/** Keeps notes in memory only. Used by previews and tests. */
class InMemoryNoteStore(private var notes: List<Note>? = null) : NoteStore {
    override fun load(): List<Note>? = notes
    override fun save(notes: List<Note>) {
        this.notes = notes
    }
}

/**
 * Backs the note list with SharedPreferences. Small enough for a notebook app and keeps the
 * module free of a database dependency.
 */
class PreferencesNoteStore(context: Context) : NoteStore {
    private val preferences =
        context.applicationContext.getSharedPreferences("notebook", Context.MODE_PRIVATE)

    override fun load(): List<Note>? =
        preferences.getString(KEY_NOTES, null)?.let(NoteJson::decode)

    override fun save(notes: List<Note>) {
        preferences.edit().putString(KEY_NOTES, NoteJson.encode(notes)).apply()
    }

    private companion object {
        const val KEY_NOTES = "notes.json"
    }
}

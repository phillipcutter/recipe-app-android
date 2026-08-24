package com.example.lumennotes.data

import android.content.Context
import com.example.lumennotes.model.Note
import org.json.JSONArray
import java.io.File

/** Plain-file JSON persistence — small enough that a database would be overkill. */
class NoteStore(context: Context) {

    private val file = File(context.filesDir, "notes.json")

    fun load(): List<Note> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val array = JSONArray(file.readText())
            (0 until array.length()).map { Note.fromJson(array.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    fun save(notes: List<Note>) {
        runCatching {
            val array = JSONArray()
            notes.forEach { array.put(it.toJson()) }
            file.writeText(array.toString())
        }
    }
}

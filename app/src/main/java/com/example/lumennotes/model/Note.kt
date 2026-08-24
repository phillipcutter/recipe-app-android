package com.example.lumennotes.model

import org.json.JSONObject

/** A single note. [id] is stable across edits; [updatedAt] drives list ordering. */
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val accent: Int,
    val pinned: Boolean = false,
    val updatedAt: Long = 0L,
) {
    val preview: String
        get() = body.trim().replace(Regex("\\s+"), " ")

    val displayTitle: String
        get() = title.trim().ifEmpty { preview.take(32).ifEmpty { "Untitled note" } }

    val wordCount: Int
        get() = body.trim().split(Regex("\\s+")).count { it.isNotEmpty() }

    fun matches(query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        return title.contains(q, ignoreCase = true) || body.contains(q, ignoreCase = true)
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("body", body)
        .put("accent", accent)
        .put("pinned", pinned)
        .put("updatedAt", updatedAt)

    companion object {
        fun fromJson(o: JSONObject) = Note(
            id = o.getString("id"),
            title = o.optString("title"),
            body = o.optString("body"),
            accent = o.optInt("accent"),
            pinned = o.optBoolean("pinned"),
            updatedAt = o.optLong("updatedAt"),
        )
    }
}

/** Pinned notes first, then most recently edited. */
val noteOrder: Comparator<Note> =
    compareByDescending<Note> { it.pinned }.thenByDescending { it.updatedAt }

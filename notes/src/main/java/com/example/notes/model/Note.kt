package com.example.notes.model

/** Palette a note can be tinted with. Stored by [key] so the value survives reordering. */
enum class NoteColor(val key: String, val label: String) {
    Default("default", "Paper"),
    Amber("amber", "Amber"),
    Rose("rose", "Rose"),
    Mint("mint", "Mint"),
    Sky("sky", "Sky"),
    Violet("violet", "Violet");

    companion object {
        fun fromKey(key: String): NoteColor = entries.firstOrNull { it.key == key } ?: Default
    }
}

enum class NoteFilter(val label: String) {
    All("All"),
    Pinned("Pinned"),
    Tasks("Tasks"),
    Archived("Archived"),
}

data class Note(
    val id: Long,
    val title: String,
    val body: String,
    val color: NoteColor = NoteColor.Default,
    val tags: Set<String> = emptySet(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
) {
    /** Checklist lines, in body order. A line counts when it starts with `- [ ]` or `- [x]`. */
    val tasks: List<Task> get() = body.lines().mapNotNull(Task::parse)

    val doneTasks: Int get() = tasks.count { it.isDone }

    val wordCount: Int get() = body.split(Regex("\\s+")).count { it.isNotBlank() }

    /** First non-empty body line that is not the title, used for the card preview. */
    val preview: String
        get() = body.lines().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty()

    val displayTitle: String
        get() = title.trim().ifEmpty { preview.take(40).ifEmpty { "Untitled note" } }

    fun matches(query: String): Boolean {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return true
        return title.lowercase().contains(needle) ||
            body.lowercase().contains(needle) ||
            tags.any { it.lowercase().contains(needle) }
    }
}

/** A single `- [ ] something` line inside a note body. */
data class Task(val text: String, val isDone: Boolean) {
    companion object {
        private val PATTERN = Regex("^\\s*-\\s\\[( |x|X)]\\s?(.*)$")

        fun parse(line: String): Task? {
            val match = PATTERN.matchEntire(line) ?: return null
            return Task(text = match.groupValues[2].trim(), isDone = !match.groupValues[1].isBlank())
        }
    }
}

/**
 * Search + filter + sort in one pass. Pinned notes float to the top, then most recently
 * edited first, so the list matches what the user last touched.
 */
fun List<Note>.matching(query: String, filter: NoteFilter, tag: String? = null): List<Note> =
    asSequence()
        .filter { note ->
            when (filter) {
                NoteFilter.All -> !note.isArchived
                NoteFilter.Pinned -> !note.isArchived && note.isPinned
                NoteFilter.Tasks -> !note.isArchived && note.tasks.isNotEmpty()
                NoteFilter.Archived -> note.isArchived
            }
        }
        .filter { tag == null || it.tags.contains(tag) }
        .filter { it.matches(query) }
        .sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
        .toList()

/** Every tag in use, most common first, so the chip row leads with what matters. */
fun List<Note>.tagCounts(): List<Pair<String, Int>> =
    filterNot { it.isArchived }
        .flatMap { it.tags }
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })

/** Coarse "3m ago" style stamp. Kept pure so it can be unit tested without a clock. */
fun relativeTime(now: Long, then: Long): String {
    val seconds = ((now - then) / 1000).coerceAtLeast(0)
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        seconds < 60 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 365 -> "${days / 7}w ago"
        else -> "${days / 365}y ago"
    }
}

/** Splits a free-text tag field ("work, ideas") into a clean tag set. */
fun parseTags(raw: String): Set<String> =
    raw.split(',', ' ', '#')
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .toSet()

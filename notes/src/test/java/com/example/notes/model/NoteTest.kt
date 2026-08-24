package com.example.notes.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTest {
    private val now = 1_700_000_000_000L
    private val notes = SampleNotes.seed(now)

    @Test
    fun `search matches titles bodies and tags`() {
        assertEquals(
            listOf("Grocery run"),
            notes.matching("sourdough", NoteFilter.All).map { it.title },
        )
        assertEquals(
            listOf("Trip ideas"),
            notes.matching("travel", NoteFilter.All).map { it.title },
        )
        assertTrue(notes.matching("standup", NoteFilter.All).single().title == "Standup notes")
    }

    @Test
    fun `archived notes are hidden from every filter but Archived`() {
        assertTrue(notes.matching("", NoteFilter.All).none { it.isArchived })
        assertTrue(notes.matching("", NoteFilter.Archived).all { it.isArchived })
        assertEquals(1, notes.matching("", NoteFilter.Archived).size)
    }

    @Test
    fun `tasks filter keeps only notes with checklists`() {
        val withTasks = notes.matching("", NoteFilter.Tasks)

        assertTrue(withTasks.isNotEmpty())
        assertTrue(withTasks.all { it.tasks.isNotEmpty() })
    }

    @Test
    fun `pinned notes sort above the most recently edited`() {
        val visible = notes.matching("", NoteFilter.All)

        assertEquals("Welcome to Notebook", visible.first().title)
        val unpinned = visible.drop(1)
        assertEquals(unpinned.sortedByDescending { it.updatedAt }, unpinned)
    }

    @Test
    fun `checklist lines are parsed and counted`() {
        val groceries = notes.single { it.title == "Grocery run" }

        assertEquals(5, groceries.tasks.size)
        assertEquals(2, groceries.doneTasks)
        assertEquals(Task("Coffee beans", isDone = true), groceries.tasks.first())
        assertFalse(groceries.tasks.last().isDone)
    }

    @Test
    fun `plain lines are not mistaken for tasks`() {
        assertEquals(null, Task.parse("- not a task"))
        assertEquals(null, Task.parse("just text"))
        assertEquals(Task("indented", isDone = false), Task.parse("  - [ ] indented"))
        assertEquals(Task("upper", isDone = true), Task.parse("- [X] upper"))
    }

    @Test
    fun `tag counts exclude archived notes and lead with the most used`() {
        val counts = notes.tagCounts().toMap()

        assertEquals(1, counts["work"])
        assertEquals(1, counts["errands"])
        // "home" is on one live note and one archived note; only the live one counts.
        assertEquals(1, counts["home"])
    }

    @Test
    fun `tag filter narrows the list`() {
        assertEquals(
            listOf("Trip ideas"),
            notes.matching("", NoteFilter.All, tag = "travel").map { it.title },
        )
        assertTrue(notes.matching("", NoteFilter.All, tag = "nope").isEmpty())
    }

    @Test
    fun `untitled notes fall back to their first line`() {
        val note = Note(id = 9, title = "  ", body = "\n\nbuy stamps\nand envelopes", createdAt = 0, updatedAt = 0)

        assertEquals("buy stamps", note.displayTitle)
        assertEquals("Untitled note", note.copy(body = "").displayTitle)
    }

    @Test
    fun `relative time buckets by magnitude`() {
        assertEquals("just now", relativeTime(now, now - 5_000))
        assertEquals("3m ago", relativeTime(now, now - 3 * 60_000))
        assertEquals("2h ago", relativeTime(now, now - 2 * 3_600_000))
        assertEquals("3d ago", relativeTime(now, now - 3 * 86_400_000L))
        assertEquals("2w ago", relativeTime(now, now - 15 * 86_400_000L))
        assertEquals("1y ago", relativeTime(now, now - 400 * 86_400_000L))
        assertEquals("just now", relativeTime(now, now + 5_000))
    }

    @Test
    fun `tag input is normalised`() {
        assertEquals(setOf("work", "ideas"), parseTags(" Work, IDEAS "))
        assertEquals(setOf("work"), parseTags("#work #work"))
        assertEquals(emptySet<String>(), parseTags("  ,  "))
    }

    @Test
    fun `word count ignores extra whitespace`() {
        val note = Note(id = 1, title = "t", body = "  one   two\n\nthree  ", createdAt = 0, updatedAt = 0)

        assertEquals(3, note.wordCount)
        assertEquals(0, note.copy(body = "   ").wordCount)
    }
}

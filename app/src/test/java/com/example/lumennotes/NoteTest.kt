package com.example.lumennotes

import com.example.lumennotes.model.Note
import com.example.lumennotes.model.noteOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTest {

    private fun note(
        id: String = "1",
        title: String = "",
        body: String = "",
        pinned: Boolean = false,
        updatedAt: Long = 0L,
    ) = Note(id, title, body, accent = 0, pinned = pinned, updatedAt = updatedAt)

    @Test
    fun `display title falls back to body then placeholder`() {
        assertEquals("Groceries", note(title = "Groceries").displayTitle)
        assertEquals("milk and eggs", note(body = "milk  and\neggs").displayTitle)
        assertEquals("Untitled note", note().displayTitle)
    }

    @Test
    fun `word count ignores extra whitespace`() {
        assertEquals(3, note(body = "  one   two\n three ").wordCount)
        assertEquals(0, note(body = "   ").wordCount)
    }

    @Test
    fun `search matches title and body case-insensitively`() {
        val n = note(title = "Trip plan", body = "Book the ferry")
        assertTrue(n.matches("trip"))
        assertTrue(n.matches("FERRY"))
        assertTrue(n.matches("  "))
        assertFalse(n.matches("train"))
    }

    @Test
    fun `ordering puts pinned first then newest`() {
        val old = note(id = "old", updatedAt = 100)
        val new = note(id = "new", updatedAt = 200)
        val pinnedOld = note(id = "pin", pinned = true, updatedAt = 50)
        val sorted = listOf(old, new, pinnedOld).sortedWith(noteOrder).map { it.id }
        assertEquals(listOf("pin", "new", "old"), sorted)
    }

    @Test
    fun `json round trip preserves fields`() {
        val original = Note("abc", "T", "B", accent = 3, pinned = true, updatedAt = 42L)
        assertEquals(original, Note.fromJson(original.toJson()))
    }
}

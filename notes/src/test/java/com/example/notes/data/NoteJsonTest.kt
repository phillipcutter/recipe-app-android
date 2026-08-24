package com.example.notes.data

import com.example.notes.model.Note
import com.example.notes.model.NoteColor
import com.example.notes.model.SampleNotes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteJsonTest {

    @Test
    fun `seed notes survive a round trip`() {
        val notes = SampleNotes.seed(1_700_000_000_000L)

        assertEquals(notes, NoteJson.decode(NoteJson.encode(notes)))
    }

    @Test
    fun `awkward text survives a round trip`() {
        val note = Note(
            id = 42,
            title = """He said "hi" \ then left""",
            body = "line one\n\tline two\r\nline three",
            color = NoteColor.Violet,
            tags = setOf("a,b", "c d"),
            isPinned = true,
            isArchived = true,
            createdAt = 1,
            updatedAt = 2,
        )

        assertEquals(listOf(note), NoteJson.decode(NoteJson.encode(listOf(note))))
    }

    @Test
    fun `empty list round trips`() {
        assertEquals("[]", NoteJson.encode(emptyList()))
        assertEquals(emptyList<Note>(), NoteJson.decode("[]"))
    }

    @Test
    fun `corrupt input decodes to nothing instead of throwing`() {
        assertTrue(NoteJson.decode("not json").isEmpty())
        assertTrue(NoteJson.decode("").isEmpty())
        assertTrue(NoteJson.decode("""[{"id":1,""").isEmpty())
    }

    @Test
    fun `unknown fields and missing optionals are tolerated`() {
        val json = """[{"id":7,"title":"t","createdAt":100,"mystery":{"a":[1,2,null]}}]"""

        val note = NoteJson.decode(json).single()

        assertEquals(7L, note.id)
        assertEquals("t", note.title)
        assertEquals("", note.body)
        assertEquals(NoteColor.Default, note.color)
        assertEquals(emptySet<String>(), note.tags)
        // updatedAt falls back to createdAt so the note still sorts sensibly.
        assertEquals(100L, note.updatedAt)
    }

    @Test
    fun `entries without an id are skipped`() {
        val json = """[{"title":"no id"},{"id":3,"title":"kept","createdAt":1}]"""

        assertEquals(listOf("kept"), NoteJson.decode(json).map { it.title })
    }
}

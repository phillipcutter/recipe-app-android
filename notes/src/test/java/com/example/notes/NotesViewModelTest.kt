package com.example.notes

import com.example.notes.data.InMemoryNoteStore
import com.example.notes.data.NoteJson
import com.example.notes.data.NoteStore
import com.example.notes.model.Note
import com.example.notes.model.NoteColor
import com.example.notes.model.NoteFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesViewModelTest {

    /** Records what was written so persistence can be asserted without Android. */
    private class RecordingStore(private var notes: List<Note>? = null) : NoteStore {
        var saves = 0
            private set

        override fun load(): List<Note>? = notes

        override fun save(notes: List<Note>) {
            this.notes = notes
            saves++
        }
    }

    private var clock = 1_000_000L

    private fun viewModel(store: NoteStore = InMemoryNoteStore()) =
        NotesViewModel(store) { clock }

    @Test
    fun `first launch seeds the notebook and persists it`() {
        val store = RecordingStore()

        val vm = viewModel(store)

        assertTrue(vm.state.notes.isNotEmpty())
        assertEquals(1, store.saves)
        assertEquals(vm.state.notes, store.load())
    }

    @Test
    fun `an existing store is not overwritten with the seed`() {
        val saved = listOf(Note(id = 5, title = "mine", body = "", createdAt = 1, updatedAt = 1))
        val store = RecordingStore(saved)

        val vm = viewModel(store)

        assertEquals(saved, vm.state.notes)
        assertEquals(0, store.saves)
    }

    @Test
    fun `creating a note gives it a fresh id and puts it in the list`() {
        val vm = viewModel()
        val before = vm.state.notes.size

        val created = vm.createNote()

        assertEquals(before + 1, vm.state.notes.size)
        assertEquals(created, vm.noteById(created.id))
        assertEquals(created.id, vm.state.notes.maxOf { it.id })
    }

    @Test
    fun `editing bumps updatedAt but an identical edit does not`() {
        val vm = viewModel()
        val note = vm.createNote()

        clock += 60_000
        vm.updateNote(note.id, title = "Groceries")
        assertEquals(clock, vm.noteById(note.id)!!.updatedAt)

        val stamp = clock
        clock += 60_000
        vm.updateNote(note.id, title = "Groceries")
        assertEquals(stamp, vm.noteById(note.id)!!.updatedAt)
    }

    @Test
    fun `toggling a task rewrites only that line`() {
        val vm = viewModel()
        val note = vm.createNote()
        vm.updateNote(note.id, body = "header\n- [ ] one\nmiddle\n- [x] two")

        vm.toggleTask(note.id, 0)

        assertEquals("header\n- [x] one\nmiddle\n- [x] two", vm.noteById(note.id)!!.body)

        vm.toggleTask(note.id, 1)

        assertEquals("header\n- [x] one\nmiddle\n- [ ] two", vm.noteById(note.id)!!.body)
    }

    @Test
    fun `archiving hides the note, clears its pin, and offers an undo`() {
        val vm = viewModel()
        val note = vm.createNote()
        vm.updateNote(note.id, title = "Receipts")
        vm.togglePinned(note.id)

        vm.setArchived(note.id, true)

        val archived = vm.noteById(note.id)!!
        assertTrue(archived.isArchived)
        assertFalse(archived.isPinned)
        assertTrue(vm.state.visibleNotes.none { it.id == note.id })
        assertEquals("Note archived", vm.state.undo?.label)

        vm.undo()

        assertTrue(vm.noteById(note.id)!!.isPinned)
        assertFalse(vm.noteById(note.id)!!.isArchived)
        assertNull(vm.state.undo)
    }

    @Test
    fun `deleting removes the note and undo brings it back intact`() {
        val vm = viewModel()
        val note = vm.createNote()
        vm.updateNote(note.id, title = "Draft", color = NoteColor.Mint)
        val before = vm.noteById(note.id)!!

        vm.deleteNote(note.id)
        assertNull(vm.noteById(note.id))

        vm.undo()
        assertEquals(before, vm.noteById(note.id))
    }

    @Test
    fun `every mutation is written through to the store`() {
        val store = RecordingStore()
        val vm = viewModel(store)
        val note = vm.createNote()
        vm.updateNote(note.id, body = "persisted")

        val reloaded = NoteJson.decode(NoteJson.encode(store.load()!!))

        assertEquals("persisted", reloaded.single { it.id == note.id }.body)
    }

    @Test
    fun `tag filter toggles off when the active tag is tapped again`() {
        val vm = viewModel()

        vm.toggleTag("work")
        assertEquals("work", vm.state.activeTag)
        assertTrue(vm.state.visibleNotes.all { it.tags.contains("work") })

        vm.toggleTag("work")
        assertNull(vm.state.activeTag)
    }

    @Test
    fun `search and filter combine`() {
        val vm = viewModel()
        vm.setFilter(NoteFilter.Tasks)
        vm.setQuery("lemons")

        assertEquals(listOf("Grocery run"), vm.state.visibleNotes.map { it.title })

        vm.setQuery("standup")
        assertTrue(vm.state.visibleNotes.isEmpty())
    }
}

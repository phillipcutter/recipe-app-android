package com.example.notes.model

/** Seeded on first launch so the app never opens to a blank wall. */
object SampleNotes {
    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    fun seed(now: Long): List<Note> = listOf(
        Note(
            id = 1,
            title = "Welcome to Notebook",
            body = """
                Tap a card to edit it. Everything saves as you type.

                Things worth trying:
                - [x] Pin a note so it sticks to the top
                - [ ] Tint a note from the palette in the editor
                - [ ] Search across titles, bodies, and tags
                - [ ] Archive a note instead of deleting it
            """.trimIndent(),
            color = NoteColor.Sky,
            tags = setOf("guide"),
            isPinned = true,
            createdAt = now - 3 * DAY,
            updatedAt = now - 20 * MINUTE,
        ),
        Note(
            id = 2,
            title = "Grocery run",
            body = """
                - [x] Coffee beans
                - [x] Oat milk
                - [ ] Lemons
                - [ ] Olive oil
                - [ ] Sourdough
            """.trimIndent(),
            color = NoteColor.Mint,
            tags = setOf("home", "errands"),
            createdAt = now - 2 * DAY,
            updatedAt = now - 4 * HOUR,
        ),
        Note(
            id = 3,
            title = "Standup notes",
            body = """
                Shipped the offline cache. Rollout is at 20% and error rate is flat.

                Blockers: still waiting on the design review for the empty state.
            """.trimIndent(),
            color = NoteColor.Amber,
            tags = setOf("work"),
            createdAt = now - 5 * DAY,
            updatedAt = now - DAY,
        ),
        Note(
            id = 4,
            title = "Trip ideas",
            body = "Lisbon in the spring. Overnight train to Porto, two nights, back on the coast road.",
            color = NoteColor.Rose,
            tags = setOf("travel", "someday"),
            createdAt = now - 12 * DAY,
            updatedAt = now - 6 * DAY,
        ),
        Note(
            id = 5,
            title = "Old passwords note",
            body = "Archived so it stays out of the way but is still searchable.",
            color = NoteColor.Violet,
            tags = setOf("home"),
            isArchived = true,
            createdAt = now - 90 * DAY,
            updatedAt = now - 40 * DAY,
        ),
    )
}

# Lumen Notes

A small, fast Android note-taking app built with Jetpack Compose and Material 3.

## Features

- **Notes with accents** — eight colors, shown as a spine on each card and a chip below it.
- **Pin** — pinned notes sort to the top; everything else is newest-first.
- **Search** — filters live over titles and bodies.
- **Delete with undo** — a snackbar restores the note.
- **Persistence** — notes are stored as JSON in the app's files directory, loaded on launch.
- Light and dark themes, edge-to-edge, no third-party runtime dependencies.

## Build

```
./gradlew :app:assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest # unit tests for note model + ordering
```

## Layout

| Path | What it holds |
| --- | --- |
| `model/Note.kt` | Note data class, display/search helpers, JSON round trip, sort order |
| `data/NoteStore.kt` | File-backed JSON persistence |
| `NotesViewModel.kt` | List state, search query, upsert / pin / delete / undo |
| `ui/NotesApp.kt` | List and editor screens |
| `ui/theme/Theme.kt` | Color schemes and the accent palette |

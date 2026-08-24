# Recipe Tracker

A compact Android sample app for tracking recipes, built with Kotlin, Jetpack Compose, Material 3, and a simple state-driven architecture.

## Features

- Browse seeded recipes in a responsive card list
- Search by recipe name, ingredient, or tag
- Filter by all, favorites, quick meals, or vegetarian recipes
- Add recipes with a lightweight form
- Favorite recipes and adjust serving counts
- View recipe details, ingredients, steps, prep time, and difficulty
- Empty states, summary stats, dark-mode support, and edge-to-edge UI
- Unit tests for search/filter behavior and ingredient scaling

## Run

1. Open the repository in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or Android device (API 26+).

From the command line:

```bash
./gradlew test
./gradlew assembleDebug
```

## Project layout

- `app/src/main/java/.../model` — recipe data model and sample data
- `app/src/main/java/.../ui` — Compose screens, components, and theme
- `app/src/main/java/.../RecipeViewModel.kt` — state and user actions
- `app/src/test` — JVM unit tests


---

# Notebook (`:notes`)

A second app in this repo: a note-taking app built with the same stack — Kotlin, Jetpack
Compose, Material 3, and a state-driven ViewModel. It is a separate Gradle module and a
separate installable APK (`com.example.notes`); the recipe app is untouched.

## Features

- Adaptive card grid of notes, pinned first, then most recently edited
- Live search across titles, bodies, and tags
- Filters for all / pinned / checklists / archived, plus tappable tag chips with counts
- Distraction-free editor with no save button — every keystroke persists
- Markdown-style checklists: any `- [ ] thing` line becomes a tickable task, with
  progress shown on the card and a checkbox view in the editor
- Six-colour palette per note, pin, archive, and delete with an undo snackbar
- Persistence with zero extra dependencies: notes are stored as JSON in SharedPreferences
  via a hand-rolled codec (`NoteJson`) that is unit tested directly
- Seeded on first launch so the app never opens blank; light and dark themes; edge-to-edge

## Run

```bash
./gradlew :notes:test           # 28 unit tests
./gradlew :notes:assembleDebug  # notes/build/outputs/apk/debug/notes-debug.apk
```

## Project layout

- `notes/src/main/java/com/example/notes/model` — `Note`, checklist parsing, search/filter/sort
- `notes/src/main/java/com/example/notes/data` — JSON codec and the SharedPreferences store
- `notes/src/main/java/com/example/notes/NotesViewModel.kt` — state and user actions
- `notes/src/main/java/com/example/notes/ui` — list screen, editor, theme
- `notes/src/test` — JVM unit tests for the model, codec, and ViewModel

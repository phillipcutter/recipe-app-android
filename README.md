# Recipe Tracker

A compact Android sample app for tracking recipes, built with Kotlin, Jetpack Compose, Material 3, and a simple state-driven architecture.

## Features

- Browse seeded recipes in a responsive card list
- Search by recipe name, ingredient, or tag
- Filter by all, favorites, quick meals, or vegetarian recipes
- Add recipes with a lightweight form
- Favorite recipes and adjust serving counts
- View recipe details, ingredients, steps, prep time, and difficulty
- Keep a grocery list of what you still need from the store
- Send a recipe's ingredients to the list, scaled to the servings you picked
- Quick-add items in plain text — "2 cups flour" is split into amount, unit, and name
- Check items off, clear the cart, and see how much is left to buy
- Empty states, summary stats, dark-mode support, and edge-to-edge UI
- Unit tests for search/filter behavior, ingredient scaling, and grocery list logic

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

- `app/src/main/java/.../model` — recipe and grocery data models, sample data
- `app/src/main/java/.../ui` — Compose screens, components, and theme
- `app/src/main/java/.../RecipeViewModel.kt` — state and user actions
- `app/src/test` — JVM unit tests

## Screenshots

UI renders of the grocery list feature live in [`docs/screenshots`](docs/screenshots).

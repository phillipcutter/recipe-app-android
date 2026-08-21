# UI renders

These SVGs render the Compose screens added in this branch, using the app's own
Material 3 palette from `ui/theme/Theme.kt` and the copy and layout from
`ui/RecipeApp.kt` and `ui/GroceryScreen.kt`.

**They are renders, not emulator captures.** They were produced in an
environment with no Android SDK and no KVM, whose egress policy blocks
`dl.google.com`, `maven.google.com`, and `repo1.maven.org` — the project could
not be assembled or run there. Font metrics, ripples, and elevation shadows
will differ from a device; the structure, copy, and colors come from the source.

Replace them with real captures (`adb exec-out screencap -p > shot.png`) the
next time the app runs on a device or emulator.

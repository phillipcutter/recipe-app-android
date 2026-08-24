package com.example.lumennotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF4C5BD4)
private val IndigoLight = Color(0xFFB9C0FF)

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E3FF),
    onPrimaryContainer = Color(0xFF11175E),
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF16161D),
    surface = Color.White,
    onSurface = Color(0xFF16161D),
    surfaceVariant = Color(0xFFEDEDF4),
    onSurfaceVariant = Color(0xFF5A5A6B),
)

private val DarkColors = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Color(0xFF1B2270),
    primaryContainer = Color(0xFF333C9E),
    onPrimaryContainer = Color(0xFFE0E3FF),
    background = Color(0xFF111117),
    onBackground = Color(0xFFE6E6EE),
    surface = Color(0xFF1A1A22),
    onSurface = Color(0xFFE6E6EE),
    surfaceVariant = Color(0xFF2A2A35),
    onSurfaceVariant = Color(0xFFB9B9C7),
)

/** The eight accent colors a note can wear, tuned for both themes. */
val accentPalette: List<Color> = listOf(
    Color(0xFF4C5BD4), // indigo
    Color(0xFF1E9E8A), // teal
    Color(0xFFE0663C), // ember
    Color(0xFF8E54C4), // violet
    Color(0xFFD4A017), // amber
    Color(0xFF3B82C4), // sky
    Color(0xFFC44569), // rose
    Color(0xFF5E9E3B), // moss
)

val accentNames: List<String> =
    listOf("Indigo", "Teal", "Ember", "Violet", "Amber", "Sky", "Rose", "Moss")

fun accentColor(index: Int): Color = accentPalette[index.mod(accentPalette.size)]

fun accentName(index: Int): String = accentNames[index.mod(accentNames.size)]

@Composable
fun LumenNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}

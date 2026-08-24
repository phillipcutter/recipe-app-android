package com.example.notes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.example.notes.model.NoteColor

private val LightColors = lightColorScheme(
    primary = Color(0xFF3D5AFE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE1FF),
    onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFF5B5D72),
    secondaryContainer = Color(0xFFE0E1F9),
    background = Color(0xFFFCFCFF),
    surface = Color(0xFFFCFCFF),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF46464F),
    outline = Color(0xFF777680),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9C3FF),
    onPrimary = Color(0xFF001A6E),
    primaryContainer = Color(0xFF2B3F9B),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFC4C5DD),
    secondaryContainer = Color(0xFF434659),
    background = Color(0xFF121316),
    surface = Color(0xFF121316),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC7C5D0),
    outline = Color(0xFF918F9A),
)

/**
 * The app keeps a fixed palette rather than dynamic color: the note tints below are chosen to
 * sit against these exact surfaces, and a wallpaper-derived scheme would fight them.
 */
@Composable
fun NotebookTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}

private val LightTints = mapOf(
    NoteColor.Default to Color(0xFFF2F3FA),
    NoteColor.Amber to Color(0xFFFFECC7),
    NoteColor.Rose to Color(0xFFFFDDE2),
    NoteColor.Mint to Color(0xFFC9F2DE),
    NoteColor.Sky to Color(0xFFD3E8FF),
    NoteColor.Violet to Color(0xFFE6DBFF),
)

private val DarkTints = mapOf(
    NoteColor.Default to Color(0xFF23242B),
    NoteColor.Amber to Color(0xFF4A3A17),
    NoteColor.Rose to Color(0xFF4E2530),
    NoteColor.Mint to Color(0xFF1D4034),
    NoteColor.Sky to Color(0xFF1E3A54),
    NoteColor.Violet to Color(0xFF352A54),
)

/** Card background for a note tint, resolved against the current light/dark scheme. */
@Composable
@ReadOnlyComposable
fun NoteColor.tint(): Color {
    val dark = MaterialTheme.colorScheme.background.luminanceIsDark()
    return (if (dark) DarkTints else LightTints).getValue(this)
}

private fun Color.luminanceIsDark(): Boolean = (red + green + blue) / 3f < 0.5f

package com.example.recipetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF9A4522),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCB),
    secondary = Color(0xFF5C5F33),
    secondaryContainer = Color(0xFFE2E5A9),
    background = Color(0xFFFFF8F5),
    surface = Color(0xFFFFF8F5),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB693),
    primaryContainer = Color(0xFF7B2E0C),
    secondary = Color(0xFFC6C98F),
    secondaryContainer = Color(0xFF444817),
)

@Composable
fun RecipeTrackerTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dark -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}

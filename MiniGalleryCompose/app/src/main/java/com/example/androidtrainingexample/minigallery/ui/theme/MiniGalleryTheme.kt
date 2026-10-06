package com.example.androidtrainingexample.minigallery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF5364BE), secondary = Color(0xFF5B5D72), tertiary = Color(0xFF77536E)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFBAC3FF), secondary = Color(0xFFC4C4DC), tertiary = Color(0xFFE7B8D7)
)

@Composable
fun MiniGalleryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
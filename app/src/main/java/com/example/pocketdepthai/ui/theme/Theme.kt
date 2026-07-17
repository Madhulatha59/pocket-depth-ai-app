package com.example.pocketdepthai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5D48D1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EBFF),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF7B61FF),
    onSecondary = Color.White,
    tertiary = Color(0xFF4CAF50),
    background = Color(0xFFF5F7FF),
    surface = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    error = Color(0xFFD32F2F)
)

@Composable
fun PocketDepthAITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

package com.talotv.os.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TaloAccent,
    secondary = TaloCardGlass,
    background = TaloDarkBackground,
    surface = TaloOledBlack,
    onPrimary = Color.Black,
    onSecondary = TaloTextPrimary,
    onBackground = TaloTextPrimary,
    onSurface = TaloTextPrimary
)

@Composable
fun TaloOSTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

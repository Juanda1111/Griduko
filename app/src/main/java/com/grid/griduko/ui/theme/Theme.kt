package com.grid.griduko.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GridukoColorScheme = darkColorScheme(
    primary = AccentYellow,
    onPrimary = TextDarkNavy,
    secondary = SnakeGreen,
    onSecondary = TextDarkNavy,
    tertiary = LightBlue,
    background = NavyBackground,
    onBackground = TextWhite,
    surface = NavySurface,
    onSurface = TextWhite,
    outline = NavyBorder
)

@Composable
fun GridukoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GridukoColorScheme,
        typography = Typography,
        content = content
    )
}

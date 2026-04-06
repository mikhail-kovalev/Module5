package com.example.todolist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TodoColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryBlue,
    tertiary = PrimaryBlue,
    background = SurfaceBackground,
    surface = SurfaceBackground
)

@Composable
fun TodoListTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TodoColorScheme,
        typography = Typography,
        content = content
    )
}

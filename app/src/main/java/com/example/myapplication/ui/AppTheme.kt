package com.example.myapplication.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// Light ve Dark renk şemaları
private val LightColors = lightColorScheme(
    primary = Color(0xFF64B5F6), // açık mavi
    onPrimary = Color.White,
    background = Color(0xFFF3F6FA),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0B1622),
    onPrimary = Color.White,
    background = Color(0xFF0B1622),
    surface = Color(0xFF424242),
    onSurface = Color(0xFF0B1622),
)

// Dark mod degrade arka plan
private val DarkGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0A2742),
        Color(0xFF123E63)
    ),
    start = Offset.Zero,
    end = Offset(0f, Float.POSITIVE_INFINITY)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(colorScheme = colors) {
        ThemedSystemBars(
            darkTheme = darkTheme
        )
        content()
    }
}

@Composable
private fun ThemedSystemBars(darkTheme: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window

        // Edge-to-edge önerilir
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Renkler (senin istediğin gibi)
        val statusBarColor = if (darkTheme) Color.Black else Color(0xFF64B5F6)
        val navigationBarColor = if (darkTheme) Color.Black else Color(0xFF64B5F6)
        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = navigationBarColor.toArgb()

        val controller = WindowCompat.getInsetsController(window, view)

        // Status bar ikonları beyaz kalsın
        controller.isAppearanceLightStatusBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            controller.isAppearanceLightNavigationBars = false
        }

        // 🔻 Alt navigation bar'ı gizle, swipe ile geçici göster
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        // Eğer status bar'ı da gizlemek istersen:
        // controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val lightBg = LightColors.background
    Box(
        modifier = modifier
            .fillMaxSize()
            .let {
                if (dark) it.background(DarkGradient)
                else it.background(Brush.linearGradient(listOf(lightBg, lightBg)))
            }
    ) { content() }
}

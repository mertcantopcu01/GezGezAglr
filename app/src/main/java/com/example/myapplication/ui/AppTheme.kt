
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

// Light ve Dark renk şemaları
private val LightColors = lightColorScheme(
    primary = Color(0xFF2D6AA6),
    onPrimary = Color.White,
    background = Color(0xFFF3F6FA), // açık arka plan
    surface = Color.White,          // kart beyaz
    onSurface = Color(0xFF1A1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF2D6AA6),
    onPrimary = Color.White,
    background = Color(0xFF0B1622),   // arka plan (degrade ile kaplanacak)
    surface = Color(0xFFE0E0E0),      // 🔹 açık gri kart (pembe değil, siyah değil)
    onSurface = Color(0xFF1A1A1A)     // koyu yazı
)

// Dark mod degrade arka plan
private val DarkGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0A2742), // lacivert
        Color(0xFF123E63)  // mavi
    ),
    start = Offset.Zero,
    end = Offset(0f, Float.POSITIVE_INFINITY)
)

/** Uygulama genel teması (tek yerden) */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(colorScheme = colors) {
        // Sistem çubukları (status/nav bar)
        ThemedSystemBars(
            statusBarColor = if (darkTheme) Color(0xFF0A2742) else colors.background,
            navigationBarColor = if (darkTheme) Color(0xFF0A2742) else colors.background,
            darkTheme = darkTheme
        )
        content()
    }
}

/** Sistem çubuklarını günceller, ikon kontrastını moda göre ayarlar */
@Composable
private fun ThemedSystemBars(
    statusBarColor: Color,
    navigationBarColor: Color,
    darkTheme: Boolean
) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = navigationBarColor.toArgb()

        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

/** Ekran kökünde kullan: Light’ta düz, Dark’ta degrade arka plan verir */
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

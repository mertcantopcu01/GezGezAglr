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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Material ana renkler ---
private val LightColors = lightColorScheme(

    primary = Color(0xFF2F3CFD), // mavi kalsın ama topBar için kullanmıyoruz
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF0F172A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4F46E5), // mor-mavi kalsın ama topBar için kullanmıyoruz
    onPrimary = Color.White,
    background = Color(0xFF0B1622),
    surface = Color(0xFF0B1622),
    onSurface = Color.White
)

// --- Uygulamaya özel renkler: sadece TopBar için ---
@Stable
data class ExtraColors(
    val topBar: Color,
    val onTopBar: Color
)

private val LightExtra = ExtraColors(
    topBar = Color.White,         // Light mode: üst bar beyaz
    onTopBar = Color.Black        // Light mode: yazılar/ikonlar siyah
)
private val DarkExtra = ExtraColors(
    topBar = Color(0xFF0B1622),   // Dark mode: koyu
    onTopBar = Color.White        // Dark mode: yazılar/ikonlar beyaz
)

private val LocalExtraColors = staticCompositionLocalOf { LightExtra }

object AppThemeColors {
    val extra: ExtraColors
        @Composable get() = LocalExtraColors.current
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val material = if (darkTheme) DarkColors else LightColors
    val extra = if (darkTheme) DarkExtra else LightExtra

    CompositionLocalProvider(LocalExtraColors provides extra) {
        MaterialTheme(colorScheme = material) {
            ThemedSystemBars(extra)
            content()
        }
    }
}

@Composable
private fun ThemedSystemBars(extra: ExtraColors) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window

        // Sistem pencereleri içerik kenar boşluklarını uygulasın
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Bar renklerini topBar ile eşitle
        val barColor = extra.topBar
        window.statusBarColor = barColor.toArgb()
        window.navigationBarColor = barColor.toArgb()

        // İkon rengi: açık zeminde siyah, koyu zeminde beyaz
        val controller = WindowCompat.getInsetsController(window, view)
        val useDarkIcons = barColor.luminance() > 0.5f  // açık renk -> siyah ikon
        controller.isAppearanceLightStatusBars = useDarkIcons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            controller.isAppearanceLightNavigationBars = useDarkIcons
        }

        // Barları görünür bırak (önceki gizleme davranışı varsa iptal)
        controller.show(androidx.core.view.WindowInsetsCompat.Type.statusBars())
        controller.show(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
    }
}

private val DarkGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0B1622), Color(0xFF0B1622))
)

@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val bg = if (dark) DarkGradient else Brush.verticalGradient(listOf(Color.White, Color.White))
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) { content() }
}

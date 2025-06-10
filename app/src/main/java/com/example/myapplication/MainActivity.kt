package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.navigation.AppNavGraph
import com.example.myapplication.navigation.Routes
import com.example.myapplication.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            AppTheme {
                val navController = rememberNavController()
                val startDestination = if (AuthService.getCurrentUser() != null) {
                    Routes.HOME
                } else {
                    Routes.LOGIN
                }
                AppNavGraph(navController = navController, startDestination = startDestination)
            }
        }
    }
}

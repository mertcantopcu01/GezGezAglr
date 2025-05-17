package com.example.myapplication


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.AppNavGraph
import com.example.myapplication.navigation.Routes
import com.example.myapplication.firebase.AuthService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            val startDestination = if (AuthService.getCurrentUser() != null) {
                Routes.HOME
            } else {
                Routes.LOGIN
            }

            Surface(color = MaterialTheme.colorScheme.background) {
                AppNavGraph(navController = navController)
            }
        }
    }
}

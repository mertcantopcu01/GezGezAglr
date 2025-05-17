package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.firebase.AuthService

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val user = AuthService.getCurrentUser()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hoş geldin!", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Email: ${user?.email ?: "Bilinmiyor"}")

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            AuthService.signOut()
            onLogout()
        }) {
            Text("Çıkış Yap")
        }
    }
}

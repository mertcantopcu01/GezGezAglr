package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.welcome),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        IconButton(
            onClick = onNavigateToProfile,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.go_profile),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(
            onClick = {
                AuthService.signOut()
                onLogout()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = stringResource(R.string.login_out),
                tint = MaterialTheme.colorScheme.error
            )
        }

        IconButton(
            onClick = onNavigateToSearch,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Kullanıcı Ara",
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

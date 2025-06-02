package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersScreen(
    userId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    var list by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    LaunchedEffect(userId) {
        FirestoreService.getFollowersList(userId) { list = it }
    }

    // Degradeli arka plan renkleri (res/values/colors.xml içinde tanımlı olmalı)
    val gradientColors = listOf(
        colorResource(id = R.color.blue_900),
        colorResource(id = R.color.green_800)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takipçiler", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                modifier = Modifier.background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
            )
        },
        // Scaffold'un kendi content parametresi içine Box + LazyColumn alıyoruz
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(list) { u ->
                    ListItem(
                        modifier = Modifier
                            .clickable { onUserClick(u.uid) }
                            .padding(vertical = 4.dp),
                        headlineContent = {
                            Text(
                                text = u.username,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingContent = {
                            Image(
                                painter = rememberAsyncImagePainter(u.profileImageUrl ?: ""),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                    Divider(color = Color.Transparent.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewFollowersScreen() {
    // Örnek veri
    val sampleUsers = listOf(
        UserProfile(uid = "u1", username = "Ali", profileImageUrl = null, bio = null),
        UserProfile(uid = "u2", username = "Ayşe", profileImageUrl = null, bio = null)
    )

    // “preview” için list değerini sabitleyelim:
    var list by remember { mutableStateOf(sampleUsers) }

    // Degradeli arka plan renkleri
    val gradientColors = listOf(
        colorResource(id = R.color.blue_900),
        colorResource(id = R.color.green_800)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takipçiler", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { /* Geri */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                modifier = Modifier.background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(list) { u ->
                    ListItem(
                        modifier = Modifier
                            .clickable { /* Kullanıcıya git */ }
                            .padding(vertical = 4.dp),
                        headlineContent = {
                            Text(
                                text = u.username,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingContent = {
                            Image(
                                painter = rememberAsyncImagePainter(u.profileImageUrl ?: ""),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                    Divider(color = Color.Transparent.copy(alpha = 0.5f))
                }
            }
        }
    }
}

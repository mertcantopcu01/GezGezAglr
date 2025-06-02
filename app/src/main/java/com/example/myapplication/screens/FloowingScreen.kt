package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
fun FollowingScreen(
    userId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    var list by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    LaunchedEffect(userId) {
        FirestoreService.getFollowingList(userId) { list = it }
    }

    // Degradeli arka plan renkleri
    val gradientColors = listOf(
        colorResource(id = R.color.blue_900),
        colorResource(id = R.color.green_800)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takip Edilenler", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
        }
    ) { paddingValues ->
        LazyColumn(
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
                Divider(color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewFollowingScreen() {
    // Örnek veri
    val sampleUsers = listOf(
        UserProfile(uid = "u1", username = "Mehmet", profileImageUrl = "https://picsum.photos/40", bio = null),
        UserProfile(uid = "u2", username = "Zeynep", profileImageUrl = null, bio = null)
    )
    var list by remember { mutableStateOf(sampleUsers) }

    val gradientColors = listOf(
        colorResource(id = R.color.blue_900),
        colorResource(id = R.color.green_800)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takip Edilenler", color = Color.White) },
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
        }
    ) { paddingValues ->
        LazyColumn(
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
                Divider(color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

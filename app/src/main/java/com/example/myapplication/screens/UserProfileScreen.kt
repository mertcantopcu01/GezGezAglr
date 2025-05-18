package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile

@Composable
fun UserProfileScreen(userId: String) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }

    LaunchedEffect(userId) {
        FirestoreService.getUserProfile(userId) { profile = it }
        FirestoreService.getUserPosts(userId) { posts = it }
    }

    profile?.let { user ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(user.profileImageUrl ?: ""),
                        contentDescription = null,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(user.username, style = MaterialTheme.typography.titleLarge)
                    user.bio?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                }
            }

            items(posts) { post ->
                PostCard(post)
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

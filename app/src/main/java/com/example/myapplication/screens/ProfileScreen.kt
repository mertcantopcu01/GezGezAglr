package com.example.myapplication.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile

@Composable
fun ProfileScreen(
    onCreatePost: () -> Unit,
    onPostClick: (String) -> Unit
) {
    val uid = AuthService.getCurrentUser()?.uid
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val context = LocalContext.current
    val TAG = "ProfileScreen"

    // Yüklemeler
    LaunchedEffect(uid) {
        uid?.let {
            FirestoreService.getUserProfile(it) { user -> profile = user }
            FirestoreService.getUserPosts(it) { userPosts -> posts = userPosts }
        }
    }

    profile?.let { user ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profil üst kısmı
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(user.profileImageUrl ?: ""),
                        contentDescription = context.getString(R.string.profil_photo),
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = user.username, style = MaterialTheme.typography.titleLarge)
                    user.bio?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Yeni post oluştur butonu
                    Button(
                        onClick = {
                            Log.d(TAG, "Yeni Post At butonuna basıldı")
                            onCreatePost()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Bir Yer Öner")
                    }
                }
            }

            // Post başlık + görsel listesi
            if (posts.isNotEmpty()) {
                items(posts) { post ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPostClick(post.postId) },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = post.title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            post.photoUrl?.let { url ->
                                Image(
                                    painter = rememberAsyncImagePainter(url),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = context.getString(R.string.there_is_no_tweet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun PostCard(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(post.title, style = MaterialTheme.typography.bodyLarge)
            post.location?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text("📍 $it", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
            }
            post.photoUrl?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Post Fotoğrafı",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }
        }
    }
}


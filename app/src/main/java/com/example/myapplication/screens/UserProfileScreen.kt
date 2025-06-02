package com.example.myapplication.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun UserProfileScreen(
    userId: String,
    onCreatePost: () -> Unit,
    onPostClick: (String) -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val currentUserId = AuthService.getCurrentUser()?.uid
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isFollowing by remember { mutableStateOf(false) }
    var followersCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var isLoadingFollow by remember { mutableStateOf(false) }

    var postToDelete by remember { mutableStateOf<Post?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Degradeli arka plan renkleri
    val gradientColors = listOf(
        colorResource(id = R.color.blue_900),
        colorResource(id = R.color.green_800)
    )

    LaunchedEffect(userId) {
        FirestoreService.getUserProfile(userId)    { profile = it }
        FirestoreService.getUserPosts(userId)      { posts = it }
        FirestoreService.getFollowersCount(userId) { followersCount = it }
        FirestoreService.getFollowingCount(userId) { followingCount = it }
        if (currentUserId != null && currentUserId != userId) {
            FirestoreService.isFollowing(currentUserId, userId) { isFollowing = it }
        }
    }

    // Silme onayı varsa AlertDialog göster
    if (showDeleteDialog && postToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Gönderiyi Sil") },
            text = { Text("Bu gönderiyi silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(onClick = {
                    FirestoreService.deletePost(postToDelete!!.postId) { success ->
                        if (success) posts = posts.filterNot { it.postId == postToDelete!!.postId }
                        postToDelete = null
                        showDeleteDialog = false
                    }
                }) { Text("Evet") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Hayır") }
            }
        )
    }

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
    ) {
        profile?.let { user ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
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
                        Spacer(Modifier.height(8.dp))
                        Text(user.username, style = MaterialTheme.typography.titleLarge, color = Color.White)
                        user.bio?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { onFollowersClick(userId) }
                                    .padding(8.dp)
                            ) {
                                Text("$followersCount", style = MaterialTheme.typography.titleMedium, color = Color.White)
                                Text("Takipçi", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { onFollowingClick(userId) }
                                    .padding(8.dp)
                            ) {
                                Text("$followingCount", style = MaterialTheme.typography.titleMedium, color = Color.White)
                                Text("Takip Edilen", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        if (currentUserId != null && currentUserId == userId) {
                            Button(
                                onClick = onCreatePost,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) { Text("Gönderi Paylaş") }
                            Spacer(Modifier.height(16.dp))
                        } else if (currentUserId != null) {
                            Button(
                                onClick = {
                                    isLoadingFollow = true
                                    if (isFollowing) {
                                        FirestoreService.unfollowUser(currentUserId, userId) { success, _ ->
                                            if (success) {
                                                isFollowing = false
                                                followersCount--
                                            }
                                            isLoadingFollow = false
                                        }
                                    } else {
                                        FirestoreService.followUser(currentUserId, userId) { success, _ ->
                                            if (success) {
                                                isFollowing = true
                                                followersCount++
                                            }
                                            isLoadingFollow = false
                                        }
                                    }
                                },
                                enabled = !isLoadingFollow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    when {
                                        isLoadingFollow -> "Lütfen bekleyin..."
                                        isFollowing      -> "Takipten Çık"
                                        else             -> "Takip Et"
                                    }
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        Divider(color = Color.White.copy(alpha = 0.6f))
                    }
                }

                items(posts) { post ->
                    val isMine = currentUserId == userId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onPostClick(post.postId) },
                                onLongClick = {
                                    if (isMine) {
                                        postToDelete = post
                                        showDeleteDialog = true
                                    }
                                }
                            ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(post.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
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
                            if (isMine) {
                                IconButton(
                                    onClick = {
                                        postToDelete = post
                                        showDeleteDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Sil",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        }

        if(currentUserId == userId){
            IconButton(
                onClick = {
                    AuthService.signOut()
                    onLogout()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Çıkış Yap",
                    tint = Color.White
                )
            }
        }
    }
}

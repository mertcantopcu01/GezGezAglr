package com.example.myapplication.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.AppBackground

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onCreatePost: () -> Unit,
    onPostClick: (String) -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,          // ✅ YENİ
    onBack: (() -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val currentUserId = AuthService.getCurrentUser()?.uid

    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isFollowing by remember { mutableStateOf(false) }
    var followersCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var isLoadingFollow by remember { mutableStateOf(false) }

    var postToDelete by remember { mutableStateOf<Post?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        FirestoreService.getUserProfile(userId)    { profile = it }
        FirestoreService.getUserPosts(userId)      { posts = it }
        FirestoreService.getFollowersCount(userId) { followersCount = it }
        FirestoreService.getFollowingCount(userId) { followingCount = it }
        if (currentUserId != null && currentUserId != userId) {
            FirestoreService.isFollowing(currentUserId, userId) { isFollowing = it }
        }
    }

    if (showDeleteDialog && postToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Gönderiyi Sil", fontFamily = FontFamily.Monospace) },
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
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Hayır") } }
        )
    }

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            profile?.username ?: "Profil",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        onBack?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentUserId == userId) {
                            IconButton(onClick = onEditProfile) {
                                Icon(Icons.Filled.Edit, contentDescription = "Profili Düzenle", tint = Color.White)
                            }
                            IconButton(onClick = {
                                AuthService.signOut()
                                onLogout()
                            }) {
                                Icon(Icons.Filled.ExitToApp, contentDescription = "Çıkış", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (profile == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = cs.primary
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Header kart
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cs.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(ctx)
                                            .data(profile!!.profileImageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            Box(
                                                Modifier
                                                    .size(96.dp)
                                                    .clip(CircleShape)
                                                    .background(cs.secondary.copy(0.2f))
                                            )
                                        },
                                        error = {
                                            Box(
                                                Modifier
                                                    .size(96.dp)
                                                    .clip(CircleShape)
                                                    .background(cs.secondary.copy(0.2f))
                                            )
                                        },
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                    )

                                    Spacer(Modifier.height(10.dp))

                                    Text(
                                        profile!!.username,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = FontFamily.Monospace,
                                        color = cs.onSurface
                                    )

                                    profile!!.bio?.takeIf { it.isNotBlank() }?.let {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = cs.onSurface.copy(alpha = 0.8f)
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .clickable(onClick = { onFollowersClick(userId) })
                                        ) {
                                            Text("$followersCount",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontFamily = FontFamily.Monospace)
                                            Text("Takipçi", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .clickable(onClick = { onFollowingClick(userId) })
                                        ) {
                                            Text("$followingCount",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontFamily = FontFamily.Monospace)
                                            Text("Takip Edilen", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // ✅ Kendi profili: iki buton
                                    if (currentUserId == userId) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = onCreatePost,
                                                modifier = Modifier.weight(1f),
                                                shape = MaterialTheme.shapes.medium
                                            ) { Text("Gönderi Paylaş", fontFamily = FontFamily.Monospace) }

                                            OutlinedButton(
                                                onClick = onEditProfile,
                                                modifier = Modifier.weight(1f),
                                                shape = MaterialTheme.shapes.medium
                                            ) {
                                                Icon(Icons.Filled.Edit, contentDescription = null)
                                                Spacer(Modifier.width(6.dp))
                                                Text("Profili Düzenle", fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    } else if (currentUserId != null) {
                                        // Başkasının profili → Takip / Takipten çık
                                        Button(
                                            onClick = {
                                                if (isFollowing) {
                                                    FirestoreService.unfollowUser(currentUserId, userId) { ok, _ ->
                                                        if (ok) { isFollowing = false; followersCount-- }
                                                    }
                                                } else {
                                                    FirestoreService.followUser(currentUserId, userId) { ok, _ ->
                                                        if (ok) { isFollowing = true; followersCount++ }
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = MaterialTheme.shapes.medium
                                        ) { Text(if (isFollowing) "Takipten Çık" else "Takip Et") }
                                    }
                                }
                            }
                        }

                        // Gönderiler
                        items(posts, key = { it.postId }) { post ->
                            val isMine = currentUserId == userId
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cs.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = MaterialTheme.shapes.large,
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
                                    )
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            post.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = cs.onSurface,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isMine) {
                                            IconButton(onClick = {
                                                postToDelete = post
                                                showDeleteDialog = true
                                            }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Sil", tint = cs.error)
                                            }
                                        }
                                    }

                                    post.photoUrl?.let { url ->
                                        Spacer(Modifier.height(10.dp))
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(ctx)
                                                .data(url)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            loading = {
                                                Box(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(180.dp)
                                                        .background(cs.secondary.copy(0.1f))
                                                )
                                            },
                                            error = {
                                                Box(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(180.dp)
                                                        .background(cs.secondary.copy(0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) { Text("Görsel yüklenemedi", fontFamily = FontFamily.Monospace) }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(MaterialTheme.shapes.medium)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

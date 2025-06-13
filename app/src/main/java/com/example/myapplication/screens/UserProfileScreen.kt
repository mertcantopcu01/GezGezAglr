package com.example.myapplication.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalWearMaterialApi::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onCreatePost: () -> Unit,
    onPostClick: (String) -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onLogout: () -> Unit,
) {
    AppTheme {
        val currentUserId = AuthService.getCurrentUser()?.uid
        var profile by remember { mutableStateOf<UserProfile?>(null) }
        var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var isFollowing by remember { mutableStateOf(false) }
        var followersCount by remember { mutableStateOf(0) }
        var followingCount by remember { mutableStateOf(0) }
        var isLoadingFollow by remember { mutableStateOf(false) }
        val context = LocalContext.current

        var postToDelete by remember { mutableStateOf<Post?>(null) }
        var showDeleteDialog by remember { mutableStateOf(false) }

        val PROTECTED_UID = "KfiILMiSHsU2q7ush2CAVT1ryC33"


        // Load user data
        LaunchedEffect(userId) {
            FirestoreService.getUserProfile(userId) { profile = it }
            FirestoreService.getUserPosts(userId) { posts = it }
            FirestoreService.getFollowersCount(userId) { followersCount = it }
            FirestoreService.getFollowingCount(userId) { followingCount = it }
            if (currentUserId != null && currentUserId != userId) {
                FirestoreService.isFollowing(currentUserId, userId) { isFollowing = it }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog && postToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Gönderiyi Sil", style = MaterialTheme.typography.titleMedium) },
                text = { Text("Bu gönderiyi silmek istediğinize emin misiniz?", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(onClick = {
                        FirestoreService.deletePost(postToDelete!!.postId) { success ->
                            if (success) {
                                posts = posts.filterNot { it.postId == postToDelete!!.postId }
                            }
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

        // Handle system back
        BackHandler { onBack() }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                        }
                    },
                    title = {
                        Text(
                            text = profile?.username.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        if (currentUserId == userId) {
                            IconButton(onClick = {
                                AuthService.signOut()
                                onLogout()
                            }) {
                                Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            profile?.let { user ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile header
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(vertical = 16.dp)
                        ) {
                            val painter = rememberAsyncImagePainter(user.profileImageUrl ?: "")
                            Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = user.username,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            user.bio?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
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
                                    Text(
                                        "$followersCount",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Takipçi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { onFollowingClick(userId) }
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        "$followingCount",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Takip Edilen",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            if (currentUserId == userId) {
                                Button(
                                    onClick = onCreatePost,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 32.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                ) {
                                    Text("Gönderi Paylaş")
                                }
                            } else {
                                val isProtected = userId == PROTECTED_UID
                                Button(
                                    onClick = {
                                        // Korunmuş kullanıcı için unfollow engelleniyor
                                        if (isProtected && isFollowing) {
                                            Toast
                                                .makeText(context, "Bu kullanıcı takipten çıkarılamaz", Toast.LENGTH_SHORT)
                                                .show()
                                            return@Button
                                        }
                                        isLoadingFollow = true
                                        if (isFollowing) {
                                            FirestoreService.unfollowUser(currentUserId!!, userId) { success, _ ->
                                                if (success) {
                                                    isFollowing = false
                                                    followersCount--
                                                }
                                                isLoadingFollow = false
                                            }
                                        } else {
                                            FirestoreService.followUser(currentUserId!!, userId) { success, _ ->
                                                if (success) {
                                                    isFollowing = true
                                                    followersCount++
                                                }
                                                isLoadingFollow = false
                                            }
                                        }
                                    },
                                    enabled = !isLoadingFollow && !(isProtected && isFollowing),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 32.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                ) {
                                    Text(
                                        text = when {
                                            isLoadingFollow -> "Lütfen Bekleyin..."
                                            isProtected && isFollowing -> "Takip Ediliyor"
                                            isFollowing -> "Takipten Çık"
                                            else -> "Takip Et"
                                        }
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                        }
                    }

                    // Posts with swipe-to-delete for own profile
                    items(posts, key = { it.postId }) { post ->
                        if (currentUserId == userId) {
                            // 2) Swipe state ve demo flag
                            val maxDragPx = with(LocalDensity.current) { -50.dp.toPx() }
                            val swipeState = rememberSwipeableState(0)
                            val anchors = mapOf(0f to 0, maxDragPx to 1)

                            // Demo için flag
                            val hasDemonstrated = remember { mutableStateOf(false) }

                            Log.e("MERT TEST ", "UserProfileScreen: " + !hasDemonstrated.value )

                            // 3) İlk öğe ve henüz demo yapılmadıysa animasyonu tetikle
                            if (!hasDemonstrated.value && posts.firstOrNull()?.postId == post.postId) {
                                LaunchedEffect(swipeState) {
                                    delay(1000)
                                    swipeState.animateTo(
                                        targetValue = 1
                                    )
                                    // Bir süre açık kalsın
                                    delay(1000)
                                    // Geri orijinale dön
                                    swipeState.animateTo(
                                        targetValue = 0
                                    )
                                    hasDemonstrated.value = true
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .clip(MaterialTheme.shapes.medium)
                                    .swipeable(
                                        state = swipeState,
                                        anchors = anchors,
                                        thresholds = { _, _ -> FractionalThreshold(0.3f) },
                                        orientation = Orientation.Horizontal
                                    )
                            ) {
                                // 2) Arka plan: sola doğru açıkta kalan kısım
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.error)
                                        .padding(end = 16.dp),             // sağdan iç boşluk
                                    contentAlignment = Alignment.CenterEnd // sağda ve dikey ortada
                                ) {
                                    Icon(
                                        imageVector    = Icons.Default.Delete,
                                        contentDescription = "Sil",
                                        tint           = Color.White,
                                        modifier       = Modifier
                                            .size(24.dp)                   // uygun boyut
                                            .clickable {
                                                postToDelete = post
                                                showDeleteDialog = true
                                            }
                                    )
                                }
                                // 4) Kartı swipeState.offset kadar kaydır ve sabitle
                                HomePostItem(
                                    post = post,
                                    authorProfile = profile,
                                    onUserClick = {},
                                    onPostClick = { onPostClick(post.postId) },
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                swipeState.offset.value.roundToInt(),
                                                0
                                            )
                                        }
                                        .fillMaxWidth()
                                )
                            }
                        } else {
                            HomePostItem(
                                post = post,
                                authorProfile = profile,
                                onUserClick = {},
                                onPostClick = { onPostClick(post.postId) }
                            )
                        }
                    }
                }
            } ?: Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
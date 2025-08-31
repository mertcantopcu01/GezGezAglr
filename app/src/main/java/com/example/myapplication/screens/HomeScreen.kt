package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.myapplication.ui.AppThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onUserClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onPostClick: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val currentUid = AuthService.getCurrentUser()?.uid
    var feedPosts by remember { mutableStateOf(emptyList<Post>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val authorCache = remember { mutableStateMapOf<String, UserProfile?>() }

    LaunchedEffect(currentUid) {
        if (currentUid == null) {
            isLoading = false
            errorMsg = "Oturum açılmamış."
            return@LaunchedEffect
        }
        FirestoreService.getFollowingIds(currentUid) { followingIds ->
            FirestoreService.getPostsByUserIds(followingIds) { posts ->
                feedPosts = posts
                isLoading = false
                posts.map { it.uid }.distinct().forEach { uid ->
                    if (!authorCache.containsKey(uid)) {
                        FirestoreService.getUserProfile(uid) { p -> authorCache[uid] = p }
                    }
                }
            }
        }
    }

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "GezGezAglr",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Ara", )
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Default.Person, contentDescription = "Profil", )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppThemeColors.extra.topBar,
                        titleContentColor = AppThemeColors.extra.onTopBar,
                        navigationIconContentColor = AppThemeColors.extra.onTopBar,
                        actionIconContentColor = AppThemeColors.extra.onTopBar
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = cs.primary
                    )

                    errorMsg != null -> Text(
                        text = errorMsg!!,
                        color = cs.onBackground,
                        modifier = Modifier.align(Alignment.Center),
                        fontFamily = FontFamily.Monospace
                    )

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(feedPosts, key = { it.postId }) { post ->
                                val author = authorCache[post.uid]
                                PostCard(
                                    post = post,
                                    author = author,
                                    onUserClick = onUserClick,
                                    onPostClick = onPostClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    author: UserProfile?,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick(post.postId) },
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // SOL: avatar + kullanıcı adı + konum
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (author?.profileImageUrl != null) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(ctx)
                                .data(author.profileImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(cs.secondary.copy(alpha = 0.2f))
                                )
                            },
                            error = {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(cs.secondary.copy(alpha = 0.2f))
                                )
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onUserClick(author.uid) }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(cs.secondary.copy(alpha = 0.25f))
                                .clickable { author?.uid?.let(onUserClick) }
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = author?.username ?: "Bilinmeyen",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = cs.onSurface,
                            fontFamily = FontFamily.Monospace
                        )
                        val locText = post.location?.takeIf { it.isNotBlank() } ?: "Konum yok"
                        Text(
                            text = locText,
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurface.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(onClick = { /* menü */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü", tint = cs.onSurface)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐ ${post.rating}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.primary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "📍 ${post.location?.takeIf { it.isNotBlank() } ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurface.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // SAĞ: Fotoğraf (varsa)
            post.photoUrl?.let { url ->
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
                                .width(120.dp)
                                .height(160.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(cs.secondary.copy(alpha = 0.1f))
                        )
                    },
                    error = {
                        Box(
                            Modifier
                                .width(120.dp)
                                .height(160.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(cs.secondary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Görsel yok",
                                color = cs.onSurface.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    modifier = Modifier
                        .width(120.dp)
                        .height(160.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }
        }
    }
}

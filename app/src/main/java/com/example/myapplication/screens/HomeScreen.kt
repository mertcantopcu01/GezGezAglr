package com.example.myapplication.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onUserClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onPostClick: (String) -> Unit
) {
    val currentUid = AuthService.getCurrentUser()?.uid
    var feedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(currentUid) {
        if (currentUid == null) {
            isLoading = false
            errorMsg = "Oturum açılmamış."
        } else {
            FirestoreService.getFollowingIds(currentUid) { ids ->
                FirestoreService.getPostsByUserIds(ids) { posts ->
                    feedPosts = posts
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GEZGEZAGLR",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Ara",
                            tint = colors.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil",
                            tint = colors.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = colors.primary,
                    titleContentColor = colors.onPrimary,
                    navigationIconContentColor = colors.onPrimary,
                    actionIconContentColor = colors.onPrimary
                )
            )
        },
        containerColor = colors.background
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.primary
                )
                errorMsg != null -> Text(
                    text = errorMsg!!,
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.error
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(feedPosts) { post ->
                        val authorProfile by produceState<UserProfile?>(initialValue = null, key1 = post.uid) {
                            FirestoreService.getUserProfile(post.uid) { value = it }
                        }
                        HomePostItem(
                            post = post,
                            authorProfile = authorProfile,
                            onUserClick = onUserClick,
                            onPostClick = onPostClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomePostItem(
    post: Post,
    authorProfile: UserProfile?,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val displayImageUrl = post.photoUrls?.firstOrNull() ?: post.photoUrl
    val painter = rememberAsyncImagePainter(model = displayImageUrl)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onPostClick(post.postId) },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                authorProfile?.profileImageUrl?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Yazar fotoğrafı",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { onUserClick(post.uid) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = authorProfile.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { onUserClick(post.uid) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = post.description.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "⭐ ${post.rating}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary
                )
            }

            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
            ) {
                if (displayImageUrl != null) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.onSurface.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "placeholder",
                            tint = colors.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

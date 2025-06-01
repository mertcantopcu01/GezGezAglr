package com.example.myapplication.screens

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile

@Composable
fun HomeScreen(
    onUserClick: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onPostClick: (String) -> Unit
) {
    val currentUid = AuthService.getCurrentUser()?.uid
    var feedPosts by remember { mutableStateOf(emptyList<Post>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end   = 16.dp,
                    top = 80.dp,
                    bottom= 80.dp
                )
            ) {
                items(feedPosts) { post ->
                    // Burada produceState ile kullanıcı profili çekiliyor:
                    val authorProfile by produceState<UserProfile?>(initialValue = null, key1 = post.uid) {
                        FirestoreService.getUserProfile(post.uid) { user ->
                            value = user
                        }
                    }

                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPostClick(post.postId) },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            authorProfile?.let { author ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = author.username,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clickable { onUserClick(post.uid) }
                                            .padding(end = 8.dp)
                                    )
                                    author.profileImageUrl?.let { url ->
                                        Image(
                                            painter = rememberAsyncImagePainter(url),
                                            contentDescription = "Yazar fotoğrafı",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable { onUserClick(author.uid) }
                                        )
                                    }
                                }
                            }

                            // ➤ Post başlığı ve görseli
                            Text(
                                post.title,
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
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "⭐ ${post.rating}    📍 ${post.location ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
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
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_user),
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun HomeScreenFullPreview() {
    // 3a) sample posts
    val samplePosts = listOf(
        Post(postId = "1", uid = "u1", title = "Açık Hava Konseri", photoUrl = "https://picsum.photos/200", rating = 4, location = "İstanbul"),
        Post(postId = "2", uid = "u2", title = "Tech Meetup", photoUrl = null, rating = 3, location = "Ankara"),
        Post(postId = "3", uid = "u1", title = "Sevgi Adası", photoUrl = "https://picsum.photos/201", rating = 5, location = "Adana")
    )
    // 3b) matching fake profiles
    val fakeProfiles = mapOf(
        "u1" to UserProfile(uid = "u1", username = "Ali", profileImageUrl = null),
        "u2" to UserProfile(uid = "u2", username = "Ayşe", profileImageUrl = "https://picsum.photos/50")
    )

    MaterialTheme {
        HomeScreenContent(
            feedPosts       = samplePosts,
            authorProfiles  = fakeProfiles,
            onUserClick     = {},
            onLogout        = {},
            onNavigateToProfile = {},
            onNavigateToSearch  = {},
            onPostClick     = {}
        )
    }
}

@Composable
fun HomeScreenContent(
    feedPosts: List<Post>,
    authorProfiles: Map<String, UserProfile?>,
    onUserClick: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onPostClick: (String) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp, top = 80.dp, bottom = 80.dp)
        ) {
            items(feedPosts) { post ->
                val author = authorProfiles[post.uid]
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPostClick(post.postId) },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        author?.let {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    it.username,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { onUserClick(it.uid) }
                                        .padding(end = 8.dp)
                                )
                                it.profileImageUrl?.let { url ->
                                    Image(
                                        painter = rememberAsyncImagePainter(url),
                                        contentDescription = "Yazar fotoğrafı",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .clickable { onUserClick(it.uid) }
                                    )
                                }
                            }
                        }

                        Text(post.title, style = MaterialTheme.typography.titleMedium)
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
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "⭐ ${post.rating}    📍 ${post.location ?: "-"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onNavigateToProfile,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile")
        }
        IconButton(
            onClick = onNavigateToSearch,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
        IconButton(
            onClick = onLogout,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
        }
    }
}
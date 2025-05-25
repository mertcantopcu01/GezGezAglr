package com.example.myapplication.screens

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
                                        color = MaterialTheme.colorScheme.primary,         // mavi
                                        modifier = Modifier
                                            .clickable { onUserClick(post.uid) }           // doğru UID ile
                                            .padding(end = 8.dp)
                                    )
                                    author.profileImageUrl?.let { url ->
                                        Image(
                                            painter = rememberAsyncImagePainter(url),
                                            contentDescription = "Yazar fotoğrafı",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable { onUserClick(author.uid) }        // resme de tıklanabilir
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

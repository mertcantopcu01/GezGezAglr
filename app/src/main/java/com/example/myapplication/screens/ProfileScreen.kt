package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile

@Composable
fun ProfileScreen() {
    val uid = AuthService.getCurrentUser()?.uid
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val context = LocalContext.current

    // Profil bilgileri ve postları çek
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
                .padding(16.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = rememberAsyncImagePainter(user.profileImageUrl ?: "https://via.placeholder.com/150"),
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

                    // Tweet giriş alanı
                    TweetInputSection(onPostSuccess = {
                        FirestoreService.getUserPosts(uid!!) { updatedPosts -> posts = updatedPosts }
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (posts.isNotEmpty()) {
                items(posts.size) { index ->
                    val post = posts[index]
                    PostCard(post)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                item {
                    Text(
                        text = context.getString(R.string.there_is_no_tweet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun TweetInputSection(onPostSuccess: () -> Unit) {
    var tweetText by remember { mutableStateOf("") }
    val uid = AuthService.getCurrentUser()?.uid ?: return
    val context = LocalContext.current


    Column {
        OutlinedTextField(
            value = tweetText,
            onValueChange = { tweetText = it },
            label = { Text(context.getString(R.string.what_are_you_thinking)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (tweetText.isNotBlank()) {
                    FirestoreService.postTweet(uid, tweetText, null, null) {
                        if (it) {
                            tweetText = ""
                            onPostSuccess()
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(context.getString(R.string.share))
        }
    }
}

@Composable
fun PostCard(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(post.text, style = MaterialTheme.typography.bodyLarge)
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

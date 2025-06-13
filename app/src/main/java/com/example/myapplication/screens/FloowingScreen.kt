package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingScreen(
    userId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val context = LocalContext.current
    val PROTECTED_UID = "KfiILMiSHsU2q7ush2CAVT1ryC33"

    var followingList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    LaunchedEffect(userId) {
        FirestoreService.getFollowingList(userId) { followingList = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takip Edilenler") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(followingList) { user ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUserClick(user.uid) }
                        .padding(vertical = 4.dp),
                    leadingContent = {
                        Image(
                            painter = rememberAsyncImagePainter(user.profileImageUrl ?: ""),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    },
                    headlineContent = {
                        Text(text = user.username, style = MaterialTheme.typography.bodyLarge)
                    },
                    supportingContent = {
                        user.bio?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    },
                    trailingContent = {
                        IconButton(onClick = {
                            if (user.uid == PROTECTED_UID) {
                                Toast
                                    .makeText(context, "Bu kullanıcı takipten çıkarılmaz", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                FirestoreService.unfollowUser(
                                    followerId = userId,
                                    followingId = user.uid
                                ) { success, error ->
                                    if (success) {
                                        Toast.makeText(context, "Takipten çıkarıldı", Toast.LENGTH_SHORT).show()
                                        // Listeyi hemen güncellemek için:
                                        followingList = followingList.filter { it.uid != user.uid }
                                    } else {
                                        Toast.makeText(context, "Hata: $error", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Takipten çıkar"
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Divider()
            }
        }
    }
}



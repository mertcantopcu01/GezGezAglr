package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.AppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersScreen(
    userId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    var list by remember { mutableStateOf<List<UserProfile>>(emptyList()) }

    LaunchedEffect(userId) {
        FirestoreService.getFollowersList(userId) { list = it }
    }

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Takipçiler",
                            color = cs.onPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = cs.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cs.primary,
                        titleContentColor = cs.onPrimary,
                        navigationIconContentColor = cs.onPrimary
                    )
                )
            },
            containerColor = cs.background
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(list, key = { it.uid }) { u ->
                    FollowerRowCard(
                        profile = u,
                        onClick = { onUserClick(u.uid) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowerRowCard(
    profile: UserProfile,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(profile.profileImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                loading = {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(cs.secondary.copy(alpha = 0.2f), shape = CircleShape)
                    )
                },
                error = {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(cs.secondary.copy(alpha = 0.2f), shape = CircleShape)
                    )
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = profile.username,
                    style = MaterialTheme.typography.titleSmall,
                    color = cs.onSurface,
                    fontFamily = FontFamily.Monospace
                )
                profile.bio?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

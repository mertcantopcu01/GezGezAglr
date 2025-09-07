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
import com.example.myapplication.ui.AppThemeColors   // 👈 eklendi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingScreen(
    userId: String,
    isOwner: Boolean,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current

    var list by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    val loadingIds = remember { mutableStateListOf<String>() }
    var pendingUnfollow by remember { mutableStateOf<UserProfile?>(null) }
    var dialogSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        FirestoreService.getFollowingList(userId) { list = it }
    }

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Takip Edilenler",
                            color = AppThemeColors.extra.onTopBar,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = AppThemeColors.extra.onTopBar
                            )
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
                    val isLoading = loadingIds.contains(u.uid)
                    FollowingRowCard(
                        profile = u,
                        onClick = { onUserClick(u.uid) },
                        isLoading = isLoading,
                        canUnfollow = isOwner,                 // <<< sadece sahibine buton göster
                        onUnfollowClick = {
                            if (isOwner) pendingUnfollow = u   // değilse hiçbir şey yapma
                        }
                    )
                }
            }
        }

        // Onay Dialog — sadece kendi sayfasında
        val target = pendingUnfollow
        if (isOwner && target != null) {
            AlertDialog(
                onDismissRequest = { if (!dialogSubmitting) pendingUnfollow = null },
                title = { Text("Takibi bırak?", color = cs.onSurface) },
                text = {
                    Text(
                        "“${target.username}” kullanıcısını takip etmeyi bırakmak istediğine emin misin?",
                        color = cs.onSurface
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = !dialogSubmitting,
                        onClick = {
                            dialogSubmitting = true
                            val old = list
                            loadingIds.add(target.uid)
                            // Optimistic UI
                            list = list.filter { it.uid != target.uid }
                            pendingUnfollow = null
                            dialogSubmitting = false

                            FirestoreService.unfollowUser(
                                followerId = userId,
                                followingId = target.uid
                            ) { success, _ ->
                                loadingIds.remove(target.uid)
                                if (!success) list = old
                            }
                        }
                    ) {
                        Text("Evet, çıkar", color = cs.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !dialogSubmitting,
                        onClick = { if (!dialogSubmitting) pendingUnfollow = null }
                    ) { Text("İptal", color = cs.primary) }
                },
                containerColor = cs.surface,
                titleContentColor = cs.onSurface,
                textContentColor = cs.onSurface
            )
        }
    }
}

@Composable
private fun FollowingRowCard(
    profile: UserProfile,
    onClick: () -> Unit,
    isLoading: Boolean,
    canUnfollow: Boolean,                 // <<< EKLENDİ
    onUnfollowClick: () -> Unit
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

            Column(modifier = Modifier.weight(1f)) {
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

            // --- Takibi bırak butonu sadece canUnfollow == true iken ---
            if (canUnfollow) {
                OutlinedButton(
                    onClick = onUnfollowClick,
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    border = ButtonDefaults.outlinedButtonBorder,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isLoading) cs.onSurface.copy(alpha = 0.5f) else cs.primary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 6.dp),
                            strokeWidth = 2.dp,
                            color = cs.primary
                        )
                    }
                    Text(
                        text = if (isLoading) "Çıkılıyor..." else "Takibi bırak",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

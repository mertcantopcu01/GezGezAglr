@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.AppBackground
import com.example.myapplication.ui.AppThemeColors

@Composable
fun UserProfileScreen(
    userId: String,
    onCreatePost: () -> Unit,
    onPostClick: (String) -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme
    val currentUserId = AuthService.getCurrentUser()?.uid
    var showLogoutDialog by remember { mutableStateOf(false) }

    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isFollowing by remember { mutableStateOf(false) }
    var followersCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var tabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(userId) {
        FirestoreService.getUserProfile(userId) { profile = it }
        FirestoreService.getUserPosts(userId) { posts = it }
        FirestoreService.getFollowersCount(userId) { followersCount = it }
        FirestoreService.getFollowingCount(userId) { followingCount = it }
        if (currentUserId != null && currentUserId != userId) {
            FirestoreService.isFollowing(currentUserId, userId) { isFollowing = it }
        }
    }

    AppBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = profile?.username ?: "Profil",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        onBack?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = AppThemeColors.extra.onTopBar
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentUserId == userId) {
                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Çıkış Yap",
                                    tint = AppThemeColors.extra.onTopBar
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = AppThemeColors.extra.topBar,
                        titleContentColor = AppThemeColors.extra.onTopBar,
                        navigationIconContentColor = AppThemeColors.extra.onTopBar,
                        actionIconContentColor = AppThemeColors.extra.onTopBar
                    )
                )
            },
            containerColor = cs.background
        ) { padding ->
            if (profile == null) {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center), color = cs.primary)
                }
                return@Scaffold
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    ProfileHeaderBlock(
                        profile = profile!!,
                        postsCount = posts.size,
                        followersCount = followersCount,
                        followingCount = followingCount,
                        isOwn = currentUserId == userId,
                        isFollowing = isFollowing,
                        onToggleFollow = {
                            val me = currentUserId ?: return@ProfileHeaderBlock
                            if (isFollowing) {
                                FirestoreService.unfollowUser(me, userId) { ok, _ ->
                                    if (ok) {
                                        isFollowing = false
                                        followersCount = (followersCount - 1).coerceAtLeast(0)
                                    }
                                }
                            } else {
                                FirestoreService.followUser(me, userId) { ok, _ ->
                                    if (ok) {
                                        isFollowing = true
                                        followersCount += 1
                                    }
                                }
                            }
                        },
                        onFollowersTap = { onFollowersClick(userId) },
                        onFollowingTap = { onFollowingClick(userId) },
                        onEditProfile = onEditProfile,
                        onCreatePost = onCreatePost
                    )
                }

                // Tabs
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    TabRow(
                        selectedTabIndex = tabIndex,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        containerColor = cs.background,
                        contentColor = cs.primary
                    ) {
                        Tab(
                            selected = tabIndex == 0,
                            onClick = { tabIndex = 0 },
                            text = { Text("Gönderiler") }
                        )
                        Tab(
                            selected = tabIndex == 1,
                            onClick = { tabIndex = 1 },
                            text = { Text("Kaydedilenler") }
                        )
                    }
                }

                if (tabIndex == 0) {
                    // kapak = photos[0] varsa onu, yoksa photoUrl
                    items(posts, key = { it.postId }) { post ->
                        PostGridItem(
                            url = post.coverUrl(),
                            onClick = { onPostClick(post.postId) }
                        )
                    }
                } else {
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Kaydedilenler yakında",
                                color = cs.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

    }

    if (showLogoutDialog) {
        ConfirmLogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }
}

@Composable
private fun ProfileHeaderBlock(
    profile: UserProfile,
    postsCount: Int,
    followersCount: Int,
    followingCount: Int,
    isOwn: Boolean,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    onFollowersTap: () -> Unit,
    onFollowingTap: () -> Unit,
    onEditProfile: () -> Unit,
    onCreatePost: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(color = cs.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        val ctx = LocalContext.current
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(ctx).data(profile.profileImageUrl).crossfade(true).build(),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = profile.username,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = cs.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        profile.username.takeIf { it.isNotBlank() }?.let {
            val handle = "@${it.lowercase().replace(' ', '.')}"
            Spacer(Modifier.height(2.dp))
            Text(
                text = handle,
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onBackground.copy(alpha = 0.75f)
            )
        }

        profile.bio?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onBackground.copy(alpha = 0.85f)
            )
        }

        Spacer(Modifier.height(14.dp))

        // İstatistikler
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatButton(value = postsCount.compact(), label = "Gönderi", onClick = null)
            StatButton(value = followersCount.compact(), label = "Takipçi", onClick = onFollowersTap)
            StatButton(value = followingCount.compact(), label = "Takip", onClick = onFollowingTap)
        }

        Spacer(Modifier.height(14.dp))

        if (isOwn) {
            OutlinedButton(
                onClick = onEditProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = MaterialTheme.shapes.medium
            ) { Text("Profili Düzenle") }

            Spacer(Modifier.height(8.dp))

        } else {
            Button(
                onClick = onToggleFollow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(if (isFollowing) "Takipten Çık" else "Takip Et")
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatButton(
    value: String,
    label: String,
    onClick: (() -> Unit)?
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = onClick != null, role = Role.Button) { onClick?.invoke() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .defaultMinSize(minWidth = 84.dp),
        color = androidx.compose.ui.graphics.Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = cs.onBackground
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PostGridItem(url: String?, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.small)
            .background(cs.surfaceVariant.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
    ) {
        if (url != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(ctx).data(url).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
private fun ConfirmLogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Çıkış yapmak istediğinizden emin misiniz?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = cs.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Tekrar giriş yapmanız gerekecektir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(bottomStart = 20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (isDark) cs.surfaceVariant.copy(alpha = 0.35f)
                                else cs.surfaceVariant,
                            contentColor = cs.onSurface
                        ),
                        elevation = null
                    ) { Text("Hayır", fontWeight = FontWeight.Medium) }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(bottomEnd = 20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary
                        ),
                        elevation = null
                    ) { Text("Evet", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

/* ------- küçük yardımcılar ------- */

private fun Int.compact(): String =
    when {
        this >= 1_000_000 -> String.format("%.1fM", this / 1_000_000f).trimEnd('0').trimEnd('.')
        this >= 1_000     -> String.format("%.1fK", this / 1_000f).trimEnd('0').trimEnd('.')
        else              -> this.toString()
    }


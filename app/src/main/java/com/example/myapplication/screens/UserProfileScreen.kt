package com.example.myapplication.screens

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.AppBackground
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// Yalnızca iki durum: kapalı ve 24dp reveal
enum class CardPos { Closed, Revealed }

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
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
    val ctx = LocalContext.current
    val currentUserId = AuthService.getCurrentUser()?.uid

    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isFollowing by remember { mutableStateOf(false) }
    var followersCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }

    var postToDelete by remember { mutableStateOf<Post?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // İlk girişte sadece bir kere peek
    var hasPeeked by rememberSaveable(userId) { mutableStateOf(false) }

    LaunchedEffect(userId) {
        FirestoreService.getUserProfile(userId)    { profile = it }
        FirestoreService.getUserPosts(userId)      { posts = it }
        FirestoreService.getFollowersCount(userId) { followersCount = it }
        FirestoreService.getFollowingCount(userId) { followingCount = it }
        val me = currentUserId
        if (me != null && me != userId) {
            FirestoreService.isFollowing(me, userId) { isFollowing = it }
        }
    }

    // Silme onayı
    if (showDeleteDialog && postToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Gönderiyi Sil", fontFamily = FontFamily.Monospace, color = cs.onSurface) },
            text  = { Text("Bu gönderiyi silmek istediğinize emin misiniz?", color = cs.onSurface) },
            confirmButton = {
                TextButton(onClick = {
                    FirestoreService.deletePost(postToDelete!!.postId) { success ->
                        if (success) posts = posts.filterNot { it.postId == postToDelete!!.postId }
                        postToDelete = null
                        showDeleteDialog = false
                    }
                }) { Text("Evet", color = cs.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Hayır", color = cs.primary) }
            },
            containerColor = cs.surface,
            titleContentColor = cs.onSurface,
            textContentColor = cs.onSurface
        )
    }

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            profile?.username ?: "Profil",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.onPrimary
                        )
                    },
                    navigationIcon = {
                        onBack?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = cs.onPrimary
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentUserId == userId) {
                            IconButton(onClick = {
                                AuthService.signOut()
                                onLogout()
                            }) {
                                Icon(Icons.Filled.ExitToApp, contentDescription = "Çıkış", tint = cs.onPrimary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cs.primary,
                        titleContentColor = cs.onPrimary,
                        navigationIconContentColor = cs.onPrimary,
                        actionIconContentColor = cs.onPrimary
                    )
                )
            },
            containerColor = cs.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (profile == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = cs.primary
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // HEADER
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cs.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(ctx)
                                            .data(profile!!.profileImageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            Box(
                                                Modifier
                                                    .size(96.dp)
                                                    .clip(CircleShape)
                                                    .background(cs.secondary.copy(alpha = 0.2f))
                                            )
                                        },
                                        error = {
                                            Box(
                                                Modifier
                                                    .size(96.dp)
                                                    .clip(CircleShape)
                                                    .background(cs.secondary.copy(alpha = 0.2f))
                                            )
                                        },
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                    )

                                    Spacer(Modifier.height(10.dp))

                                    Text(
                                        profile!!.username,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = FontFamily.Monospace,
                                        color = cs.onSurface
                                    )

                                    profile!!.bio?.takeIf { it.isNotBlank() }?.let {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = cs.onSurface.copy(alpha = 0.8f)
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .clickable(onClick = { onFollowersClick(userId) })
                                        ) {
                                            Text(
                                                "$followersCount",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontFamily = FontFamily.Monospace,
                                                color = cs.onSurface
                                            )
                                            Text("Takipçi", style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(0.7f))
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .clickable(onClick = { onFollowingClick(userId) })
                                        ) {
                                            Text(
                                                "$followingCount",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontFamily = FontFamily.Monospace,
                                                color = cs.onSurface
                                            )
                                            Text("Takip Edilen", style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(0.7f))
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    if (currentUserId == userId) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = onCreatePost,
                                                modifier = Modifier.weight(1f),
                                                shape = MaterialTheme.shapes.medium,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = cs.primary,
                                                    contentColor = cs.onPrimary
                                                )
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null) // 🔹 Gönderi ekleme ikonu
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Gönderi Paylaş", fontFamily = FontFamily.Monospace)
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = onEditProfile,
                                                modifier = Modifier.weight(1f),
                                                shape = MaterialTheme.shapes.medium,
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.primary)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically, // 🔹 ikon ve text hizalı
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Profili Düzenle", fontFamily = FontFamily.Monospace)
                                                }
                                            }
                                        }

                                    } else if (currentUserId != null) {
                                        Button(
                                            onClick = {
                                                if (isFollowing) {
                                                    FirestoreService.unfollowUser(currentUserId, userId) { ok, _ ->
                                                        if (ok) { isFollowing = false; followersCount-- }
                                                    }
                                                } else {
                                                    FirestoreService.followUser(currentUserId, userId) { ok, _ ->
                                                        if (ok) { isFollowing = true; followersCount++ }
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = MaterialTheme.shapes.medium,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isFollowing) cs.errorContainer else cs.primary,
                                                contentColor = if (isFollowing) cs.onErrorContainer else cs.onPrimary
                                            )
                                        ) { Text(if (isFollowing) "Takipten Çık" else "Takip Et", fontFamily = FontFamily.Monospace) }
                                    }
                                }
                            }
                        }

                        // GÖNDERİLER
                        itemsIndexed(posts, key = { _, it -> it.postId }) { index, post ->
                            val isMine = currentUserId == userId
                            val shouldPeek = isMine && !hasPeeked && index == 0
                            RevealDismissPostCard(
                                post = post,
                                cs = cs,
                                isMine = isMine,
                                onClick = { onPostClick(post.postId) },
                                onRequestDelete = {
                                    postToDelete = post
                                    showDeleteDialog = true
                                },
                                ctx = ctx,
                                doPeek = shouldPeek,
                                onPeekDone = { hasPeeked = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun RevealDismissPostCard(
    post: Post,
    cs: ColorScheme,
    isMine: Boolean,
    onClick: () -> Unit,
    onRequestDelete: () -> Unit,
    ctx: Context,
    doPeek: Boolean = false,
    onPeekDone: () -> Unit = {}
) {
    if (!isMine) {
        Card(
            colors = CardDefaults.cardColors(containerColor = cs.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            PostContent(post = post, cs = cs, ctx = ctx)
        }
        return
    }

    val density = LocalDensity.current
    val revealPx = with(density) { 32.dp.toPx() } // 🔹 24dp sola "peek"
    var widthPx by remember { mutableStateOf(0f) }
    val swipeState = rememberSwipeableState(initialValue = CardPos.Closed)

    // Sadece Closed ↔ Revealed (dismiss yok)
    val anchors = remember(widthPx, revealPx) {
        mapOf(
            0f to CardPos.Closed,
            -revealPx to CardPos.Revealed
        )
    }

    // İlk girişte otomatik peek (aç → kısa bekle → kapa)
    LaunchedEffect(doPeek) {
        if (doPeek) {
            swipeState.snapTo(CardPos.Closed)
            swipeState.animateTo(CardPos.Revealed)
            delay(650)
            swipeState.animateTo(CardPos.Closed)
            onPeekDone()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { widthPx = it.size.width.toFloat() }
            .height(IntrinsicSize.Min)
    ) {
        // ARKA PLAN: kartın şekliyle clip + kartla uyumlu renk
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(MaterialTheme.shapes.large)
                .background(cs.surface)
        ) {
            // ÇÖP KUTUSU: TAM en sağda, padding/margin = 0
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Sil",
                tint = cs.error,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .clickable(onClick = onRequestDelete)
            )
        }

        // ÖN PLAN: Kart
        Card(
            colors = CardDefaults.cardColors(containerColor = cs.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(swipeState.offset.value.roundToInt(), 0) }
                .swipeable(
                    state = swipeState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) }, // 24dp'ye kolay otursun
                    orientation = Orientation.Horizontal,
                    enabled = true,
                    reverseDirection = false, // sağdan sola negatif offset
                    resistance = null
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onRequestDelete
                )
        ) {
            PostContent(post = post, cs = cs, ctx = ctx)
        }
    }
}

@Composable
private fun PostContent(
    post: Post,
    cs: ColorScheme,
    ctx: Context
) {
    Column(Modifier.padding(16.dp)) {
        Text(
            post.title,
            style = MaterialTheme.typography.titleMedium,
            color = cs.onSurface,
            fontFamily = FontFamily.Monospace
        )
        post.photoUrl?.let { url ->
            Spacer(Modifier.height(10.dp))
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(ctx).data(url).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(cs.secondary.copy(alpha = 0.1f))
                    )
                },
                error = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(cs.secondary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Görsel yüklenemedi",
                            color = cs.onSurface.copy(0.7f),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        }
    }
}

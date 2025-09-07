@file:OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.example.myapplication.screens

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.ui.AppBackground
import com.example.myapplication.ui.AppThemeColors
import kotlinx.coroutines.delay

@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current

    var post by remember { mutableStateOf<Post?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Tam ekran görsel
    var showImageFullScreen by rememberSaveable { mutableStateOf(false) }
    var fullScreenStartIndex by rememberSaveable { mutableStateOf(0) }

    // Silme dialog state
    var showDelete by rememberSaveable { mutableStateOf(false) }
    var deleting by rememberSaveable { mutableStateOf(false) }

    val scroll = rememberScrollState()

    // "Bir kere göster" ipucu ayarı
    val prefs = remember { ctx.getSharedPreferences("hints", Context.MODE_PRIVATE) }
    var everShown by remember { mutableStateOf(prefs.getBoolean("hint_postdetail_scroll", false)) }
    var showHint by rememberSaveable { mutableStateOf(false) }

    // Postu yükle
    LaunchedEffect(postId) {
        FirestoreService.getPostById(postId) {
            post = it
            loading = false
        }
    }

    // Foto listesi: her zaman üst scope’ta hesaplanır (post null ise boş)
    val photos: List<String> = remember(post) {
        when {
            post?.photos?.isNotEmpty() == true -> post!!.photos
            !post?.photoUrl.isNullOrBlank() -> listOf(post!!.photoUrl!!)
            else -> emptyList()
        }
    }

    // İçerik yüklendikten sonra gerçekten scroll var mı?
    LaunchedEffect(scroll.maxValue, everShown, loading) {
        if (!loading && !everShown && scroll.maxValue > 0) {
            showHint = true
        }
    }

    // Kullanıcı kaydırınca ipucunu kapat
    LaunchedEffect(scroll.isScrollInProgress) {
        if (scroll.isScrollInProgress && showHint) {
            showHint = false
            prefs.edit().putBoolean("hint_postdetail_scroll", true).apply()
            everShown = true
        }
    }

    val me = remember { AuthService.getCurrentUserId().orEmpty() }
    val isOwner = remember(post, me) { post?.uid == me }
    val isAdmin = remember(me) { AuthService.isSuperUser() }
    val canDelete = !loading && post != null && (isOwner || isAdmin)

    Log.e("TAG", "PostDetailScreen: me=$me postUid=${post?.uid}")

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = AppThemeColors.extra.onTopBar
                            )
                        }
                    },
                    title = {
                        Text(
                            post?.title?.takeIf { it.isNotBlank() } ?: "Gönderi",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = AppThemeColors.extra.onTopBar,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    actions = {
                        if (canDelete) {
                            IconButton(onClick = { showDelete = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Gönderiyi sil",
                                    tint = Color(0xFFD32F2F)
                                )
                            }
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
            Box(Modifier.fillMaxSize()) {

                when {
                    loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = cs.primary) }
                    }

                    post == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) { Text("Gönderi bulunamadı.", color = cs.onBackground) }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .verticalScroll(scroll)
                        ) {
                            // === KAPAK GALERİ ===
                            if (photos.isNotEmpty()) {
                                val pagerState = rememberPagerState(pageCount = { photos.size })

                                Box {
                                    HorizontalPager(
                                        state = pagerState,
                                        flingBehavior = PagerDefaults.flingBehavior(pagerState),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp)
                                    ) { page ->
                                        val url = photos[page]
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(url)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            loading = {
                                                Box(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(260.dp)
                                                        .background(cs.secondary.copy(alpha = 0.1f))
                                                )
                                            },
                                            error = {
                                                Box(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(260.dp)
                                                        .background(cs.secondary.copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "Görsel yüklenemedi",
                                                        color = cs.onSurface.copy(alpha = 0.7f),
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(260.dp)
                                                .clickable {
                                                    fullScreenStartIndex = page
                                                    showImageFullScreen = true
                                                }
                                        )
                                    }

                                    // Alttan hafif karartma
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    0f to Color.Transparent,
                                                    0.7f to Color.Transparent,
                                                    1f to cs.scrim.copy(alpha = 0.25f)
                                                )
                                            )
                                    )

                                    // Dot indicator
                                    if (photos.size > 1) {
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 10.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            repeat(photos.size) { i ->
                                                val active = i == pagerState.currentPage
                                                Box(
                                                    modifier = Modifier
                                                        .size(if (active) 10.dp else 8.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (active) cs.primary
                                                            else cs.onSurface.copy(alpha = 0.35f)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // === DETAY KARTI ===
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = cs.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = post!!.title,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = cs.onSurface,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Spacer(Modifier.height(10.dp))

                                    // ROZETLER: Puan + Konum
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Row(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(cs.primary.copy(alpha = 0.12f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = cs.primary
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                "${post!!.rating}/10",
                                                color = cs.primary,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Spacer(Modifier.width(10.dp))

                                        (post!!.location?.takeIf { it.isNotBlank() })?.let { loc ->
                                            Row(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(cs.secondaryContainer.copy(alpha = 0.5f))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "📍 $loc",
                                                    color = cs.onSecondaryContainer,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    // Açıklama
                                    if (post!!.description.isNotBlank()) {
                                        Text(
                                            text = post!!.description,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = cs.onSurface.copy(alpha = 0.9F),
                                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                        )
                                    } else {
                                        Text(
                                            text = "Açıklama bulunmuyor.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = cs.onSurface.copy(alpha = 0.7f),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // === Tam ekran galeri overlay ===
                if (showImageFullScreen && photos.isNotEmpty()) {
                    FullScreenGallery(
                        photos = photos,
                        startIndex = fullScreenStartIndex,
                        onClose = { showImageFullScreen = false }
                    )
                }

                // --- Kaydırma ipucu overlay (en üstte) ---
                ScrollHintOverlay(
                    visible = showHint,
                    onDismiss = {
                        showHint = false
                        prefs.edit().putBoolean("hint_postdetail_scroll", true).apply()
                        everShown = true
                    }
                )
            }
        }

        // --- Silme Onay Diyaloğu ---
        if (showDelete && post != null) {
            AlertDialog(
                onDismissRequest = { if (!deleting) showDelete = false },
                title = { Text("Gönderiyi sil?", color = cs.onSurface) },
                text = { Text("Bu işlem geri alınamaz.", color = cs.onSurface) },
                confirmButton = {
                    TextButton(
                        enabled = !deleting,
                        onClick = {
                            deleting = true
                            FirestoreService.deletePostWithImages(post!!) { ok ->
                                deleting = false
                                showDelete = false
                                if (ok) onBack()
                            }
                        }
                    ) {
                        if (deleting) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Evet, sil", color = cs.error)
                    }
                },
                dismissButton = {
                    TextButton(enabled = !deleting, onClick = { showDelete = false }) {
                        Text("İptal", color = cs.primary)
                    }
                },
                containerColor = cs.surface,
                titleContentColor = cs.onSurface,
                textContentColor = cs.onSurface
            )
        }
    }
}

/* -------------------------------------------------- */
/* Overlay: “Aşağı kaydırabilirsiniz” tek-sefer ipucu */
/* -------------------------------------------------- */
@Composable
private fun ScrollHintOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val infinite = rememberInfiniteTransition(label = "scrollHint")
    val offsetY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    val alpha by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // 2.5 sn sonra kendiliğinden kapan
    LaunchedEffect(Unit) {
        delay(2500)
        onDismiss()
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visible = visible) {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .offset(y = offsetY.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDismiss() }
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Aşağı kaydırabilirsiniz",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/* ------------------------ */
/* Tam ekran galeri & zoom */
/* ------------------------ */
@Composable
private fun FullScreenGallery(
    photos: List<String>,
    startIndex: Int,
    onClose: () -> Unit
) {
    var currentScale by remember { mutableStateOf(1f) }
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { photos.size })

    // Arka plan: yarı saydam karartma (tam ekran değil)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
    ) {
        // İçerik kutusu: her yerden 12dp margin, köşeler yuvarlak
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)                         // <-- her yerden 12dp
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)                // görüntüleyici arka planı
                .align(Alignment.Center)
        ) {
            // Kaydırılabilir görsel galerisi
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = currentScale <= 1.01f, // zoom > 1 iken sayfa kaydırma kilit
                flingBehavior = PagerDefaults.flingBehavior(pagerState),
                modifier = Modifier.matchParentSize()
            ) { page ->
                ZoomableImage(
                    imageUrl = photos[page],
                    onScaleChanged = { s -> currentScale = s }
                )
            }

            // Sayfa noktaları
            if (photos.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(photos.size) { i ->
                        val active = i == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (active) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (active) Color.White else Color.White.copy(alpha = 0.35f))
                        )
                    }
                }
            }

            // Kapat butonu (kutu içinde, 12dp margin’in içinde kalır)
            IconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                onClick = onClose
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Kapat",
                    tint = Color.White
                )
            }
        }
    }
}


@Composable
private fun ZoomableImage(
    imageUrl: String,
    onScaleChanged: (Float) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    // Sadece iki parmak var ya da zoom > 1 ise transformable aktif olsun
    var enableTransform by remember { mutableStateOf(false) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        val newOffset =
            if (newScale <= 1f) androidx.compose.ui.geometry.Offset.Zero
            else offset + panChange

        scale = newScale
        offset = newOffset
        onScaleChanged(scale)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)

            // Kaç parmak basılı takip et → enableTransform güncelle
            .pointerInput(scale) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointersDown = event.changes.count { it.pressed }
                        // İki parmak varsa ya da zaten zoom yapıldıysa transformable açık
                        enableTransform = (pointersDown > 1) || (scale > 1f)
                    }
                }
            }

            // Double-tap ile hızlı zoom in/out
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = androidx.compose.ui.geometry.Offset.Zero
                        } else {
                            scale = 2f
                        }
                        onScaleChanged(scale)
                        // zoom 1'e düştüyse pager swipe tekrar aktif olsun
                        enableTransform = scale > 1f
                    }
                )
            }

            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
            // transformable'ı yalnızca gerekli olduğunda uygula
            .then(if (enableTransform) Modifier.transformable(transformState) else Modifier)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray.copy(alpha = 0.2f))
                )
            },
            error = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Görsel yüklenemedi", color = Color.White.copy(alpha = 0.8f))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

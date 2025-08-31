package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.ui.AppBackground
import com.example.myapplication.ui.AppThemeColors   // <-- EKLENDİ

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    var post by remember { mutableStateOf<Post?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scroll = rememberScrollState()

    LaunchedEffect(postId) {
        FirestoreService.getPostById(postId) {
            post = it
            loading = false
        }
    }

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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppThemeColors.extra.topBar,      // <-- primary yerine burası
                        titleContentColor = AppThemeColors.extra.onTopBar,  // metin rengi
                        navigationIconContentColor = AppThemeColors.extra.onTopBar
                    )
                )
            },
            containerColor = cs.background
        ) { padding ->
            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = cs.primary)
                    }
                }

                post == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Gönderi bulunamadı.", color = cs.onBackground)
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(scroll)
                    ) {
                        // KAPAK GÖRSELİ
                        post!!.photoUrl?.let { url ->
                            Box {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(ctx)
                                        .data(url)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(240.dp)
                                                .background(cs.secondary.copy(alpha = 0.1f))
                                        )
                                    },
                                    error = {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(240.dp)
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
                                        .height(240.dp)
                                )
                                // Alttan hafif karartma (tema uyumlu)
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
                            }
                        }

                        // DETAY KARTI
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
                                    // Puan rozeti
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

                                    // Konum etiketi (varsa)
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
                                if (!post!!.description.isNullOrBlank()) {
                                    Text(
                                        text = post!!.description!!,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = cs.onSurface.copy(alpha = 0.9f),
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
        }
    }
}

package com.example.myapplication.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post
import com.example.myapplication.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit
) {
    AppTheme {
        var post by remember { mutableStateOf<Post?>(null) }
        LaunchedEffect(postId) {
            FirestoreService.getPostById(postId) { post = it }
        }
        BackHandler { onBack() }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    },
                    title = {
                        Text(
                            text = post?.title.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { contentPadding ->
            post?.let { itPost ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(contentPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = itPost.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Puan: ${itPost.rating}/10",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = itPost.description.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Fotoğrafları listele (tek URL veya çoklu URL)
                    val urls = itPost.photoUrls ?: listOfNotNull(itPost.photoUrl)
                    if (urls.isNotEmpty()) {
                        // PagerState ile sayfa sayısını belirt
                        val pagerState = rememberPagerState(
                            initialPage = 0,
                            pageCount = { urls.size }
                        )
                        val currentPage by remember { derivedStateOf { pagerState.currentPage } }

                        // Resimleri tam ekranda kaydırılabilir pager ile göster
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(MaterialTheme.shapes.medium)
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                pageSpacing = 8.dp
                            ) { page ->
                                Image(
                                    painter = rememberAsyncImagePainter(urls[page]),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Sayfa göstergesi (ör. 1/5, 2/5)
                        Text(
                            text = "${currentPage + 1}/${urls.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
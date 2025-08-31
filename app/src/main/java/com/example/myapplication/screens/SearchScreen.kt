package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
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
import com.example.myapplication.ui.TextFieldStyles
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onUserSelected: (String) -> Unit,
    onBack: () -> Unit   // ✅ geri dönüş callback
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val tfColors = TextFieldStyles.defaultTextFieldColors()

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    // Basit debounce
    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            isSearching = false
            hasSearched = false
        } else {
            isSearching = true
            delay(300)
            FirestoreService.searchUsers(query) { found ->
                results = found
                isSearching = false
                hasSearched = true
            }
        }
    }

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Kullanıcı Ara",
                            fontFamily = FontFamily.Monospace,
                            color = cs.onPrimary
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // 🔍 Arama kutusu
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kullanıcı ara", fontFamily = FontFamily.Monospace) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Temizle")
                            }
                        }
                    },
                    singleLine = true,
                    colors = tfColors
                )

                Spacer(Modifier.height(12.dp))

                when {
                    isSearching -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = cs.primary)
                        }
                    }
                    results.isEmpty() && hasSearched -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                "Sonuç bulunamadı.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.onBackground.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(results, key = { it.uid }) { user ->
                                UserRowCard(
                                    profile = user,
                                    onClick = { if (user.uid.isNotBlank()) onUserSelected(user.uid) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun UserRowCard(
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

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = cs.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

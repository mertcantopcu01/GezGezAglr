@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.UserProfile
import kotlinx.coroutines.delay

@Composable
fun SearchTabScreen(
    onUserSelected: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    // Debounce + arama
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Üst başlık: “Ara”
        Text(
            text = "Ara",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = cs.onBackground
        )

        // Arama alanı (yuvarlak, dolu arka plan)
        val fieldBg = if (isDark) cs.surfaceVariant.copy(alpha = 0.25f) else Color(0xFFF1F2F4)
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = { Text("Kullanıcı ara...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Temizle")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg,
                disabledContainerColor = fieldBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = cs.primary
            )
        )

        Spacer(Modifier.height(12.dp))

        when {
            isSearching -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = cs.primary) }
            }

            results.isEmpty() && hasSearched -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.TopCenter
                ) { Text("Sonuç bulunamadı", color = cs.onBackground.copy(alpha = 0.7f)) }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(results, key = { it.uid }) { user ->
                        UserRowItem(
                            profile = user,
                            onClick = { if (user.uid.isNotBlank()) onUserSelected(user.uid) }
                        )
                        Divider(color = cs.outline.copy(alpha = 0.15f))
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRowItem(
    profile: UserProfile,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(ctx)
                .data(profile.profileImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            loading = {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(cs.surfaceVariant.copy(alpha = 0.4f), CircleShape)
                )
            },
            error = {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(cs.surfaceVariant.copy(alpha = 0.4f), CircleShape)
                )
            }
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.username,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = cs.onBackground
            )
            profile.bio?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

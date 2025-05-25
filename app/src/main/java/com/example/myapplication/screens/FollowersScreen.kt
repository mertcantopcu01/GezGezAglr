package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersScreen(
    userId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit   // eklendi
) {
    var list by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    LaunchedEffect(userId) {
        FirestoreService.getFollowersList(userId) { list = it }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takipçiler") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(list) { u ->
                ListItem(
                    modifier = Modifier
                        .clickable { onUserClick(u.uid) }
                        .padding(vertical = 4.dp),
                    headlineContent = { Text(u.username) },
                    leadingContent  = {
                        Image(
                            painter = rememberAsyncImagePainter(u.profileImageUrl ?: ""),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    }
                )
                Divider()
            }
        }
    }
}
package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.UserProfile

@Composable
fun SearchScreen(onUserSelected: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (query.isNotBlank()) {
                    FirestoreService.searchUsers(query) { foundUsers ->
                        results = foundUsers
                    }
                } else {
                    results = emptyList()
                }
            },
            label = { Text(context.getString(R.string.search_user)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(results) { user ->
                ListItem(
                    headlineContent = { Text(user.username) },
                    supportingContent = {
                        user.bio?.let {
                            Text(it)
                        }
                    },
                    leadingContent = {
                        Image(
                            painter = rememberAsyncImagePainter(user.profileImageUrl ?: ""),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = user.uid.isNotBlank()) {
                            onUserSelected(user.uid)
                        }
                        .padding(vertical = 4.dp)
                )
                Divider()
            }
        }
    }
}

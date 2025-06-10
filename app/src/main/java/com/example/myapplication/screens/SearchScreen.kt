package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onUserSelected: (String) -> Unit
) {
    AppTheme {
        var query by remember { mutableStateOf("") }
        var results by remember { mutableStateOf<List<UserProfile>>(emptyList()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    },
                    title = { Text("Kullanıcı Ara") },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { text ->
                        query = text
                        if (text.isNotBlank()) {
                            FirestoreService.searchUsers(text) { found ->
                                results = found
                            }
                        } else {
                            results = emptyList()
                        }
                    },
                    placeholder = { Text("Kullanıcı adı girin") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        cursorColor          = MaterialTheme.colorScheme.primary,
                        focusedTextColor            = MaterialTheme.colorScheme.onBackground,
                        focusedPlaceholderColor     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results) { user ->
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserSelected(user.uid) }
                                .padding(vertical = 4.dp),
                            leadingContent = {
                                Image(
                                    painter = rememberAsyncImagePainter(user.profileImageUrl ?: ""),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )
                            },
                            headlineContent = {
                                Text(text = user.username, style = MaterialTheme.typography.bodyLarge)
                            },
                            supportingContent = {
                                user.bio?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

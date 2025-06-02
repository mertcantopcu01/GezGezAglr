package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.TextFieldStyles

@Composable
fun SearchScreen(onUserSelected: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    val context = LocalContext.current

    val tfColors = TextFieldStyles.defaultTextFieldColors(
    )

    // Degradeli arka plan renkleri (res/values/colors.xml içinde tanımlı olmalı)
    val gradientColors = listOf(
        colorResource(id = R.color.blue_900),
        colorResource(id = R.color.green_800)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .padding(16.dp)
    ) {
        Column {
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
                label = { Text(stringResource(R.string.search_user), color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = tfColors
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(results) { user ->
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = user.uid.isNotBlank()) {
                                onUserSelected(user.uid)
                            }
                            .padding(vertical = 4.dp),
                        headlineContent = {
                            Text(
                                text = user.username,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        supportingContent = {
                            user.bio?.let {
                                Text(it, color = Color.White.copy(alpha = 0.8f))
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
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                    Divider(color = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}

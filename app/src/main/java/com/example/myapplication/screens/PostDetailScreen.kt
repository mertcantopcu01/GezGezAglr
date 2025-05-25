package com.example.myapplication.screens

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator

import coil.compose.rememberAsyncImagePainter

import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.Post


@Composable
fun PostDetailScreen(postId: String) {
    var post by remember { mutableStateOf<Post?>(null) }
    val context = LocalContext.current

    LaunchedEffect(postId) {
        FirestoreService.getPostById(postId) { post = it }
    }

    post?.let {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(it.title, style = MaterialTheme.typography.headlineMedium)
            Text("Puan: ${it.rating}/10", style = MaterialTheme.typography.titleSmall)
            Text(it.description, style = MaterialTheme.typography.bodyLarge)
            it.photoUrl?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter(url),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

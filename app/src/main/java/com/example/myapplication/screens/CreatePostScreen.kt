package com.example.myapplication.screens


import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.FirebaseStorageService

@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5f) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }


    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            selectedImageUri = uri
        }
    )

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri
        }
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Yeni Post Oluştur", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Mekan İsmi") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Açıklama") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Text(text = "Puan: ${rating.toInt()}/10")
        Slider(
            value = rating,
            onValueChange = { rating = it },
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pickMediaLauncher.launch(
                    PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
                )
            } else {
                openDocumentLauncher.launch(arrayOf("image/*"))
            }
        }) {
            Text("Fotoğraf Seç")
        }
        selectedImageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        errorMsg?.let {
            Text(text = it, color = Color.Red)
        }

        Button(
            onClick = {
                if (title.isBlank() || description.isBlank()) {
                    errorMsg = "Başlık ve açıklama boş olamaz."
                    return@Button
                }
                isLoading = true
                if (selectedImageUri != null) {
                    FirebaseStorageService.uploadImage(context, selectedImageUri!!) { url ->
                        savePostAndReturn(title, description, rating.toInt(), url, onPostCreated) {
                            isLoading = false; errorMsg = it
                        }
                    }
                } else {
                    savePostAndReturn(title, description, rating.toInt(), null, onPostCreated) {
                        isLoading = false; errorMsg = it
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Paylaşılıyor..." else "Paylaş")
        }
    }
}

private fun savePostAndReturn(
    title: String,
    description: String,
    rating: Int,
    photoUrl: String?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    FirestoreService.createPost(
        title = title,
        description = description,
        rating = rating,
        photoUrl = photoUrl,
        location = null
    ) { ok ->
        if (ok) onSuccess() else onError("Post kaydedilemedi.")
    }
}

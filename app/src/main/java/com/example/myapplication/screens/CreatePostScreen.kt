package com.example.myapplication.screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.ui.TextFieldStyles

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
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> selectedImageUri = uri }
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    val tfColors = TextFieldStyles.defaultTextFieldColors()


    // Degradeli arka plan renkleri
    val gradientColors = listOf(
        colorResource(id = R.color.blue_900),
        colorResource(id = R.color.green_800)
    )
    val scrollState = rememberScrollState()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.create_post),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        text = stringResource(R.string.place_name),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = {
                    Text(
                        text = stringResource(R.string.description),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = tfColors
            )

            Text(
                text = "Puan: ${rating.toInt()}/10",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Slider(
                value = rating,
                onValueChange = { rating = it },
                valueRange = 0f..10f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White.copy(alpha = 0.8f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    } else {
                        openDocumentLauncher.launch(arrayOf("image/*"))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = stringResource(R.string.choose_photo))
            }

            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = stringResource(R.string.chosen_photo),
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
                                isLoading = false
                                errorMsg = it
                            }
                        }
                    } else {
                        savePostAndReturn(title, description, rating.toInt(), null, onPostCreated) {
                            isLoading = false
                            errorMsg = it
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Paylaş")
                }
            }
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

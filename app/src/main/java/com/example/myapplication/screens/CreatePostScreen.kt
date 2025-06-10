package com.example.myapplication.screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onBack: () -> Unit,
    onPostCreated: () -> Unit
) {
    AppTheme {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var rating by remember { mutableStateOf(5f) }
        var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        // Android 13+ için çoklu görsel seçici
        val pickMultipleMediaLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
        ) { uris ->
            selectedImageUris = uris
        }
        // Eski API'ler için çoklu doküman açma
        val openMultipleDocumentsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->
            selectedImageUris = uris
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector    = Icons.Filled.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    },
                    title = { Text(stringResource(R.string.create_post)) },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor              = MaterialTheme.colorScheme.primary,
                        titleContentColor           = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor  = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.place_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Text(text = "Puan: ${rating.toInt()}/10", style = MaterialTheme.typography.bodyLarge)

                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pickMultipleMediaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            openMultipleDocumentsLauncher.launch(arrayOf("image/*"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fotoğrafları Seç")
                }

                if (selectedImageUris.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedImageUris) { uri ->
                            Image(
                                painter            = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier           = Modifier
                                    .size(100.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                errorMsg?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        if (title.isBlank() || description.isBlank()) {
                            errorMsg = "Başlık ve açıklama boş olamaz."
                            return@Button
                        }
                        isLoading = true
                        // Seçilen tüm URI'ları storage'a yükleyip URL listesini elde et
                        val uploadedUrls = mutableListOf<String>()
                        fun uploadNext(index: Int) {
                            if (index >= selectedImageUris.size) {
                                // Tüm resimler yüklendi, şimdi Firestore'a kaydet
                                FirestoreService.createPost(
                                    title = title,
                                    description = description,
                                    rating = rating.toInt(),
                                    photoUrls = uploadedUrls, // varsaydığımız parametre
                                    location = null
                                ) { ok ->
                                    isLoading = false
                                    if (ok) onPostCreated() else errorMsg = "Post kaydedilemedi."
                                }
                                return
                            }
                            FirebaseStorageService.uploadImage(
                                context = context,
                                imageUri = selectedImageUris[index]
                            ) { url ->
                                if (url != null) {
                                    uploadedUrls.add(url)
                                }
                                uploadNext(index + 1)
                            }
                        }
                        if (selectedImageUris.isNotEmpty()) {
                            uploadNext(0)
                        } else {
                            // Fotoğraf yoksa tek URL yerine boş liste gönder
                            FirestoreService.createPost(
                                title = title,
                                description = description,
                                rating = rating.toInt(),
                                photoUrls = emptyList(),
                                location = null
                            ) { ok ->
                                isLoading = false
                                if (ok) onPostCreated() else errorMsg = "Post kaydedilemedi."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.share))
                    }
                }
            }
        }
    }
}

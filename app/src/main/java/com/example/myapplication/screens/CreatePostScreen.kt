package com.example.myapplication.screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.ui.AppBackground
import com.example.myapplication.ui.TextFieldStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(onPostCreated: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val tfColors = TextFieldStyles.defaultTextFieldColors()
    val scroll = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5f) }         // 0..10
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> selectedImageUri = uri }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.create_post),
                            color = androidx.compose.ui.graphics.Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(stringResource(R.string.place_name), fontFamily = FontFamily.Monospace) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = tfColors
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(stringResource(R.string.description), fontFamily = FontFamily.Monospace) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            colors = tfColors
                        )

                        Text("Puan: ${rating.toInt()}/10", fontFamily = FontFamily.Monospace, color = cs.onSurface)
                        Slider(
                            value = rating,
                            onValueChange = { rating = it },
                            valueRange = 0f..10f,
                            steps = 9
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        pickMedia.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } else {
                                        openDocument.launch(arrayOf("image/*"))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium
                            ) { Text(stringResource(R.string.choose_photo), fontFamily = FontFamily.Monospace) }

                            if (selectedImageUri != null) {
                                OutlinedButton(
                                    onClick = { selectedImageUri = null },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error)
                                ) { Text("Kaldır", fontFamily = FontFamily.Monospace, color = cs.error) }
                            }
                        }

                        selectedImageUri?.let { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = stringResource(R.string.chosen_photo),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                        }

                        // Hata
                        errorMsg?.let {
                            Surface(
                                color = cs.errorContainer,
                                contentColor = cs.onErrorContainer,
                                shape = MaterialTheme.shapes.medium
                            ) { Text(it, Modifier.padding(12.dp), fontFamily = FontFamily.Monospace) }
                        }

                        // Paylaş
                        Button(
                            onClick = {
                                if (title.isBlank() || description.isBlank()) {
                                    errorMsg = "Başlık ve açıklama boş olamaz."
                                    return@Button
                                }
                                errorMsg = null
                                isLoading = true

                                fun save(imageUrl: String?) {
                                    FirestoreService.createPost(
                                        title = title,
                                        description = description,
                                        rating = rating.toInt(),
                                        photoUrl = imageUrl,
                                        location = null
                                    ) { ok ->
                                        isLoading = false
                                        if (ok) onPostCreated() else errorMsg = "Post kaydedilemedi."
                                    }
                                }

                                selectedImageUri?.let { uri ->
                                    FirebaseStorageService.uploadImage(context, uri) { url ->
                                        save(url)
                                    }
                                } ?: save(null)
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                    color = cs.onPrimary
                                )
                            } else {
                                Text("Paylaş", fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

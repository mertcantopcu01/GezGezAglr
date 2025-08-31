package com.example.myapplication.screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.TextFieldStyles
import com.example.myapplication.ui.AppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val tfColors = TextFieldStyles.defaultTextFieldColors()
    val uid = AuthService.getCurrentUser()?.uid

    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // form state
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var removePhoto by remember { mutableStateOf(false) }

    // Görsel seçiciler
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) { selectedImageUri = uri; removePhoto = false } }
    )
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) { selectedImageUri = uri; removePhoto = false } }
    )

    // profili yükle
    LaunchedEffect(uid) {
        if (uid == null) {
            errorMsg = "Oturum bulunamadı."
            isLoading = false
        } else {
            FirestoreService.getUserProfile(uid) { p ->
                profile = p
                username = p?.username.orEmpty()
                bio = p?.bio.orEmpty()
                isLoading = false
            }
        }
    }

    fun saveWithImageUrl(imageUrl: String?) {
        FirestoreService.saveUserProfile(uid!!, username, bio.ifBlank { "" }, imageUrl, null)
        saving = false
        onSaved()
    }

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Profili Düzenle",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.onPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = cs.onPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (saving || uid == null) return@IconButton
                                saving = true
                                errorMsg = null

                                if (removePhoto) {
                                    saveWithImageUrl(null)
                                } else if (selectedImageUri != null) {
                                    FirebaseStorageService.uploadImage(context, selectedImageUri!!) { url ->
                                        if (url == null) {
                                            saving = false
                                            errorMsg = "Fotoğraf yüklenemedi."
                                        } else {
                                            saveWithImageUrl(url)
                                        }
                                    }
                                } else {
                                    saveWithImageUrl(profile?.profileImageUrl)
                                }
                            },
                            enabled = !saving && uid != null && username.isNotBlank()
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = "Kaydet", tint = cs.onPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = cs.onPrimary,
                        navigationIconContentColor = cs.onPrimary,
                        actionIconContentColor = cs.onPrimary
                    )
                )
            },
            containerColor = cs.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = cs.primary
                    )

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar önizleme
                            val showingUrl = when {
                                removePhoto -> null
                                selectedImageUri != null -> selectedImageUri.toString()
                                else -> profile?.profileImageUrl
                            }

                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(showingUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profil Fotoğrafı",
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(
                                        Modifier
                                            .size(110.dp)
                                            .clip(CircleShape)
                                            .background(cs.secondary.copy(alpha = 0.2f))
                                    )
                                },
                                error = {
                                    Box(
                                        Modifier
                                            .size(110.dp)
                                            .clip(CircleShape)
                                            .background(cs.secondary.copy(alpha = 0.2f))
                                    )
                                },
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            pickMediaLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        } else {
                                            openDocumentLauncher.launch(arrayOf("image/*"))
                                        }
                                    },
                                    shape = MaterialTheme.shapes.medium
                                ) { Text("Fotoğraf Değiştir", fontFamily = FontFamily.Monospace) }

                                OutlinedButton(
                                    onClick = {
                                        selectedImageUri = null
                                        removePhoto = true
                                    },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, tint = cs.error)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Kaldır", fontFamily = FontFamily.Monospace, color = cs.error)
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Kullanıcı adı", color = cs.onSurface.copy(alpha = 0.7f)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = tfColors
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Bio (isteğe bağlı)", color = cs.onSurface.copy(alpha = 0.7f)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                colors = tfColors
                            )

                            if (!errorMsg.isNullOrBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Surface(
                                    color = cs.errorContainer,
                                    contentColor = cs.onErrorContainer,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        text = errorMsg!!,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onCancel,
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium
                                ) { Text("İptal", fontFamily = FontFamily.Monospace) }

                                Button(
                                    onClick = {
                                        if (saving || uid == null) return@Button
                                        saving = true
                                        errorMsg = null
                                        if (removePhoto) {
                                            saveWithImageUrl(null)
                                        } else if (selectedImageUri != null) {
                                            FirebaseStorageService.uploadImage(context, selectedImageUri!!) { url ->
                                                if (url == null) {
                                                    saving = false
                                                    errorMsg = "Fotoğraf yüklenemedi."
                                                } else {
                                                    saveWithImageUrl(url)
                                                }
                                            }
                                        } else {
                                            saveWithImageUrl(profile?.profileImageUrl)
                                        }
                                    },
                                    enabled = !saving && username.isNotBlank(),
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = cs.primary,
                                        contentColor = cs.onPrimary,
                                        disabledContainerColor = cs.primary.copy(alpha = 0.4f),
                                        disabledContentColor = cs.onPrimary.copy(alpha = 0.7f)
                                    )
                                ) {
                                    if (saving) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(20.dp),
                                            color = cs.onPrimary
                                        )
                                    } else {
                                        Text("Kaydet", fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

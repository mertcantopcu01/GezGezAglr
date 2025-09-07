@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.UserProfile
import com.example.myapplication.ui.AppBackground
import com.example.myapplication.ui.TextFieldStyles

@Composable
fun EditProfileScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val tfColors = TextFieldStyles.defaultTextFieldColors()
    val uid = AuthService.getCurrentUser()?.uid

    // state
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var removePhoto by remember { mutableStateOf(false) }

    // görsel seçiciler (RegisterScreen ile aynı mantık)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) { selectedImageUri = uri; removePhoto = false } }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) { selectedImageUri = uri; removePhoto = false } }

    fun triggerPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            openDocumentLauncher.launch(arrayOf("image/*"))
        }
    }

    // profil yükle
    LaunchedEffect(uid) {
        if (uid == null) {
            errorMessage = "Oturum bulunamadı."
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

    // basit validasyon (RegisterScreen diline yakın)
    fun usernameError(t: String): String? {
        if (t.isBlank()) return "Kullanıcı adı boş olamaz."
        if (t.length < 3) return "En az 3 karakter olmalı."
        if (!t.matches(Regex("^[a-zA-Z0-9._]+$"))) return "Sadece harf, rakam, . ve _ kullanın."
        return null
    }
    val usernameErr = usernameError(username)
    val canSave = !saving && uid != null && usernameErr == null

    fun saveWithImageUrl(url: String?) {
        FirestoreService.saveUserProfile(uid!!, username.trim(), bio.ifBlank { "" }, url, null)
        saving = false
        onSaved()
    }

    fun onSave() {
        if (!canSave) return
        saving = true
        errorMessage = null

        when {
            removePhoto -> saveWithImageUrl(null)
            selectedImageUri != null -> {
                FirebaseStorageService.uploadImage(context, selectedImageUri!!) { uploaded ->
                    if (uploaded == null) {
                        saving = false
                        errorMessage = "Fotoğraf yüklenemedi."
                    } else {
                        saveWithImageUrl(uploaded)
                    }
                }
            }
            else -> saveWithImageUrl(profile?.profileImageUrl)
        }
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                // RegisterScreen ile aynı düzen: önce birincil buton, altında text button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Button(
                        onClick = { onSave() },
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary,
                            disabledContainerColor = cs.primary.copy(alpha = 0.4f),
                            disabledContentColor = cs.onPrimary.copy(alpha = 0.7f)
                        )
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                color = cs.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Kaydet", fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
                        }
                    }

                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("İptal", fontFamily = FontFamily.SansSerif)
                    }
                }
            }
        ) { padding ->
            if (isLoading) {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center), color = cs.primary)
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Üst başlık (RegisterScreen ile aynı tipografi/dil)
                Text(
                    text = "Profili Düzenle",
                    style = MaterialTheme.typography.titleLarge,
                    color = cs.onBackground,
                    fontFamily = FontFamily.Monospace
                )

                // Avatar alanı (RegisterScreen ile aynı kurgu)
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .border(2.dp, cs.primary.copy(alpha = 0.8f), CircleShape)
                        .clickable { triggerPickImage() },
                    contentAlignment = Alignment.Center
                ) {
                    val display = when {
                        selectedImageUri != null -> selectedImageUri
                        removePhoto -> null
                        else -> profile?.profileImageUrl?.let { Uri.parse(it) }
                    }
                    if (display != null) {
                        Image(
                            painter = rememberAsyncImagePainter(display),
                            contentDescription = "Profil Fotoğrafı",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.unknown_avatar),
                            contentDescription = "Varsayılan",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(cs.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Ekle", tint = cs.onPrimary)
                    }
                }
                Text(
                    "Profil fotoğrafını güncelle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onBackground,
                    fontFamily = FontFamily.SansSerif
                )
                TextButton(
                    onClick = {
                        selectedImageUri = null
                        removePhoto = true
                    }
                ) {
                    Text("Fotoğrafı kaldır", color = cs.error, fontFamily = FontFamily.SansSerif)
                }

                // Kullanıcı adı
                Column(Modifier.fillMaxWidth()) {
                    Text("Kullanıcı Adı", color = cs.onSurface.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        placeholder = { Text("Kullanıcı adınızı güncelleyin") },
                        singleLine = true,
                        isError = usernameErr != null,
                        supportingText = {
                            val msg = usernameErr ?: "Sadece harf, rakam, . ve _ kullanın (min 3)."
                            Text(
                                msg,
                                color = if (usernameErr != null) cs.error else cs.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors
                    )
                }

                // Biyografi
                Column(Modifier.fillMaxWidth()) {
                    Text("Biyografi", color = cs.onSurface.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it; errorMessage = null },
                        placeholder = { Text("Kendinizden bahsedin") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 120.dp),
                        colors = tfColors
                    )
                }

                // Hata kutusu
                errorMessage?.let {
                    Surface(
                        color = cs.errorContainer,
                        contentColor = cs.onErrorContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(it, modifier = Modifier.padding(12.dp), fontFamily = FontFamily.SansSerif)
                    }
                }

                Spacer(Modifier.height(8.dp)) // içerik ile bottomBar arasında nefes
            }
        }
    }
}

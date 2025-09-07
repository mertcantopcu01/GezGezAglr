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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.core.AppConstants
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.ui.AppBackground
import com.example.myapplication.ui.TextFieldStyles
import com.example.myapplication.ui.AppThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val tfColors = TextFieldStyles.defaultTextFieldColors()

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPw by remember { mutableStateOf(false) }
    var showPw2 by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Görsel seçiciler
    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) selectedImageUri = uri }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) selectedImageUri = uri }

    // Basit ilerleme göstergesi
    val filledCount =
        (if (email.isNotBlank()) 1 else 0) +
                (if (username.isNotBlank()) 1 else 0) +
                (if (password.length >= 6) 1 else 0) +
                (if (confirmPassword.isNotBlank()) 1 else 0)
    val progress = (filledCount / 4f).coerceIn(0f, 1f)
    val progressPercentText = "${(progress * 100).toInt()}%"

    AppBackground {
        Scaffold(
            // Top bar kaldırıldı
            containerColor = Color.Transparent,
            bottomBar = {
                // Alt çubuk: Buton her zaman görünür
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Button(
                        onClick = {
                            if (password != confirmPassword) {
                                errorMessage = "Şifreler eşleşmiyor."
                                return@Button
                            }
                            isLoading = true
                            AuthService.registerUser(email, password) { success, err ->
                                if (!success) {
                                    isLoading = false
                                    errorMessage = err ?: "Kayıt başarısız."
                                    return@registerUser
                                }
                                val uid = AuthService.getCurrentUser()?.uid
                                if (uid == null) {
                                    isLoading = false
                                    errorMessage = "Kullanıcı oluşturulamadı."
                                    return@registerUser
                                }
                                selectedImageUri?.let { uri ->
                                    FirebaseStorageService.uploadImage(context, uri) { imageUrl ->
                                        if (imageUrl == null) {
                                            isLoading = false
                                            errorMessage = "Resim yüklenemedi."
                                        } else {
                                            FirestoreService.saveUserProfile(uid, username, bio, imageUrl, password)
                                            val superUid = AppConstants.SUPER_UID
                                            if (uid != superUid) {
                                                FirestoreService.followUser(uid, superUid) { _, _ -> }
                                            }
                                            isLoading = false
                                            onRegisterSuccess()
                                        }
                                    }
                                } ?: run {
                                    FirestoreService.saveUserProfile(uid, username, bio, null, password)
                                    isLoading = false
                                    onRegisterSuccess()
                                }
                            }
                        },
                        enabled = !isLoading && email.isNotBlank() && username.isNotBlank() && password.length >= 6,
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
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = cs.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Kayıt Ol", fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
                        }
                    }

                    TextButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hesabın yok mu? Giriş yap", fontFamily = FontFamily.SansSerif)
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(), // klavye açılınca içerik yukarı çıksın
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Üst başlık (artık AppBar yok)
                Text(
                    text = "Hesap Oluştur",
                    style = MaterialTheme.typography.titleLarge,
                    color = cs.onBackground,
                    fontFamily = FontFamily.Monospace
                )

                // Profil fotoğrafı alanı
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .border(2.dp, cs.primary.copy(alpha = 0.8f), CircleShape)
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                pickMediaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } else {
                                openDocumentLauncher.launch(arrayOf("image/*"))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
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
                    "Profil Fotoğrafı Ekle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onBackground,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    "Veya daha sonra ekle",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onBackground.copy(alpha = 0.6f),
                    fontFamily = FontFamily.SansSerif
                )

                // E-posta
                Column(Modifier.fillMaxWidth()) {
                    Text("E-posta", color = cs.onSurface.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        placeholder = { Text("E-posta adresinizi girin") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors
                    )
                }

                // Kullanıcı adı
                Column(Modifier.fillMaxWidth()) {
                    Text("Kullanıcı Adı", color = cs.onSurface.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        placeholder = { Text("Bir kullanıcı adı seçin") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors
                    )
                }

                // Biyografi (yüksekliği çok büyütmeden)
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

                // Parola
                Column(Modifier.fillMaxWidth()) {
                    Text("Parola", color = cs.onSurface.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        placeholder = { Text("Güçlü bir parola oluşturun") },
                        singleLine = true,
                        visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPw = !showPw }) {
                                Icon(
                                    imageVector = if (showPw) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPw) "Şifreyi gizle" else "Şifreyi göster"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors
                    )
                }

                // Parolayı Doğrula
                Column(Modifier.fillMaxWidth()) {
                    Text("Parolayı Doğrula", color = cs.onSurface.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        placeholder = { Text("Parolanızı tekrar girin") },
                        singleLine = true,
                        visualTransformation = if (showPw2) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPw2 = !showPw2 }) {
                                Icon(
                                    imageVector = if (showPw2) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPw2) "Şifreyi gizle" else "Şifreyi göster"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
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
                        Text(it, modifier = Modifier.padding(12.dp))
                    }
                }

                // İlerleme
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(progressPercentText, color = cs.onBackground.copy(0.8f))
                }

                Spacer(Modifier.height(8.dp)) // içerik ile bottomBar arasında nefes payı
            }
        }
    }
}

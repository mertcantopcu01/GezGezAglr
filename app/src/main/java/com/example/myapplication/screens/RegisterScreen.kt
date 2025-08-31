package com.example.myapplication.screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.ui.TextFieldStyles
import com.example.myapplication.ui.AppBackground
import androidx.compose.ui.platform.LocalContext


@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPw by remember { mutableStateOf(false) }
    var showPw2 by remember { mutableStateOf(false) }


    val tfColors = TextFieldStyles.defaultTextFieldColors()
    val scroll = rememberScrollState()
    val context = LocalContext.current

    // Görsel seçiciler
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> selectedImageUri = uri }
    )
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    AppBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Üst başlık
            Text(
                text = "GezGezAglr",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp,
                    color = if (isDark) Color.White else Color(0xFF0A2742)
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
            )

            // Kart
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cs.surface // Light: beyaz, Dark: nötr gri
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scroll)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kaydol",
                        style = MaterialTheme.typography.headlineSmall,
                        color = cs.onSurface,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(Modifier.height(18.dp))

                    // Profil resmi
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, cs.primary.copy(alpha = 0.8f), CircleShape)
                            .clickable {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    pickMediaLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } else openDocumentLauncher.launch(arrayOf("image/*"))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.unknown_avatar),
                                contentDescription = "Default",
                                modifier = Modifier.size(60.dp).clip(CircleShape)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("E-posta", color = cs.onSurface.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("Kullanıcı adı", color = cs.onSurface.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it; errorMessage = null },
                        label = { Text("Bio", color = cs.onSurface.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        colors = tfColors
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Şifre", color = cs.onSurface.copy(alpha = 0.6f)) },
                        singleLine = true,
                        visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(
                                onClick = { showPw = !showPw },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) { Text(if (showPw) "Gizle" else "Göster") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = { Text("Şifre (tekrar)", color = cs.onSurface.copy(alpha = 0.6f)) },
                        singleLine = true,
                        visualTransformation = if (showPw2) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(
                                onClick = { showPw2 = !showPw2 },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) { Text(if (showPw2) "Gizle" else "Göster") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            color = cs.errorContainer,
                            contentColor = cs.onErrorContainer,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = errorMessage!!,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (password != confirmPassword) {
                                errorMessage = "Şifreler eşleşmiyor."
                                return@Button
                            }
                            isLoading = true
                            AuthService.registerUser(email, password) { success, error ->
                                if (!success) {
                                    isLoading = false
                                    errorMessage = error
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
                                            errorMessage = "Resim yüklenirken bir hata oluştu."
                                        } else {
                                            FirestoreService.saveUserProfile(uid, username, bio, imageUrl, password)
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
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
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
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text("Kaydol", fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = "Zaten hesabın var mı? Giriş yap",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    MaterialTheme { RegisterScreen(onRegisterSuccess = {}, onNavigateToLogin = {}) }
}

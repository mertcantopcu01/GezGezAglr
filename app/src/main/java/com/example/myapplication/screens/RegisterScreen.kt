// RegisterScreen.kt
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.ui.TextFieldStyles

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val tfColors = TextFieldStyles.defaultTextFieldColors()

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> selectedImageUri = uri }
    )
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundGradientColors = listOf(
        colorResource(id = R.color.blue_900),
        colorResource(id = R.color.green_800)
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = backgroundGradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Profil resmi seçimi için daire
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
                    .clickable {
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
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Selected Profile Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.unknown_avatar),
                        contentDescription = "Default Avatar",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        colorFilter = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        text = stringResource(R.string.email),
                        color = Color.Gray
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = tfColors
            )

            // Kullanıcı Adı
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = {
                    Text(
                        text = stringResource(R.string.user_id),
                        color = Color.Gray
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = tfColors
            )

            // Bio Alanı
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = {
                    Text(
                        text = stringResource(R.string.bio),
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 12.dp),
                colors = tfColors
            )

            // Şifre Alanı
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        text = stringResource(R.string.password),
                        color = Color.Gray
                    )
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = tfColors
            )

            // Şifre (Tekrar) Alanı
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = {
                    Text(
                        text = stringResource(R.string.password_again),
                        color = Color.Gray
                    )
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = tfColors
            )

            // Kayıt Ol Butonu
            Button(
                onClick = {
                    // Şifre eşleşme kontrolü
                    if (password != confirmPassword) {
                        errorMessage = context.getString(R.string.passwords_dont_match)
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
                        // Resim yükleme
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
                            // Resim yoksa sadece profili kaydet
                            FirestoreService.saveUserProfile(uid, username, bio, null, password)
                            isLoading = false
                            onRegisterSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.blue_800),
                    contentColor = colorResource(id = R.color.blue_800)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.register),
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            // Hata Mesajı
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // “Zaten hesabın var mı?” satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = stringResource(R.string.already_have_account_register),
                        color = colorResource(R.color.yellow),
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    MaterialTheme {
        RegisterScreen(
            onRegisterSuccess = {},
            onNavigateToLogin = {}
        )
    }
}

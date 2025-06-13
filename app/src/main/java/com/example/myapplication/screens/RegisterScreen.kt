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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.ui.TextFieldStyles
import com.example.myapplication.ui.theme.AppTheme

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val tfColors = TextFieldStyles.defaultTextFieldColors()
    val FiftyDp = 60.dp

    // Tema’dan gelen renk paleti:
    val colors = MaterialTheme.colorScheme

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Fotoğraf seçme launcher’ları
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> selectedImageUri = uri }
    )
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Tema’nın arka plan rengini kullanıyoruz (Dark modda siyaha yakın olacaktır)
            .background(color = colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Yatay padding biraz azaltıldı
            verticalArrangement = Arrangement.SpaceEvenly, // Alanları eşit aralıkla dağıt
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Profil resmi alanı
            Box(
                modifier = Modifier
                    .size(80.dp) // 100dp → 80dp yapıldı
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = colors.onBackground.copy(alpha = 0.4f),
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
                            .size(50.dp)
                            .clip(CircleShape),
                        colorFilter = null
                    )
                }
            }

            // 2. Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        text = stringResource(R.string.email),
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height( FiftyDp ) , // sabit yükseklik
                colors = tfColors,
                textStyle = LocalTextStyle.current.copy(color = colors.onSurface)
            )

            // 3. Kullanıcı Adı TextField
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = {
                    Text(
                        text = stringResource(R.string.user_id),
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height( FiftyDp ), // sabit yükseklik
                colors = tfColors,
                textStyle = LocalTextStyle.current.copy(color = colors.onSurface)
            )

            // 4. Bio TextField (tek satırlık olacak şekilde yüksekliği küçültüldü)
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = {
                    Text(
                        text = stringResource(R.string.bio),
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp), // 80dp → 60dp
                colors = tfColors,
                textStyle = LocalTextStyle.current.copy(color = colors.onSurface)
            )

            // 5. Şifre TextField
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        text = stringResource(R.string.password),
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height( FiftyDp ),
                colors = tfColors,
                textStyle = LocalTextStyle.current.copy(color = colors.onSurface)
            )

            // 6. Şifre (Tekrar) TextField
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = {
                    Text(
                        text = stringResource(R.string.password_again),
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height( FiftyDp ),
                colors = tfColors,
                textStyle = LocalTextStyle.current.copy(color = colors.onSurface)
            )

            // 7. Kayıt Ol Butonu
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
                                    FirestoreService.saveUserProfile(
                                        uid = uid,
                                        username = username,
                                        bio = bio,
                                        profileImageUrl = imageUrl,
                                        password = password
                                    ) { success ->
                                        isLoading = false
                                        if (success) {
                                            onRegisterSuccess()
                                        } else {
                                            errorMessage = "Profil kaydedilirken bir hata oluştu."
                                        }
                                    }
                                }
                            }
                        } ?: run {
                            // Resim yoksa sadece profili kaydet
                            FirestoreService.saveUserProfile(
                                uid = uid,
                                username = username,
                                bio = bio,
                                profileImageUrl = null,
                                password = password
                            ) { success ->
                                isLoading = false
                                if (success) {
                                    onRegisterSuccess()
                                } else {
                                    errorMessage = "Profil kaydedilirken bir hata oluştu."
                                }
                            }
                            isLoading = false
                            onRegisterSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height( FiftyDp ),
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = colors.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.register),
                        fontSize = 16.sp,
                        color = colors.onPrimary
                    )
                }
            }

            // 8. Hata Mesajı
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = colors.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 9. “Zaten hesabın var mı?” satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = stringResource(R.string.already_have_account_register),
                        color = colors.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                }
            }
        }
    }
}
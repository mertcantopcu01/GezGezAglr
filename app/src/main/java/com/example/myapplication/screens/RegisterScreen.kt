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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.myapplication.ui.AppTheme


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
    val context = LocalContext.current

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> selectedImageUri = uri }
    )
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    AppTheme {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Yükseklik küçükse compact moda geç
            val compact = maxHeight < 640.dp
            val cardTopPad = if (compact) 12.dp else 58.dp
            val avatarSize = if (compact) 64.dp else 82.dp
            val avatarIcon = if (compact) 54.dp else 56.dp
            val fieldSpacer = if (compact) 4.dp else 8.dp
            val bioHeight = if (compact) 80.dp else 120.dp
            val btnHeight = if (compact) 44.dp else 48.dp
            val cardHMargin = 3.dp

            // Kart: ekrana sığacak maksimum yükseklik
            val cardMaxHeight = maxHeight
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .heightIn(max = cardMaxHeight) // sığdır
                    .wrapContentHeight()
                    .padding(top = cardTopPad, start = cardHMargin, end = cardHMargin),
                colors = CardDefaults.cardColors(containerColor = cs.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = if (compact) 12.dp else 16.dp,
                            vertical = if (compact) 12.dp else 16.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.unknown_avatar),
                                contentDescription = "Default",
                                modifier = Modifier
                                    .size(avatarIcon)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Spacer(Modifier.height(fieldSpacer))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("E-posta", color = cs.onSurface.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors
                    )
                    Spacer(Modifier.height(fieldSpacer))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("Kullanıcı adı", color = cs.onSurface.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors
                    )
                    Spacer(Modifier.height(fieldSpacer))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it; errorMessage = null },
                        label = { Text("Bio", color = cs.onSurface.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(bioHeight),
                        colors = tfColors
                    )
                    Spacer(Modifier.height(fieldSpacer))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Şifre", color = cs.onSurface.copy(alpha = 0.6f)) },
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

                    Spacer(Modifier.height(fieldSpacer))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = { Text("Şifre (tekrar)", color = cs.onSurface.copy(alpha = 0.6f)) },
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

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(Modifier.height(fieldSpacer))
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
                                            FirestoreService.saveUserProfile(
                                                uid, username, bio, imageUrl, password
                                            )
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
                            .height(btnHeight),
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
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Kaydol", fontSize = if (compact) 14.sp else 15.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(Modifier.height(if (compact) 4.dp else 6.dp))

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

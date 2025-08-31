package com.example.myapplication.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.ui.TextFieldStyles
import com.example.myapplication.ui.AppBackground

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPw by remember { mutableStateOf(false) }

    val tfColors = TextFieldStyles.defaultTextFieldColors()

    AppBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Başlık
            Text(
                text = "GezGezAglr",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 28.sp,
                    color = if (isDark) Color.White else Color(0xFF0A2742)
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            )

            // Kart
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Giriş Yap",
                        style = MaterialTheme.typography.headlineSmall,
                        color = cs.onSurface,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(Modifier.height(18.dp))

                    // E-posta alanı
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "E-posta",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.SansSerif,
                            color = cs.onSurface.copy(alpha = 0.8f)
                        )
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

                    Spacer(Modifier.height(16.dp))

                    // Parola alanı
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Parola",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.SansSerif,
                            color = cs.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            placeholder = { Text("Parolanızı girin") },
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

                    // Hata mesajı
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
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Giriş butonu
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            AuthService.loginUser(email, password) { ok, err ->
                                isLoading = false
                                if (ok) onLoginSuccess() else errorMessage = err ?: "Giriş başarısız"
                            }
                        },
                        enabled = email.isNotBlank() && password.length >= 6 && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp),
                                color = cs.onPrimary
                            )
                        } else {
                            Text("Giriş Yap", fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(onClick = onNavigateToRegister) {
                        Text("Hesabın yok mu? Kaydol", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.SansSerif)
                    }
                }
            }
        }
    }
}

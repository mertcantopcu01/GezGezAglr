// LoginScreen.kt
package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.ui.TextFieldStyles
import com.example.myapplication.ui.theme.AppTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val colors = MaterialTheme.colorScheme

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val tfColors = TextFieldStyles.defaultTextFieldColors()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // E-posta Alanı
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
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Şifre Alanı
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
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Giriş Yap Butonu
            Button(
                onClick = {
                    isLoading = true
                    AuthService.loginUser(email, password) { success, error ->
                        isLoading = false
                        if (success) {
                            onLoginSuccess()
                        } else {
                            errorMessage = error
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
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
                        text = stringResource(R.string.login_in),
                        fontSize = 16.sp,
                        color = colors.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kayıt Ol TextButton
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = stringResource(R.string.no_account_get_register),
                    color = colors.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Hata Mesajı
            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = msg,
                    color = colors.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
fun PreviewLoginScreen_Light() {
    AppTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewLoginScreen_Dark() {
    AppTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
}

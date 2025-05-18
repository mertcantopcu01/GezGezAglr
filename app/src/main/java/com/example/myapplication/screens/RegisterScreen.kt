package com.example.myapplication.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.FirebaseStorageService
import androidx.compose.ui.res.stringResource


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


    val imagePickerLauncher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
        Log.d("RegisterScreen", "Seçilen Görsel URI: $uri")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.register), fontSize = 28.sp)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(context.getString(R.string.user_id)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text(context.getString(R.string.bio)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(R.string.password_again)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text(context.getString(R.string.choose_photo))
        }

        selectedImageUri?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
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

                    // Eğer foto seçilmişse önce Storage'a yükle, sonra Firestore kaydı
                    selectedImageUri?.let { uri ->
                        FirebaseStorageService.uploadImage(context, uri) { imageUrl ->
                            // imageUrl null ise hata mesajı göster
                            if (imageUrl == null) {
                                isLoading = false
                                errorMessage = "Resim yüklenirken bir hata oluştu."
                            } else {
                                FirestoreService.saveUserProfile(
                                    uid,
                                    username,
                                    bio,
                                    imageUrl,
                                    password
                                )
                                isLoading = false
                                onRegisterSuccess()
                            }
                        }
                    } ?: run {
                        FirestoreService.saveUserProfile(
                            uid,
                            username,
                            bio,
                            null,
                            password
                        )
                        isLoading = false
                        onRegisterSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) context.getString(R.string.loading) else context.getString(R.string.register))
        }

        TextButton(onClick = onNavigateToLogin) {
            Text(stringResource(R.string.already_have_account_register))
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = Color.Red)
        }
    }
}

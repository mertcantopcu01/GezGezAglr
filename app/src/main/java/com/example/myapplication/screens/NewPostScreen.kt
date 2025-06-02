package com.example.myapplication.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.ui.TextFieldStyles

@Composable
fun NewPostScreen(onPostSuccess: () -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("")) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val tfColors = TextFieldStyles.defaultTextFieldColors()


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    // Degradeli arka plan renkleri
    val gradientColors = listOf(
        colorResource(id = R.color.blue_900),
        colorResource(id = R.color.green_800)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = {
                    Text(
                        text = stringResource(R.string.what_are_you_thinking),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = {
                    Text(
                        text = stringResource(R.string.location),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = stringResource(R.string.choose_photo))
            }

            selectedImageUri?.let { uri ->
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = stringResource(R.string.choosen_photos),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val uid = AuthService.getCurrentUser()?.uid
                    if (uid == null) {
                        error = context.getString(R.string.there_is_no_account)
                        return@Button
                    }

                    isLoading = true
                    if (selectedImageUri != null) {
                        FirebaseStorageService.uploadImage(context, selectedImageUri!!) { imageUrl ->
                            FirestoreService.createPost(
                                description = text.text,
                                photoUrl = imageUrl,
                                location = location.text,
                                title = "", // Başlık kullanılmıyorsa boş string
                                rating = 0
                            ) { success ->
                                isLoading = false
                                if (success) onPostSuccess()
                                else error = context.getString(R.string.post_fail)
                            }
                        }
                    } else {
                        FirestoreService.createPost(
                            description = text.text,
                            photoUrl = null,
                            location = location.text,
                            title = "",
                            rating = 0
                        ) { success ->
                            isLoading = false
                            if (success) onPostSuccess()
                            else error = context.getString(R.string.post_fail)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isLoading) stringResource(R.string.loading) else stringResource(R.string.send)
                )
            }

            error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = Color.Red)
            }
        }
    }
}

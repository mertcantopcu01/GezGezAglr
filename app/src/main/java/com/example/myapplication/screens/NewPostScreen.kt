package com.example.myapplication.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.AuthService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.firebase.FirebaseStorageService

@Composable
fun NewPostScreen(onPostSuccess: () -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("")) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Yeni Gönderi", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(context.getString(R.string.what_are_you_thinking)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text(context.getString(R.string.location)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text(context.getString(R.string.choose_photo))
        }

        selectedImageUri?.let { uri ->
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = context.getString(R.string.choosen_photos),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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
                            title = null.toString(),
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
                        title = null.toString(),
                        rating = 0
                    ) { success ->
                        isLoading = false
                        if (success) onPostSuccess()
                        else error = context.getString(R.string.post_fail)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) context.getString(R.string.loading) else context.getString(R.string.send))
        }

        error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = Color.Red)
        }
    }
}

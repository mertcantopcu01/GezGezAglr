package com.example.myapplication.firebase

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import android.content.Context
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID

object FirebaseStorageService {

    private val storageRef = Firebase.storage.reference
    private val TAG = "FirebaseStorageService"

    suspend fun uploadImageSuspend(ctx: Context, uri: Uri): String? =
        suspendCancellableCoroutine { cont ->
            uploadImage(ctx, uri) { url -> cont.resume(url, onCancellation = null) }
        }

    fun uploadImage(
        context: Context,
        imageUri: Uri,
        onResult: (downloadUrl: String?) -> Unit
    ) {
        val fileRef = storageRef.child("profileImages/${UUID.randomUUID()}.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        onResult(uri.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "indirme hatası : ", e)
                        onResult(null)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "yükleme hatası : ", e)
                onResult(null)
            }
    }
}



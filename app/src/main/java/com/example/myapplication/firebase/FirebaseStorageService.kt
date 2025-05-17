package com.example.myapplication.firebase

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import android.content.Context

object FirebaseStorageService {

    fun uploadImage(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("profile_images/${System.currentTimeMillis()}.jpg")

        try {
            val stream = context.contentResolver.openInputStream(uri)
            val uploadTask = imageRef.putStream(stream!!)

            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onComplete(downloadUri.toString())
                }.addOnFailureListener {
                    onComplete(null)
                }
            }.addOnFailureListener {
                onComplete(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(null)
        }
    }
}


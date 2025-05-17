package com.example.myapplication.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser

object AuthService {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val message = when (val e = task.exception) {
                        is FirebaseAuthUserCollisionException -> "Bu email zaten kayıtlı"
                        is FirebaseAuthInvalidCredentialsException -> "Email formatı hatalı"
                        else -> e?.localizedMessage ?: "Kayıt başarısız"
                    }
                    onResult(false, message)
                }
            }
    }


    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val message = when (val e = task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Şifre yanlış"
                        is FirebaseAuthInvalidUserException -> "Bu email ile bir kullanıcı bulunamadı"
                        else -> e?.localizedMessage ?: "Giriş başarısız"
                    }
                    onResult(false, message)
                }
            }
    }


    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut() = auth.signOut()
}

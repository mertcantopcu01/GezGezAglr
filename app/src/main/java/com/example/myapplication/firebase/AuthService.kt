package com.example.myapplication.firebase

import com.example.myapplication.R
import com.example.myapplication.core.AppConstants
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
                        is FirebaseAuthUserCollisionException -> R.string.this_email_already_registered
                        is FirebaseAuthInvalidCredentialsException -> R.string.this_email_format_wrong
                        else -> e?.localizedMessage ?: R.string.register_fail
                    }
                    onResult(false, message.toString())
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
                        is FirebaseAuthInvalidCredentialsException -> R.string.wrong_password
                        is FirebaseAuthInvalidUserException -> R.string.there_is_no_register_mail
                        else -> e?.localizedMessage ?: R.string.login_fail
                    }
                    onResult(false, message.toString())
                }
            }
    }


    fun getCurrentUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid
    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser
    fun isSuperUser(): Boolean =
        getCurrentUserId() == AppConstants.SUPER_UID

    fun signOut() = auth.signOut()
}

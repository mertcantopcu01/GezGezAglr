package com.example.myapplication.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private val TAG = "FirestoreService"

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val bio: String? = null
)

data class Post(
    val postId: String = "",
    val uid: String = "",
    val text: String = "",
    val photoUrl: String? = null,
    val location: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun createUserProfile(uid: String, username: String, email: String, onComplete: (Boolean) -> Unit) {
        val user = UserProfile(uid = uid, username = username, email = email)
        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getUserProfile(uid: String, onResult: (UserProfile?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(UserProfile::class.java)
                onResult(user)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun saveUserProfile(
        uid: String,
        username: String,
        bio: String?,
        profileImageUrl: String?,
        password: String?
    ) {
        val userMap = mapOf(
            "username" to username,
            "bio" to bio,
            "profileImageUrl" to profileImageUrl,
            "password" to password
        )

        Firebase.firestore.collection("users").document(uid).set(userMap)
        Log.d(TAG, "saveUserProfile çağrıldı: $username, $bio")
    }

    fun postTweet(
        uid: String,
        text: String,
        photoUrl: String?,
        location: String?,
        onComplete: (Boolean) -> Unit
    ) {
        val data = hashMapOf(
            "uid" to uid,
            "text" to text,
            "photoUrl" to photoUrl,
            "location" to location,
            "timestamp" to System.currentTimeMillis()
        )

        Log.e(TAG, "postTweet: $uid")

        db.collection("posts")
            .add(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getUserPosts(uid: String, onResult: (List<Post>) -> Unit) {
        db.collection("posts")
            .whereEqualTo("uid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val posts = result.documents.mapNotNull { it.toObject(Post::class.java) }
                onResult(posts)
                Log.d(TAG, "getUserPosts: ${result.documents.size} adet döküman bulundu.")
            }
            .addOnFailureListener {
                Log.e(TAG, "getUserPosts: Firestore çekme hatası", it)
                onResult(emptyList())
            }
    }

    fun createPost(
        text: String,
        photoUrl: String?,
        location: String?,
        onComplete: (Boolean) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onComplete(false)
            return
        }

        val post = Post(
            postId = "", // Otomatik atanacak
            uid = currentUser.uid,
            text = text,
            photoUrl = photoUrl,
            location = location,
            timestamp = System.currentTimeMillis()
        )

        val postRef = FirebaseFirestore.getInstance()
            .collection("posts")
            .document()

        val postWithId = post.copy(postId = postRef.id)

        postRef.set(postWithId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun searchUsers(query: String, onResult: (List<UserProfile>) -> Unit) {
        Firebase.firestore.collection("users")
            .orderBy("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { doc ->
                    doc.toObject(UserProfile::class.java)
                        ?.copy(uid = doc.id)
                }
                onResult(users)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

}

package com.example.myapplication.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myapplication.core.AppConstants
import com.example.myapplication.firebase.FirebaseStorageService.uploadImage
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine

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
    val title: String = "",
    val description: String = "",
    val rating: Int = 0,
    val photos: List<String> = emptyList(),   // çoklu foto
    val photoUrl: String? = null,            // eski tekli alan (geri uyum)
    val location: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val authorName: String = "",
    val authorImageUrl: String? = null
)

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getUserProfile(uid: String, onResult: (UserProfile?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(UserProfile::class.java)
                onResult(user)
            }
            .addOnFailureListener { onResult(null) }
    }

    fun saveUserProfile(
        uid: String,
        username: String,
        bio: String?,
        profileImageUrl: String?,
        password: String? // not: plain password saklamak güvenli değil
    ) {
        val userMap = mapOf(
            "username" to username,
            "bio" to bio,
            "profileImageUrl" to profileImageUrl,
            "password" to password // ❗️istenirse kaldırılabilir
        )
        Firebase.firestore.collection("users").document(uid).set(userMap)
        Log.d(TAG, "saveUserProfile çağrıldı: $username, $bio")
    }

    fun getUserPosts(uid: String, onResult: (List<Post>) -> Unit) {
        db.collection("posts")
            .whereEqualTo("uid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val posts = result.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(postId = doc.id)
                }
                onResult(posts)
                Log.d(TAG, "getUserPosts: ${posts.size} adet post yüklendi.")
            }
            .addOnFailureListener {
                Log.e(TAG, "getUserPosts: Firestore çekme hatası", it)
                onResult(emptyList())
            }
    }

    fun createPost(
        title: String,
        description: String,
        rating: Int,
        photos: List<String>,
        location: String?,
        onComplete: (Boolean) -> Unit
    ) {
        val currentUser = auth.currentUser ?: run {
            onComplete(false); return
        }
        val first = photos.firstOrNull()
        val data = hashMapOf(
            "uid" to currentUser.uid,
            "title" to title,
            "description" to description,
            "rating" to rating,
            "photos" to photos,
            "photoUrl" to first, // geri uyumluluk: kapak olarak ilkini da yaz
            "location" to location,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("posts")
            .add(data)
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
                    doc.toObject(UserProfile::class.java)?.copy(uid = doc.id)
                }
                onResult(users)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun getPostById(postId: String, onResult: (Post?) -> Unit) {
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { doc ->
                val p = doc.toObject(Post::class.java)?.copy(postId = doc.id)
                onResult(p)
            }
            .addOnFailureListener { onResult(null) }
    }

    fun followUser(
        followerId: String,
        followingId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val docId = "${followerId}_$followingId"
        val data = mapOf(
            "followerId" to followerId,
            "followingId" to followingId,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("follows").document(docId)
            .set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun unfollowUser(
        followerId: String,
        followingId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (followingId == AppConstants.SUPER_UID) {
            onComplete(false, "Bu hesabı takipten çıkaramazsın.")
            return
        }

        val docId = "${followerId}_$followingId"
        db.collection("follows").document(docId)
            .delete()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun isFollowing(
        followerId: String,
        followingId: String,
        onResult: (Boolean) -> Unit
    ) {
        val docId = "${followerId}_$followingId"
        db.collection("follows").document(docId)
            .get()
            .addOnSuccessListener { snap -> onResult(snap.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    fun getFollowersCount(userId: String, onResult: (Int) -> Unit) {
        db.collection("follows")
            .whereEqualTo("followingId", userId)
            .get()
            .addOnSuccessListener { snap -> onResult(snap.size()) }
            .addOnFailureListener { onResult(0) }
    }

    fun getFollowingCount(userId: String, onResult: (Int) -> Unit) {
        db.collection("follows")
            .whereEqualTo("followerId", userId)
            .get()
            .addOnSuccessListener { snap -> onResult(snap.size()) }
            .addOnFailureListener { onResult(0) }
    }

    fun getFollowingIds(followerId: String, onResult: (List<String>) -> Unit) {
        db.collection("follows")
            .whereEqualTo("followerId", followerId)
            .get()
            .addOnSuccessListener { snap ->
                val ids = snap.documents.mapNotNull { it.getString("followingId") }
                onResult(ids)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun getPostsByUserIds(userIds: List<String>, onResult: (List<Post>) -> Unit) {
        if (userIds.isEmpty()) {
            onResult(emptyList()); return
        }
        db.collection("posts")
            .whereIn("uid", userIds.take(10))
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val posts = snap.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(postId = doc.id)
                }
                onResult(posts)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun getFollowersList(userId: String, onResult: (List<UserProfile>) -> Unit) {
        db.collection("follows")
            .whereEqualTo("followingId", userId)
            .get()
            .addOnSuccessListener { snaps ->
                val ids = snaps.documents.mapNotNull { it.getString("followerId") }
                if (ids.isEmpty()) { onResult(emptyList()); return@addOnSuccessListener }

                val chunks = ids.chunked(10)
                val tasks: List<Task<QuerySnapshot>> = chunks.map { chunk ->
                    db.collection("users")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                }

                Tasks.whenAllSuccess<QuerySnapshot>(tasks)
                    .addOnSuccessListener { results ->
                        val users = results.flatMap { it.documents }
                            .mapNotNull { it.toObject(UserProfile::class.java)?.copy(uid = it.id) }
                        onResult(users)
                    }
                    .addOnFailureListener { onResult(emptyList()) }
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun getFollowingList(userId: String, onResult: (List<UserProfile>) -> Unit) {
        db.collection("follows")
            .whereEqualTo("followerId", userId)
            .get()
            .addOnSuccessListener { snaps ->
                val ids = snaps.documents.mapNotNull { it.getString("followingId") }
                if (ids.isEmpty()) { onResult(emptyList()); return@addOnSuccessListener }

                val chunks = ids.chunked(10)
                val tasks: List<Task<QuerySnapshot>> = chunks.map { chunk ->
                    db.collection("users")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                }

                Tasks.whenAllSuccess<QuerySnapshot>(tasks)
                    .addOnSuccessListener { results ->
                        val users = results.flatMap { it.documents }
                            .mapNotNull { it.toObject(UserProfile::class.java)?.copy(uid = it.id) }
                        onResult(users)
                    }
                    .addOnFailureListener { onResult(emptyList()) }
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
    fun deletePostWithImages(post: Post, onComplete: (Boolean) -> Unit) {
        val urls = buildList {
            addAll(post.photos)
            post.photoUrl?.let { add(it) }
        }

        if (urls.isEmpty()) {
            deletePost(post.postId, onComplete)
            return
        }

        val storage = FirebaseStorage.getInstance()
        val deleteTasks = urls.mapNotNull { url ->
            try { storage.getReferenceFromUrl(url).delete() } catch (_: Exception) { null }
        }

        Tasks.whenAllComplete(deleteTasks)
            .addOnSuccessListener {
                deletePost(post.postId, onComplete)
            }
            .addOnFailureListener {
                // Görsel silmede hata olsa bile dokümanı yine de silmek istersen:
                deletePost(post.postId, onComplete)
            }
    }
    fun deletePost(postId: String, onComplete: (Boolean) -> Unit) {
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

}

package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    
    var isFirebaseAvailable: Boolean = false
        private set

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null

    private val _currentUserState = MutableStateFlow<FirebaseUser?>(null)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState

    fun initialize(context: Context) {
        try {
            // Intentionally check if FirebaseApp can be fetched/initialized
            val apps = FirebaseApp.getApps(context)
            val app = if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                apps[0]
            }

            if (app != null) {
                auth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                storage = FirebaseStorage.getInstance()
                isFirebaseAvailable = true
                _currentUserState.value = auth?.currentUser
                Log.d(TAG, "Firebase initialized successfully.")
            }
        } catch (e: Exception) {
            isFirebaseAvailable = false
            Log.e(TAG, "Firebase initialization failed. Falling back to local Storage. Match: ${e.message}")
        }
    }

    fun getCurrentUserId(): String {
        return auth?.currentUser?.uid ?: "local_user_id"
    }

    fun getCurrentUserEmail(): String {
        return auth?.currentUser?.email ?: "local@linkoptima.pro"
    }

    fun getCurrentUserName(): String {
        return auth?.currentUser?.displayName ?: "Professional User"
    }

    fun isUserSignedIn(): Boolean {
        return auth?.currentUser != null
    }

    // High fidelity email sign in with standard firebase auth
    fun firebaseAuthInstance(): FirebaseAuth? = auth
    fun firestoreInstance(): FirebaseFirestore? = firestore
    fun storageInstance(): FirebaseStorage? = storage
}

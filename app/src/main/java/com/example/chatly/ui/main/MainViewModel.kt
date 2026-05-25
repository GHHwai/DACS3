package com.example.chatly.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.User
import com.example.chatly.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val context = getApplication<Application>()

    private val gso = GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN
    )
        .requestIdToken(
            context.getString(R.string.default_web_client_id)
        )
        .requestEmail()
        .build()

    private val googleSignInClient: GoogleSignInClient =
        GoogleSignIn.getClient(context, gso)

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        val currentUserUid = auth.currentUser?.uid ?: return

        viewModelScope.launch {

            _isLoading.value = true

            try {

                val snapshot = firestore
                    .collection("users")
                    .get()
                    .await()

                val userList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }.filter {
                    it.uid != currentUserUid
                }

                _users.value = userList

            } catch (e: Exception) {

                e.printStackTrace()

            } finally {

                _isLoading.value = false
            }
        }
    }

    fun logout() {

        auth.signOut()

        // logout google
        googleSignInClient.revokeAccess()
    }
}
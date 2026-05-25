package com.example.chatly.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FirebaseFirestore
import com.example.chatly.data.model.User
import com.google.firebase.auth.GoogleAuthProvider

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow(auth.currentUser != null)
    val authState: StateFlow<Boolean> = _authState

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole


    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { doc ->
                                val role = doc.getString("role") ?: "user"
                                _userRole.value = role
                                _authState.value = true
                            }
                            .addOnFailureListener {
                                _userRole.value = "user"
                                _authState.value = true
                            }
                    } else {
                        _userRole.value = "user"
                        _authState.value = true
                    }
                } else {
                    _error.value = task.exception?.message
                }
            }
    }
    fun login(email: String, password: String) {
        Log.d("AuthViewModel", "Attempt login with $email")
        _error.value = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Login success")
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { doc ->
                                val role = doc.getString("role") ?: "user"
                                _userRole.value = role
                                _authState.value = true
                            }
                            .addOnFailureListener {
                                _userRole.value = "user"
                                _authState.value = true
                            }
                    } else {
                        _userRole.value = "user"
                        _authState.value = true
                    }
                } else {
                    Log.e("AuthViewModel", "Login failure", task.exception)
                    _error.value = task.exception?.localizedMessage ?: "Login failed"
                }
            }
    }

    fun register(displayName: String, mobile: String, email: String, password: String) {
        _error.value = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val userObj = User(
                        uid = user?.uid ?: "",
                        email = user?.email ?: "",
                        displayName = displayName,
                        mobile = mobile,
                        photoUrl = user?.photoUrl.toString()

                    )
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userObj.uid)
                        .set(userObj)
                        .addOnSuccessListener {
                            _userRole.value = "user"
                            _authState.value = true
                        }
                        .addOnFailureListener { e ->
                            _error.value = "Failed to save user: ${e.localizedMessage}"
                            _userRole.value = "user"
                            _authState.value = true // Even if failed to save to firestore, auth succeeded
                        }
                } else {
                    _error.value = task.exception?.localizedMessage ?: "Registration failed"
                }
            }
    }

    fun logout() {
        auth.signOut()
        _userRole.value = null
        _authState.value = false
    }


}

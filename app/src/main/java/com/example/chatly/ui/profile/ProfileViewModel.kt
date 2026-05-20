package com.example.chatly.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.example.chatly.data.local.AppDatabase
import com.example.chatly.data.model.User
import com.example.chatly.data.repository.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val mobile: String = "",
    val photoUrl: String? = null,
    val dob: String = "",
    val gender: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaved: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repo = ProfileRepository(db.userDao())
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getUserFlow().collect { user ->
                _uiState.update { state ->
                    user?.let {
                        state.copy(
                            displayName = it.displayName ?: "",
                            email = it.email ?: "",
                            mobile = it.mobile ?: "",
                            photoUrl = it.photoUrl,
                            dob = it.dob ?: "",
                            gender = it.gender ?: "",
                            isLoading = false
                        )
                    } ?: state.copy(isLoading = false)
                }
            }
        }
    }

    fun onDisplayNameChange(value: String) {
        _uiState.update { it.copy(displayName = value) }
    }

    fun onMobileChange(value: String) {
        _uiState.update { it.copy(mobile = value) }
    }

    fun onDobChange(value: String) {
        _uiState.update { it.copy(dob = value) }
    }

    fun onGenderChange(value: String) {
        _uiState.update { it.copy(gender = value) }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(profileImageUri = uri) }
    }

    fun setPhotoUrl(url: String) {
        _uiState.update { it.copy(photoUrl = url) }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            repo.fetchUserFromRemoteAndCache()
        }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val updatedUser = User(
            uid = repo.getCurrentUserId() ?: "",
            displayName = currentState.displayName,
            email = currentState.email,
            mobile = currentState.mobile,
            photoUrl = currentState.photoUrl,
            dob = currentState.dob,
            gender = currentState.gender
        )
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                repo.updateUser(updatedUser)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Unknown error") }
            }
        }
    }
    
    fun resetSaveStatus() {
        _uiState.update { it.copy(isSaved = false) }
    }
}

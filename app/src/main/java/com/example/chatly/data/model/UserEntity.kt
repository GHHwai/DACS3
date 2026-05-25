package com.example.chatly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val displayName: String?,
    val mobile: String,
    val photoUrl: String?,
    val dob: String,
    val gender: String,
    val role: String = "user",
    val status: String = "active"
) {
    fun toUser(): User = User(uid, email, displayName, mobile, photoUrl, dob, gender, role, status)
    companion object {
        fun fromUser(user: User) = UserEntity(
            user.uid, user.email, user.displayName, user.mobile, user.photoUrl, user.dob, user.gender, user.role, user.status
        )
    }
}

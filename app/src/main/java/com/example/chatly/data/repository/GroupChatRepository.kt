package com.example.chatly.data.repository

import com.example.chatly.data.model.Group
import com.example.chatly.data.model.GroupMessage
import com.example.chatly.data.model.User // Import trực tiếp để code gọn gàng hơn
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GroupChatRepository {

    // Đưa khai báo db lên đầu class để tất cả hàm bên dưới đều đọc được an toàn
    private val db = FirebaseFirestore.getInstance()

    // Lấy danh sách tất cả người dùng
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users").addSnapshotListener { value, _ ->
            if (value != null) {
                val users = value.toObjects(User::class.java)
                trySend(users)
            }
        }
        awaitClose { listener.remove() }
    }

    // Hàm dùng chung cho cả việc: Thêm thành viên mới HOẶC tự xóa mình khỏi nhóm (Rời nhóm)
    fun updateGroupMembers(
        groupId: String,
        newMembers: List<String>,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("groups").document(groupId)
            .update("members", newMembers)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    // Tạo nhóm
    fun createGroup(group: Group) {
        val doc = db.collection("groups").document()
        val newGroup = group.copy(id = doc.id)
        doc.set(newGroup)
    }

    // Lấy danh sách nhóm
    fun getGroups(): Flow<List<Group>> = callbackFlow {
        val listener = db.collection("groups").addSnapshotListener { value, _ ->
            if (value != null) {
                val groups = value.toObjects(Group::class.java)
                trySend(groups)
            }
        }
        awaitClose { listener.remove() }
    }

    // Gửi tin nhắn nhóm
    fun sendMessage(message: GroupMessage) {
        val doc = db.collection("group_messages").document()
        val newMessage = message.copy(id = doc.id)
        doc.set(newMessage)
    }

    // Lấy tin nhắn realtime
    fun getMessages(groupId: String): Flow<List<GroupMessage>> = callbackFlow {
        val listener = db.collection("group_messages")
            .whereEqualTo("groupId", groupId)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    val messages = value.toObjects(GroupMessage::class.java)
                    trySend(messages)
                }
            }
        awaitClose { listener.remove() }
    }

    // Xóa nhóm (Đã sửa lỗi gõ nhầm addOn Birgitte thành addOnCompleteListener chuẩn)
    fun deleteGroup(groupId: String, onComplete: (Boolean) -> Unit) {
        // 1. Xóa tài liệu nhóm trong collection "groups"
        db.collection("groups").document(groupId)
            .delete()
            .addOnCompleteListener { task -> // <-- ĐÃ SỬA CHỖ NÀY
                if (task.isSuccessful) {
                    // 2. Xóa toàn bộ tin nhắn thuộc nhóm này trong "group_messages" để sạch DB
                    db.collection("group_messages")
                        .whereEqualTo("groupId", groupId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val batch = db.batch()
                            for (doc in snapshot.documents) {
                                batch.delete(doc.reference)
                            }
                            batch.commit()
                            onComplete(true)
                        }
                        .addOnFailureListener { onComplete(true) } // Vẫn tính là thành công vì nhóm chính đã xóa xong
                } else {
                    onComplete(false)
                }
            }
    }
}
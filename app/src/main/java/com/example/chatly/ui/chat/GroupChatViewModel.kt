package com.example.chatly.ui.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.Group
import com.example.chatly.data.model.GroupMessage
import com.example.chatly.data.repository.GroupChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.* // DÙNG DẤU CHẤM SAO ĐỂ IMPORT TẤT CẢ TÍNH NĂNG FLOW
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream

class GroupChatViewModel(
    private val repository: GroupChatRepository
) : ViewModel() {

    // Danh sách tất cả người dùng trên hệ thống để phục vụ việc chọn/mời vào nhóm
    private val _allUsers = MutableStateFlow<List<com.example.chatly.data.model.User>>(emptyList())
    val allUsers: StateFlow<List<com.example.chatly.data.model.User>> = _allUsers

    // Danh sách nhóm chat mà user tham gia
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    // Luồng tin nhắn trong phòng chat hiện tại
    private val _messages = MutableStateFlow<List<GroupMessage>>(emptyList())
    val messages: StateFlow<List<GroupMessage>> = _messages

    // --- ĐÃ THÊM BIẾN LƯU DANH SÁCH THÀNH VIÊN ---
    var groupMembersInfo = mutableStateListOf<com.example.chatly.data.model.User>()
        private set
    // --------------------------------------------

    // Tải danh sách user
    fun loadAllUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                _allUsers.value = users
            }
        }
    }

    // Thêm thành viên hoặc rời nhóm
    fun updateMembers(groupId: String, newMembers: List<String>, onComplete: (Boolean) -> Unit = {}) {
        repository.updateGroupMembers(groupId, newMembers, onComplete)
    }

    // Tạo một nhóm chat mới
    fun createGroup(group: Group) {
        repository.createGroup(group)
    }

    // Tải danh sách nhóm chat
    fun loadGroups() {
        viewModelScope.launch {
            repository.getGroups().collect { groupList ->
                _groups.value = groupList
            }
        }
    }

    // Tải tin nhắn của một nhóm cụ thể
    fun loadMessages(groupId: String) {
        viewModelScope.launch {
            repository.getMessages(groupId).collect { messageList ->
                // Sắp xếp tin nhắn theo ID (thời gian) từ cũ đến mới
                val sortedMessages = messageList.sortedBy { it.id.toLongOrNull() ?: 0L }
                _messages.value = sortedMessages
            }
        }
    }

    // Gửi tin nhắn mới
    fun sendMessage(message: GroupMessage) {
        repository.sendMessage(message)
    }

    fun deleteGroup(groupId: String, onComplete: (Boolean) -> Unit = {}) {
        repository.deleteGroup(groupId, onComplete)
    }

    // --- ĐÃ THÊM HÀM LẤY CHI TIẾT THÀNH VIÊN TỪ FIREBASE ---
    fun getGroupMembersDetail(memberIds: List<String>) {
        groupMembersInfo.clear()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        if (memberIds.isEmpty()) return

        db.collection("users")
            .whereIn("uid", memberIds)
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.toObjects(com.example.chatly.data.model.User::class.java)
                groupMembersInfo.addAll(users)
            }
    }

    // --- ĐÃ FIX: CHUYỂN TỪ FIREBASE STORAGE SANG HÀM GỌI API UPLOAD MIỄN PHÍ ---
    fun uploadFileToFreeService(
        context: android.content.Context,
        groupId: String,
        senderName: String,
        fileUri: android.net.Uri,
        isImage: Boolean
    ) {
        // --- BƯỚC KHÓA VỊ TRÍ: Sinh ID ngay lập tức tại thời điểm bấm gửi ---
        // Việc này giúp Firebase và ViewModel xếp sẵn "chỗ ngồi" cho tin nhắn ảnh ở dưới đáy phòng chat
        val messageId = System.currentTimeMillis().toString()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(fileUri)
                val bytes = inputStream?.readBytes() ?: return@launch
                inputStream.close()

                val client = OkHttpClient()

                if (isImage) {
                    val apiKey = "49b335a5a3fe8f716f6475d9711b438d" // Key ImgBB của bạn

                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "image.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
                        .build()

                    val request = Request.Builder()
                        .url("https://api.imgbb.com/1/upload?key=$apiKey")
                        .post(requestBody)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val jsonResponse = JSONObject(response.body?.string() ?: "")
                            val downloadUrl = jsonResponse.getJSONObject("data").getString("url")

                            // Sử dụng lại đúng cái messageId đã sinh ra ở trên đầu hàm
                            val message = GroupMessage(
                                id = messageId, // <--- Dùng cái này để giữ đúng thứ tự dưới đáy
                                groupId = groupId,
                                senderName = senderName,
                                content = "[Hình ảnh]",
                                imageUrl = downloadUrl,
                                fileUrl = null
                            )
                            sendMessage(message)
                        }
                    }
                } else {
                    // Xử lý file tài liệu tương tự
                    val fileName = fileUri.lastPathSegment ?: "file_document"
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", fileName, bytes.toRequestBody("application/octet-stream".toMediaType()))
                        .build()

                    val request = Request.Builder().url("https://file.io").post(requestBody).build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val jsonResponse = JSONObject(response.body?.string() ?: "")
                            val downloadUrl = jsonResponse.getString("link")

                            val message = GroupMessage(
                                id = messageId, // <--- Giữ đúng thứ tự dưới đáy
                                groupId = groupId,
                                senderName = senderName,
                                content = "[Tập tin: $fileName]",
                                imageUrl = null,
                                fileUrl = downloadUrl
                            )
                            sendMessage(message)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    // -----------------------------------------------------

    // Factory để hỗ trợ khởi tạo ViewModel có tham số truyền vào (Luôn để dưới cùng file)
    class Factory(private val repository: GroupChatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GroupChatViewModel::class.java)) {
                return GroupChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
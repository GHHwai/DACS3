package com.example.chatly.data.repository

import android.util.Log
import com.example.chatly.data.model.QuizSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class QuizRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun createSession(session: QuizSession) {
        firestore
            .collection("quiz_sessions")
            .document(session.id)
            .set(session)
    }

    fun updateSession(sessionId: String, data: Map<String, Any>) {
        firestore
            .collection("quiz_sessions")
            .document(sessionId)
            .update(data)
    }

    fun getSessionHistory(userId: String, callback: (List<QuizSession>) -> Unit) {
        firestore
            .collection("quiz_sessions")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->

                val list = snap.documents.mapNotNull { doc ->

                    val data = doc.data ?: return@mapNotNull null

                    val isFinished = data["isFinished"] as? Boolean
                        ?: data["finished"] as? Boolean
                        ?: false

                    val session = QuizSession(
                        id = doc.id,
                        userId = data["userId"] as? String ?: "",
                        topic = data["topic"] as? String ?: "",
                        totalQuestions = (data["totalQuestions"] as? Long)?.toInt() ?: 0,
                        correctCount = (data["correctCount"] as? Long)?.toInt() ?: 0,
                        questions = emptyList(), // optional load later
                        userAnswers = (data["userAnswers"] as? List<String>) ?: emptyList(),
                        currentIndex = (data["currentIndex"] as? Long)?.toInt() ?: 0,
                        isFinished = isFinished,
                        createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis()
                    )

                    Log.d(
                        "QUIZ_HISTORY",
                        "topic=${session.topic}, isFinished=${session.isFinished}"
                    )

                    session
                }

                callback(list)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}

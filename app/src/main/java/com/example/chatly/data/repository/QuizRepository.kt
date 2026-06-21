package com.example.chatly.data.repository

import com.example.chatly.data.model.QuizSession
import com.google.firebase.firestore.FirebaseFirestore

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

    fun getSession(sessionId: String, callback: (QuizSession?) -> Unit) {
        firestore
            .collection("quiz_sessions")
            .document(sessionId)
            .get()
            .addOnSuccessListener {
                callback(it.toObject(QuizSession::class.java))
            }
    }
}
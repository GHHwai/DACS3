package com.example.chatly.data.repository

import android.util.Log
import com.example.chatly.data.model.ExamSchedule
import com.example.chatly.data.model.StudySchedule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
class ScheduleRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String {
        return auth.currentUser?.uid ?: throw Exception("User not logged in")
    }

    /*
    =========================
    STUDY SCHEDULE
    =========================
    */

    fun getStudySchedules(): Flow<List<StudySchedule>> = callbackFlow {

        val uid = getUserId()

        val listener = firestore
            .collection("users")
            .document(uid)
            .collection("study_schedules")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val schedules = snapshot?.documents?.mapNotNull { document ->

                    document.toObject(StudySchedule::class.java)

                } ?: emptyList()

                trySend(schedules)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun addStudySchedule(schedule: StudySchedule) {
        Log.d("FIREBASE_TEST", "START SAVE")

        val uid = getUserId()

        val documentId =
            if (schedule.id.isBlank()) UUID.randomUUID().toString()
            else schedule.id

        val newSchedule = schedule.copy(id = documentId)

        firestore
            .collection("users")
            .document(uid)
            .collection("study_schedules")
            .document(documentId)
            .set(newSchedule)
            .await()
        Log.d("FIREBASE_TEST", "SAVE SUCCESS")

    }

    suspend fun updateStudySchedule(schedule: StudySchedule) {

        val uid = getUserId()

        firestore
            .collection("users")
            .document(uid)
            .collection("study_schedules")
            .document(schedule.id)
            .set(schedule)
            .await()
    }

    suspend fun deleteStudySchedule(scheduleId: String) {

        val uid = getUserId()

        firestore
            .collection("users")
            .document(uid)
            .collection("study_schedules")
            .document(scheduleId)
            .delete()
            .await()
    }

    /*
    =========================
    EXAM SCHEDULE
    =========================
    */

    fun getExamSchedules(): Flow<List<ExamSchedule>> = callbackFlow {

        val uid = getUserId()

        val listener = firestore
            .collection("users")
            .document(uid)
            .collection("exam_schedules")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val exams = snapshot?.documents?.mapNotNull { document ->

                    document.toObject(ExamSchedule::class.java)

                } ?: emptyList()

                trySend(exams)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun addExamSchedule(exam: ExamSchedule) {

        val uid = getUserId()

        val documentId =
            if (exam.id.isBlank()) UUID.randomUUID().toString()
            else exam.id

        val newExam = exam.copy(id = documentId)

        firestore
            .collection("users")
            .document(uid)
            .collection("exam_schedules")
            .document(documentId)
            .set(newExam)
            .await()
    }

    suspend fun updateExamSchedule(exam: ExamSchedule) {

        val uid = getUserId()

        firestore
            .collection("users")
            .document(uid)
            .collection("exam_schedules")
            .document(exam.id)
            .set(exam)
            .await()
    }

    suspend fun deleteExamSchedule(examId: String) {

        val uid = getUserId()

        firestore
            .collection("users")
            .document(uid)
            .collection("exam_schedules")
            .document(examId)
            .delete()
            .await()
    }
}
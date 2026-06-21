package com.example.chatly.data.model

data class QuizQuestion(
    val question: String = "",
    val options: Map<String, String> = emptyMap(), // "A" -> "option text"
    val correctAnswer: String = "",                // "A", "B", "C", or "D"
    val explanation: String = ""
)

data class QuizSession(
    val id: String = "",
    val userId: String = "",
    val topic: String = "",
    val totalQuestions: Int = 0,
    val correctCount: Int = 0,
    val questions: List<QuizQuestion> = emptyList(),
    val userAnswers: List<String> = emptyList(),   // list of "A"/"B"/"C"/"D" per question
    val currentIndex: Int = 0,
    val isFinished: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// UI state for the quiz flow inside AiChatScreen
data class QuizUiState(
    val isQuizMode: Boolean = false,
    val isTopicSelectionStep: Boolean = false,       // showing topic input dialog
    val topicInput: String = "",
    val scheduleSubjects: List<String> = emptyList(),// pulled from user's study schedules
    val currentQuestion: QuizQuestion? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 5,
    val selectedAnswer: String? = null,              // the answer the user just tapped
    val isAnswerRevealed: Boolean = false,           // show correct/wrong feedback
    val score: Int = 0,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

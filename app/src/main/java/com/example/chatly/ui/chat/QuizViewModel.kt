package com.example.chatly.ui.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.QuizQuestion
import com.example.chatly.data.model.QuizSession
import com.example.chatly.data.model.QuizUiState
import com.example.chatly.data.repository.FirebaseAiChatRepository
import com.example.chatly.data.repository.QuizRepository
import com.example.chatly.data.repository.ScheduleRepository
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class QuizViewModel(
    private val quizRepository: QuizRepository = QuizRepository(),
    private val scheduleRepository: ScheduleRepository = ScheduleRepository(),
    private val aiRepository: FirebaseAiChatRepository =
        FirebaseAiChatRepository()
) : ViewModel() {
    private var currentSession: QuizSession? = null
    private var sessionId: String? = null
    private val _sessionState = MutableStateFlow<QuizSession?>(null)
    val sessionState: StateFlow<QuizSession?> = _sessionState.asStateFlow()
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Buffer of pre-fetched questions so the next one loads instantly
    private val questionBuffer = mutableListOf<QuizQuestion>()

    // ─── Entry points ────────────────────────────────────────────────────────

    /** Open the topic-selection dialog */
    fun startQuizSetup() {

        _uiState.update {
            it.copy(
                isQuizMode = true,
                isTopicSelectionStep = true
            )
        }

        viewModelScope.launch {

            try {

                val schedules =
                    scheduleRepository
                        .getStudySchedules()
                        .first()

                val subjects =
                    schedules
                        .mapNotNull {
                            it.subject.takeIf(String::isNotBlank)
                        }
                        .distinct()

                _uiState.update {
                    it.copy(
                        scheduleSubjects = subjects
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "QuizViewModel",
                    "Load schedules failed",
                    e
                )
            }
        }
    }
    fun onTopicInputChange(value: String) {
        _uiState.update { it.copy(topicInput = value) }
    }

    fun dismissTopicSelection() {
        _uiState.update { it.copy(isTopicSelectionStep = false, topicInput = "") }
    }

    /** Called when the user confirms a topic (typed or from schedule) */
    fun beginQuiz(topic: String, totalQuestions: Int = 5) {
        questionBuffer.clear()
        sessionId = java.util.UUID.randomUUID().toString()
        currentSession = QuizSession(
            id = sessionId!!,
            userId = FirebaseAuth.getInstance().uid ?: "",
            topic = topic,
            totalQuestions = totalQuestions,
            questions = emptyList(),
            userAnswers = emptyList()
        )
        _sessionState.value = currentSession
        viewModelScope.launch {
            quizRepository.createSession(currentSession!!)
        }
        _uiState.update {
            QuizUiState(
                isQuizMode = true,
                isTopicSelectionStep = false,
                topicInput = topic,
                totalQuestions = totalQuestions,
                isLoading = true,
                error = null
            )
        }
        fetchNextQuestion(topic, questionIndex = 0, totalQuestions = totalQuestions)
    }

    // ─── Answer handling ─────────────────────────────────────────────────────

    fun selectAnswer(answer: String) {
        val state = _uiState.value
        if (state.isAnswerRevealed || state.currentQuestion == null) return

        val isCorrect = answer == state.currentQuestion.correctAnswer
        val updatedSession = currentSession?.copy(
            correctCount =
                (currentSession?.correctCount ?: 0) + if (isCorrect) 1 else 0,

            userAnswers =
                currentSession?.userAnswers?.plus(answer) ?: listOf(answer)
        )
        currentSession = updatedSession
        _sessionState.value = currentSession
        updatedSession?.let {
            quizRepository.updateSession(
                sessionId!!,
                mapOf(
                    "correctCount" to it.correctCount,
                    "userAnswers" to it.userAnswers
                )
            )
        }
        _uiState.update {
            it.copy(
                selectedAnswer = answer,
                isAnswerRevealed = true,
                score = if (isCorrect) it.score + 1 else it.score
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun nextQuestion() {
        val state = _uiState.value
        val nextIndex = state.currentQuestionIndex + 1

        currentSession = currentSession?.copy(
            currentIndex = nextIndex
        )

        quizRepository.updateSession(
            sessionId!!,
            mapOf(
                "currentIndex" to nextIndex
            )
        )
        if (nextIndex >= state.totalQuestions) {
            sessionId?.let {
                quizRepository.updateSession(
                    sessionId!!,
                    mapOf(
                        "currentIndex" to nextIndex
                    )
                )
            }
            _uiState.update { it.copy(isFinished = true, isAnswerRevealed = false) }
            return
        }

        if (questionBuffer.isNotEmpty()) {
            val next = questionBuffer.removeFirst()
            _uiState.update {
                it.copy(
                    currentQuestion = next,
                    currentQuestionIndex = nextIndex,
                    selectedAnswer = null,
                    isAnswerRevealed = false,
                    isLoading = false
                )
            }
            // Pre-fetch the one after this
            if (nextIndex + 1 < state.totalQuestions) {
                fetchNextQuestion(state.topicInput, nextIndex + 1, state.totalQuestions, bufferOnly = true)
            }
        } else {
            _uiState.update { it.copy(isLoading = true) }
            fetchNextQuestion(state.topicInput, nextIndex, state.totalQuestions)
        }
    }

    fun resetQuiz() {
        questionBuffer.clear()
        currentSession = null
        sessionId = null
        _uiState.update { QuizUiState() }
    }

    // ─── AI generation ───────────────────────────────────────────────────────

    private fun fetchNextQuestion(
        topic: String,
        questionIndex: Int,
        totalQuestions: Int,
        bufferOnly: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val prompt = buildQuizPrompt(topic, questionIndex + 1, totalQuestions)

                val raw = aiRepository.generateQuizContent(prompt).getOrThrow()
                val question = parseQuizQuestion(raw)

                // luôn update session local trước
                currentSession = currentSession?.copy(
                    questions = (currentSession?.questions ?: emptyList()) + question,
                    currentIndex = questionIndex
                )
                _sessionState.value = currentSession
                if (bufferOnly) {
                    questionBuffer.add(question)
                    return@launch   // ❗ STOP, không update Firestore
                }

                // UI update (chỉ khi main question)
                _uiState.update {
                    it.copy(
                        currentQuestion = question,
                        currentQuestionIndex = questionIndex,
                        isLoading = false,
                        selectedAnswer = null,
                        isAnswerRevealed = false,
                        error = null
                    )
                }

                // Firestore update (ONLY main flow)
                sessionId?.let {
                    quizRepository.updateSession(
                        it,
                        mapOf(
                            "questions" to (currentSession?.questions ?: emptyList()),
                            "currentIndex" to questionIndex
                        )
                    )
                }

                // preload next
                if (questionIndex + 1 < totalQuestions) {
                    fetchNextQuestion(topic, questionIndex + 1, totalQuestions, bufferOnly = true)
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to generate question"
                    )
                }
            }
        }
    }
    private fun buildQuizPrompt(
        topic: String,
        questionNumber: Int,
        totalQuestions: Int
    ): String = """
You are a smart quiz generator. Generate question $questionNumber of $totalQuestions about: "$topic".

STRICT RULES:
- Output ONLY valid JSON
- Do NOT include markdown
- Do NOT include any text before or after JSON
- The first character must be '{'
- The last character must be '}'
- Ensure JSON is fully complete (never truncate output)

JSON FORMAT:
{
  "question": "The full question",
  "options": {
    "A": "First option",
    "B": "Second option",
    "C": "Third option",
    "D": "Fourth option"
  },
  "correctAnswer": "The correct answer to the question",
  "explanation": "Brief explanation on why it's correct"
}

Rules:
- The correct answer must be either A, B, C or D
- Do not repeat previous questions
""".trimIndent()

    private fun parseQuizQuestion(raw: String): QuizQuestion {
        return try {
            val start = raw.indexOf("{")
            val end = raw.lastIndexOf("}")

            val safeJson = if (start != -1 && end != -1 && end > start) {
                raw.substring(start, end + 1)
            } else if (start != -1) {
                raw.substring(start) + "}"
            } else {
                throw IllegalStateException("No JSON found")
            }

            val json = JSONObject(safeJson)
            val optionsJson = json.getJSONObject("options")

            QuizQuestion(
                question = json.getString("question"),
                options = mapOf(
                    "A" to optionsJson.getString("A"),
                    "B" to optionsJson.getString("B"),
                    "C" to optionsJson.getString("C"),
                    "D" to optionsJson.getString("D")
                ),
                correctAnswer = json.getString("correctAnswer"),
                explanation = json.optString("explanation", "")
            )

        } catch (e: Exception) {
            Log.e("QuizParse", "FAILED RAW:\n$raw", e)

            QuizQuestion(
                question = "AI failed to generate question",
                options = mapOf(
                    "A" to "Retry",
                    "B" to "Retry",
                    "C" to "Retry",
                    "D" to "Retry"
                ),
                correctAnswer = "A",
                explanation = "Invalid AI response"
            )
        }
    }

    // ─── Factory ─────────────────────────────────────────────────────────────

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return QuizViewModel(
                    scheduleRepository = ScheduleRepository(),
                    aiRepository = FirebaseAiChatRepository()
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

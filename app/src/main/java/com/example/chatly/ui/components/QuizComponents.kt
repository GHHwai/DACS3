package com.example.chatly.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chatly.data.model.QuizUiState

// ─── Topic Selection Dialog ───────────────────────────────────────────────────

@Composable
fun QuizTopicDialog(
    uiState: QuizUiState,
    onTopicChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Quiz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Start a Quiz",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Enter a topic or pick from your schedule",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Topic input
                OutlinedTextField(
                    value = uiState.topicInput,
                    onValueChange = onTopicChange,
                    label = { Text("Topic (e.g. World War II, Python, Calculus)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Schedule subject quick picks
                if (uiState.scheduleSubjects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "From your schedule:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.scheduleSubjects) { subject ->
                            FilterChip(
                                selected = uiState.topicInput == subject,
                                onClick = { onTopicChange(subject) },
                                label = { Text(subject, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(uiState.topicInput.trim()) },
                        enabled = uiState.topicInput.isNotBlank(),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text("Start Quiz", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Quiz Question Card ───────────────────────────────────────────────────────

@Composable
fun QuizQuestionCard(
    uiState: QuizUiState,
    onSelectAnswer: (String) -> Unit,
    onNext: () -> Unit
) {
    val question = uiState.currentQuestion ?: return
    val optionLabels = listOf("A", "B", "C", "D")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Progress bar
        QuizProgressHeader(
            current = uiState.currentQuestionIndex + 1,
            total = uiState.totalQuestions,
            score = uiState.score
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Question text
        AnimatedContent(
            targetState = question.question,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            label = "question_anim"
        ) { questionText ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Text(
                    text = questionText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Answer options
        optionLabels.forEach { label ->
            val optionText = question.options[label] ?: return@forEach
            AnswerOptionButton(
                label = label,
                text = optionText,
                isSelected = uiState.selectedAnswer == label,
                isRevealed = uiState.isAnswerRevealed,
                isCorrect = question.correctAnswer == label,
                onClick = { if (!uiState.isAnswerRevealed) onSelectAnswer(label) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Explanation + Next button (shown after answer)
        AnimatedVisibility(
            visible = uiState.isAnswerRevealed,
            enter = slideInVertically { it } + fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                if (question.explanation.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("💡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = question.explanation,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                val isLast = uiState.currentQuestionIndex + 1 >= uiState.totalQuestions
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        if (isLast) "See Results 🏆" else "Next Question →",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerOptionButton(
    label: String,
    text: String,
    isSelected: Boolean,
    isRevealed: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !isRevealed -> if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface
        isCorrect -> Color(0xFF22C55E).copy(alpha = 0.18f)
        isSelected -> Color(0xFFEF4444).copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        !isRevealed -> if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outlineVariant
        isCorrect -> Color(0xFF22C55E)
        isSelected -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val labelBgColor = when {
        !isRevealed -> if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.secondaryContainer
        isCorrect -> Color(0xFF22C55E)
        isSelected -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val labelTextColor = when {
        (!isRevealed && isSelected) || (isRevealed && (isCorrect || isSelected)) ->
            Color.White
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(1.5.dp, borderColor),
        tonalElevation = if (isSelected && !isRevealed) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(labelBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = labelTextColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                lineHeight = 20.sp
            )
            // Correct tick / Wrong cross
            if (isRevealed && isCorrect) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Correct",
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(22.dp)
                )
            } else if (isRevealed && isSelected && !isCorrect) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Wrong",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun QuizProgressHeader(current: Int, total: Int, score: Int) {
    val progress = current.toFloat() / total.toFloat()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Question $current / $total",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    "Score: $score",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

// ─── Quiz Result Screen ───────────────────────────────────────────────────────

@Composable
fun QuizResultScreen(
    uiState: QuizUiState,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val pct = if (uiState.totalQuestions > 0)
        (uiState.score.toFloat() / uiState.totalQuestions * 100).toInt()
    else 0

    val (emoji, message, tint) = when {
        pct >= 80 -> Triple("🏆", "Excellent work!", Color(0xFFF59E0B))
        pct >= 60 -> Triple("👍", "Good job!", MaterialTheme.colorScheme.primary)
        pct >= 40 -> Triple("📚", "Keep studying!", Color(0xFF6366F1))
        else      -> Triple("💪", "Don't give up!", Color(0xFFEF4444))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 72.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Topic: ${uiState.topicInput}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Score ring
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { pct / 100f },
                modifier = Modifier.size(140.dp),
                strokeWidth = 10.dp,
                color = tint,
                trackColor = tint.copy(alpha = 0.15f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${uiState.score}/${uiState.totalQuestions}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$pct%",
                    fontSize = 16.sp,
                    color = tint,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Play Again", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text("Back to Chat", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ─── Loading shimmer while AI generates ──────────────────────────────────────

@Composable
fun QuizLoadingCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Quiz,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = shimmerAlpha),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Generating question…",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = shimmerAlpha)
        )
        Spacer(modifier = Modifier.height(24.dp))
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
    }
}

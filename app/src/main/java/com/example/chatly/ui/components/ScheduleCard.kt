package com.example.chatly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatly.data.model.StudySchedule

@Composable
fun ScheduleCard(
    schedule: StudySchedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = schedule.subject,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("${schedule.startTime} - ${schedule.endTime}")

            Text("Room: ${schedule.room}")

            Text("Teacher: ${schedule.teacher}")

            Spacer(modifier = Modifier.height(12.dp))

            Row {

                IconButton(onClick = onEdit) {

                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null
                    )
                }

                IconButton(onClick = onDelete) {

                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
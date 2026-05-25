package com.example.chatly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DayFilterChips(
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {

    val days = listOf(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        days.forEach { day ->

            FilterChip(
                selected = selectedDay == day,
                onClick = {
                    onDaySelected(day)
                },
                label = {
                    Text(day.take(3))
                }
            )
        }
    }
}
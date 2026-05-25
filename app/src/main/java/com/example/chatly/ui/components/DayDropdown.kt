package com.example.chatly.ui.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDropdown(
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {

    val days = listOf(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
    )

    var expanded by remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(

        expanded = expanded,

        onExpandedChange = {
            expanded = !expanded
        }

    ) {

        OutlinedTextField(

            value = selectedDay,

            onValueChange = {},

            readOnly = true,

            label = {
                Text("Day")
            },

            trailingIcon = {

                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },

            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(

            expanded = expanded,

            onDismissRequest = {
                expanded = false
            }

        ) {

            days.forEach { day ->

                DropdownMenuItem(

                    text = {
                        Text(day)
                    },

                    onClick = {

                        onDaySelected(day)

                        expanded = false
                    }
                )
            }
        }
    }
}
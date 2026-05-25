package com.example.chatly.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScheduleSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {

    ChatlyTextField(
        value = query,
        onValueChange = onQueryChange,
        label = "Search subject",
        leadingIcon = Icons.Default.Search,
        modifier = Modifier.fillMaxWidth()
    )
}
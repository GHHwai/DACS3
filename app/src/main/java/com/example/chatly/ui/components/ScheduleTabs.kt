package com.example.chatly.ui.components

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ScheduleTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    TabRow(selectedTabIndex = selectedTab) {

        Tab(
            selected = selectedTab == 0,
            onClick = {
                onTabSelected(0)
            },
            text = {
                Text("Study")
            }
        )

        Tab(
            selected = selectedTab == 1,
            onClick = {
                onTabSelected(1)
            },
            text = {
                Text("Exam")
            }
        )
    }
}
package com.example.greenpulse.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Schedule : Screen("schedule", "Plan", Icons.Default.Schedule)
    object Reports : Screen("reports", "Daily", Icons.Default.Assessment)
    object Action : Screen("action", "Add", Icons.Default.Add)
    object SMSLogs : Screen("sms_logs", "Logs", Icons.Default.History)
    object Settings : Screen("settings", "Set", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Schedule, Reports, Action, SMSLogs, Settings)
    }
}

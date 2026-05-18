package com.example.greenpulse.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Schedule : Screen("schedule", "Today", Icons.Default.Schedule)
    object Tablets : Screen("tablets", "Medication", Icons.Default.Medication)
    object Reports : Screen("reports", "Daily", Icons.Default.Assessment)
    object SMSLogs : Screen("sms_logs", "Logs", Icons.Default.History)
    object Settings : Screen("settings", "Set", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Schedule, Tablets, Reports, SMSLogs, Settings)
    }
}

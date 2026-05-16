package com.example.greenpulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.greenpulse.MainViewModel
import com.example.greenpulse.data.SMSLog
import com.example.greenpulse.data.SMSLogType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SMSLogsScreen(viewModel: MainViewModel) {
    val logs = viewModel.smsLogs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "SMS History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No logs yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(logs) { log ->
                    SMSLogCard(log)
                }
            }
        }
    }
}

@Composable
fun SMSLogCard(log: SMSLog) {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (log.type) {
                SMSLogType.WARNING -> MaterialTheme.colorScheme.errorContainer
                SMSLogType.CONFIRMATION -> MaterialTheme.colorScheme.primaryContainer
                SMSLogType.REMINDER -> MaterialTheme.colorScheme.secondaryContainer
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

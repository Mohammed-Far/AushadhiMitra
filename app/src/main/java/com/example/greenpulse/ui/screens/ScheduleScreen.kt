package com.example.greenpulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.greenpulse.MainViewModel
import com.example.greenpulse.data.DoseRecord
import com.example.greenpulse.data.DoseStatus

@Composable
fun ScheduleScreen(viewModel: MainViewModel) {
    val doses = viewModel.doseRecords

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Today's Schedule",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Simulation Button
            IconButton(onClick = { 
                if (viewModel.medicines.isNotEmpty()) {
                    viewModel.triggerAlert(viewModel.medicines.first())
                }
            }) {
                Icon(Icons.Default.NotificationsActive, contentDescription = "Simulate Alert", tint = MaterialTheme.colorScheme.secondary)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(doses) { dose ->
                DoseCard(dose) { viewModel.markAsTaken(dose.id) }
            }
        }
    }
}

@Composable
fun DoseCard(dose: DoseRecord, onMarkTaken: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = dose.medicineName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Slot ${dose.slot.name} • ${dose.scheduledTime}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            
            IconButton(onClick = onMarkTaken) {
                Icon(
                    imageVector = if (dose.status == DoseStatus.TAKEN) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Status",
                    tint = if (dose.status == DoseStatus.TAKEN) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

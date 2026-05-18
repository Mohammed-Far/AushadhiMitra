package com.example.greenpulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                text = "Medication Plan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            
            IconButton(onClick = { 
                val pending = doses.find { it.status == DoseStatus.PENDING }
                pending?.let { p ->
                    val med = viewModel.medicines.find { it.name == p.medicineName }
                    med?.let { m -> viewModel.triggerAlert(m, p.id) }
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
                DoseCard(dose) {
                    if (dose.status == DoseStatus.DISPENSED) {
                        viewModel.simulatePickup(dose.id)
                    }
                }
            }
        }
    }
}

@Composable
fun DoseCard(dose: DoseRecord, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when(dose.status) {
                DoseStatus.MISSED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                DoseStatus.TAKEN -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                DoseStatus.DISPENSED -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dose.medicineName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Slot ${dose.slot.name} • ${dose.scheduledTime}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                
                if (dose.status == DoseStatus.DISPENSED) {
                    Text(
                        "DISPENSED: Waiting for pickup...", 
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            when (dose.status) {
                DoseStatus.TAKEN -> Icon(Icons.Default.CheckCircle, "Taken", tint = MaterialTheme.colorScheme.primary)
                DoseStatus.MISSED -> Icon(Icons.Default.Warning, "Missed", tint = MaterialTheme.colorScheme.error)
                DoseStatus.DISPENSED -> {
                    Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                        Text("SIMULATE PICKUP", fontSize = 10.sp)
                    }
                }
                else -> Icon(Icons.Default.RadioButtonUnchecked, "Pending", tint = Color.Gray)
            }
        }
    }
}

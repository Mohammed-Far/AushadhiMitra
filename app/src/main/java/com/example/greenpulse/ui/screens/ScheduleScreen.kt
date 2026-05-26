package com.example.greenpulse.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    val isLoading by viewModel.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ... (Header Box content remains same)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
                    ),
                )
                .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "AushadhiMitra",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    )
                    Text(
                        text = "Today's Plan",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                
                Surface(
                    onClick = { 
                        // Modified logic to dispense in order: Slot 1 -> 2 -> 3 -> 4
                        val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                        
                        // Find the first pending record across all days and slots in order
                        var foundRecord: DoseRecord? = null
                        for (day in daysOrder) {
                            val dayDoses = doses.filter { it.dayOfWeek == day }
                            // Sort by slot index (S1=0, S2=1, etc.)
                            val sortedDoses = dayDoses.sortedBy { it.slot.ordinal }
                            foundRecord = sortedDoses.find { it.status == DoseStatus.PENDING }
                            if (foundRecord != null) break
                        }

                        foundRecord?.let { p ->
                            val med = viewModel.medicines.find { it.slot == p.slot }
                            med?.let { m -> viewModel.triggerAlert(m, p.id) }
                        }
                    },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (doses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.EventNote, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    Text("No medications scheduled", color = MaterialTheme.colorScheme.secondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(doses) { dose ->
                    PremiumDoseCard(dose) {
                        if (dose.status == DoseStatus.DISPENSED) {
                            viewModel.simulatePickup(dose.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumDoseCard(dose: DoseRecord, onAction: () -> Unit) {
    val targetColor = when(dose.status) {
        DoseStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
        DoseStatus.TAKEN -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        DoseStatus.DISPENSED -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    val cardColor by animateColorAsState(targetValue = targetColor, label = "cardColor")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (dose.status == DoseStatus.PENDING) MaterialTheme.colorScheme.surfaceVariant 
                        else Color.White.copy(alpha = 0.3f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when(dose.status) {
                        DoseStatus.TAKEN -> Icons.Default.CheckCircle
                        DoseStatus.MISSED -> Icons.Default.Warning
                        DoseStatus.DISPENSED -> Icons.Default.WatchLater
                        else -> Icons.Default.Medication
                    },
                    contentDescription = null,
                    tint = if (dose.status == DoseStatus.PENDING) MaterialTheme.colorScheme.primary else Color.Unspecified,
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dose.medicineName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = dose.scheduledTime, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("•", color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Slot ${dose.slot.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
                
                if (dose.status == DoseStatus.DISPENSED) {
                    Text(
                        text = "DISPENSED: Pick up from tray!", 
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (dose.status == DoseStatus.DISPENSED) {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onTertiaryContainer),
                ) {
                    Text("PICK UP", fontSize = 12.sp)
                }
            } else {
                Icon(
                    imageVector = when (dose.status) {
                        DoseStatus.TAKEN -> Icons.Default.CheckCircle
                        DoseStatus.MISSED -> Icons.Default.Error
                        else -> Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = null,
                    tint = when (dose.status) {
                        DoseStatus.TAKEN -> MaterialTheme.colorScheme.primary
                        DoseStatus.MISSED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.outline
                    },
                )
            }
        }
    }
}

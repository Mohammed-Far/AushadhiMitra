package com.example.greenpulse.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greenpulse.MainViewModel
import com.example.greenpulse.data.DayOfWeek
import com.example.greenpulse.data.Medicine
import com.example.greenpulse.data.ScheduleType

@Composable
fun TabletsScreen(viewModel: MainViewModel) {
    val medicines = viewModel.medicines
    var slotToEdit by remember { mutableStateOf<Medicine?>(value = null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Premium Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                    ),
                )
                .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
        ) {
            Column {
                Text(
                    text = "Medication",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                )
                Text(
                    text = "6 Tablet Slots",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSecondary,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(medicines) { med ->
                MedicineSlotCard(
                    med = med,
                    onEdit = { slotToEdit = med },
                    onDelete = { viewModel.clearSlot(med.slot) },
                    onCancel = { viewModel.cancelMedication(med.slot) }
                )
            }
        }
    }

    if (slotToEdit != null) {
        val currentSlot = slotToEdit!!
        SlotEditDialog(
            slotName = currentSlot.tabletName,
            initialMedicineName = currentSlot.userMedicineName,
            initialTime = currentSlot.time,
            initialScheduleType = currentSlot.scheduleType,
            initialSelectedDays = currentSlot.selectedDays,
            onDismiss = { slotToEdit = null },
            onSave = { name, time, type, days ->
                viewModel.updateMedicine(currentSlot.slot, name, time, type, days)
                slotToEdit = null
            }
        )
    }
}

@Composable
fun MedicineSlotCard(med: Medicine, onEdit: () -> Unit, onDelete: () -> Unit, onCancel: () -> Unit) {
    val isFilled = med.userMedicineName.isNotEmpty() && med.isActive

    if (isFilled) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { onEdit() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = med.tabletName.replace("Tablet ", "T"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = med.tabletName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "💊 ${med.userMedicineName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🕐 ${med.time}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "📅 ${med.scheduleType.name.replace("_", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                Column {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Cancel Course", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    } else {
        // Empty Slot or Cancelled
        val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        val label = if (!med.isActive && med.userMedicineName.isNotEmpty()) "Course Finished" else "Empty"
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onEdit() }
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = Color.LightGray,
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = med.tabletName,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "$label (Tap to fill)",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotEditDialog(
    slotName: String,
    initialMedicineName: String,
    initialTime: String,
    initialScheduleType: ScheduleType,
    initialSelectedDays: List<DayOfWeek>,
    onDismiss: () -> Unit,
    onSave: (String, String, ScheduleType, List<DayOfWeek>) -> Unit,
) {
    var name by remember { mutableStateOf(value = initialMedicineName) }
    var time by remember { mutableStateOf(initialTime) }
    var scheduleType by remember { mutableStateOf(initialScheduleType) }
    val selectedDays = remember { mutableStateListOf(*initialSelectedDays.toTypedArray()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure $slotName") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medicine Name") },
                    placeholder = { Text("e.g. Aspirin") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Reminder Time (HH:MM)") },
                    placeholder = { Text("08:00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider()

                Text("Schedule", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                ScheduleType.entries.forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { scheduleType = type },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = scheduleType == type, onClick = { scheduleType = type })
                        Text(type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
                    }
                }

                if (scheduleType == ScheduleType.SPECIFIC_DAYS) {
                    Text("Select Days", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    FlowRow(modifier = Modifier.fillMaxWidth()) {
                        DayOfWeek.entries.forEach { day ->
                            FilterChip(
                                selected = selectedDays.contains(day),
                                onClick = {
                                    if (selectedDays.contains(day)) selectedDays.remove(day)
                                    else selectedDays.add(day)
                                },
                                label = { Text(day.name.take(3)) },
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, time, scheduleType, selectedDays.toList()) },
                enabled = name.isNotBlank() && time.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

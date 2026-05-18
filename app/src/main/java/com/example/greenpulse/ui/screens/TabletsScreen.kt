package com.example.greenpulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.greenpulse.MainViewModel
import com.example.greenpulse.data.Medicine

@Composable
fun TabletsScreen(viewModel: MainViewModel) {
    val medicines = viewModel.medicines
    var editingMed by remember { mutableStateOf<Medicine?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Medication Setup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(medicines) { med ->
                TabletSettingCard(med) { editingMed = med }
            }
        }
    }

    if (editingMed != null) {
        val currentMed = editingMed!!
        TimeEditDialog(
            medicine = currentMed,
            onDismiss = { editingMed = null }
        ) { times ->
            viewModel.updateTabletTimings(currentMed.id, times)
            editingMed = null
        }
    }
}

@Composable
fun TabletSettingCard(med: Medicine, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(med.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Slot ${med.slot.name}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (med.reminderTimes.isEmpty()) "No times set" else "Times: ${med.reminderTimes.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Times")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeEditDialog(medicine: Medicine, onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    val times = remember { mutableStateListOf<String>().apply { addAll(medicine.reminderTimes) } }
    var newTime by remember { mutableStateOf("08:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Times for ${medicine.name}") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTime,
                        onValueChange = { newTime = it },
                        label = { Text("HH:mm") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { if (newTime.isNotBlank()) times.add(newTime) }) {
                        Icon(Icons.Default.Add, null)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    times.forEach { time ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(time)
                            IconButton(onClick = { times.remove(time) }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(times.toList()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

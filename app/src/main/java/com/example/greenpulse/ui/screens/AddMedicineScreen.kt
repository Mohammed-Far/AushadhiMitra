package com.example.greenpulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.greenpulse.data.IntakeType
import com.example.greenpulse.data.SlotID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(onSave: (String, SlotID, IntakeType, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf(SlotID.S1) }
    var intakeType by remember { mutableStateOf(IntakeType.NONE) }
    var dosage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Add New Medicine",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Medicine Name") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Hardware Slot", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SlotID.entries.forEach { slot ->
                FilterChip(
                    selected = selectedSlot == slot,
                    onClick = { selectedSlot = slot },
                    label = { Text(slot.name) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Intake Instructions", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IntakeType.entries.forEach { type ->
                FilterChip(
                    selected = intakeType == type,
                    onClick = { intakeType = type },
                    label = { Text(type.name.replace("_", " ")) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = { Text("Dosage (e.g., 1 pill)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSave(name, selectedSlot, intakeType, dosage) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            enabled = name.isNotBlank()
        ) {
            Text("Save Medicine", modifier = Modifier.padding(8.dp))
        }
    }
}

package com.example.greenpulse.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.greenpulse.MainViewModel
import com.example.greenpulse.data.Medicine

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
                )
            }
        }
    }

    if (slotToEdit != null) {
        val currentSlot = slotToEdit!!
        SlotEditDialog(
            slotName = currentSlot.tabletName,
            initialMedicineName = currentSlot.userMedicineName,
            initialTime = currentSlot.time.ifEmpty { "08:00" },
            onDismiss = { slotToEdit = null },
        ) { name, time ->
            viewModel.updateMedicine(currentSlot.slot, name, time)
            slotToEdit = null
        }
    }
}

@Composable
fun MedicineSlotCard(med: Medicine, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isFilled = med.userMedicineName.isNotEmpty()

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
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    } else {
        // Empty Slot with Dashed Border
        val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        
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
                    text = "Empty (Tap to fill)",
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
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var name by remember { mutableStateOf(value = initialMedicineName) }
    var time by remember { mutableStateOf(value = initialTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure $slotName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    label = { Text("Reminder Time") },
                    placeholder = { Text("HH:mm") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, time) },
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

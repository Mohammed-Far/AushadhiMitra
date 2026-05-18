package com.example.greenpulse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.greenpulse.MainViewModel
import com.example.greenpulse.data.PatientProfile

@Composable
fun SettingsScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    var isPaired by remember { mutableStateOf(value = false) }
    val profile by viewModel.patientProfile
    val scrollState = rememberScrollState()
    var showEditDialog by remember { mutableStateOf(value = false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
    ) {
        Column {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = "User & Device Setup",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Premium Profile Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Age: ${profile.age} • ${profile.gender} • Type: ${profile.bloodGroup}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Standardized Profile Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Patient Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileDetailItem(Icons.Default.WaterDrop, "Blood Group", profile.bloodGroup)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileDetailItem(Icons.Default.MonitorWeight, "Weight", profile.weight)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileDetailItem(Icons.Default.Transgender, "Gender", profile.gender)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileDetailItem(Icons.Default.Settings, "Condition", profile.healthCondition)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hardware Section
        Text("Hardware Connectivity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Bluetooth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("AushadhiMitra_Box_v1", fontWeight = FontWeight.Bold)
                    Text(if (isPaired) "ESP32 Connected via BLE/Wi-Fi" else "Searching for nearby devices...", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = isPaired, onCheckedChange = { isPaired = it })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showEditDialog) {
        EditProfileDialog(
            profile = profile,
            onDismiss = { showEditDialog = false }
        ) { updatedProfile ->
            viewModel.updatePatientProfile(updatedProfile)
            showEditDialog = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    profile: PatientProfile,
    onDismiss: () -> Unit,
    onSave: (PatientProfile) -> Unit
) {
    var name by remember { mutableStateOf(value = profile.name) }
    var age by remember { mutableStateOf(value = profile.age) }
    var gender by remember { mutableStateOf(value = profile.gender) }
    var bloodGroup by remember { mutableStateOf(value = profile.bloodGroup) }
    var weight by remember { mutableStateOf(value = profile.weight) }
    var condition by remember { mutableStateOf(value = profile.healthCondition) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Patient Profile") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") })
                OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender") })
                OutlinedTextField(value = bloodGroup, onValueChange = { bloodGroup = it }, label = { Text("Blood Group") })
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight") })
                OutlinedTextField(value = condition, onValueChange = { condition = it }, label = { Text("Health Condition") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(PatientProfile(name, age, gender, bloodGroup, weight, condition))
                },
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileDetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

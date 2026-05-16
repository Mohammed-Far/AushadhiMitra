package com.example.greenpulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.greenpulse.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    var isPaired by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Text("Device Pairing", style = MaterialTheme.typography.titleLarge)
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
                    Text("ESP32 MedBox", fontWeight = FontWeight.Bold)
                    Text(if (isPaired) "Connected via Bluetooth" else "Not Connected", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = isPaired, onCheckedChange = { isPaired = it })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Patient Profile", style = MaterialTheme.typography.titleLarge)
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileItem(Icons.Default.Person, "John Doe", "Patient Name")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileItem(Icons.Default.Wifi, "MedBox_WiFi_Guest", "Active Network")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        OutlinedButton(
            onClick = { /* Logout */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun ProfileItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

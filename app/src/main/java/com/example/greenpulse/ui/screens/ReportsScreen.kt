package com.example.greenpulse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.greenpulse.MainViewModel
import com.example.greenpulse.data.DoseStatus

@Composable
fun ReportsScreen(viewModel: MainViewModel) {
    val records = viewModel.doseRecords
    val takenCount = records.count { it.status == DoseStatus.TAKEN }
    val totalCount = records.size
    val adherence = if (totalCount > 0) (takenCount.toFloat() / totalCount.toFloat()) else 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Adherence Analytics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Adherence Rate Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Overall Adherence", style = MaterialTheme.typography.titleMedium)
                Text("${(adherence * 100).toInt()}%", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black)
                LinearProgressIndicator(
                    progress = { adherence },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Taken", takenCount.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            StatCard("Missed", records.count { it.status == DoseStatus.MISSED }.toString(), MaterialTheme.colorScheme.error, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Weekly Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                listOf(0.4f, 0.7f, 0.9f, 0.8f, 1.0f, 0.6f, adherence).forEach { height ->
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight(height)
                            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraSmall),
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

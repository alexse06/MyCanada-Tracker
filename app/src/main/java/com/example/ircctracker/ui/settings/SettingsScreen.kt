package com.example.ircctracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.ircctracker.data.repository.GeminiRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    geminiRepository: GeminiRepository,
    onBack: () -> Unit
) {
    var apiKey by remember { mutableStateOf(geminiRepository.getApiKey() ?: "") }
    var savedMessageVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("AI Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Gemini API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Required for AI Insights and Live Processing Times.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notification Toggle
            var notificationsEnabled by remember { mutableStateOf(true) } // Load from prefs in real app
            val context = androidx.compose.ui.platform.LocalContext.current
            val prefs = remember { context.getSharedPreferences("worker_prefs", android.content.Context.MODE_PRIVATE) }
            
            LaunchedEffect(Unit) {
                notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Background Updates", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { 
                        notificationsEnabled = it 
                        prefs.edit().putBoolean("notifications_enabled", it).apply()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Frequency Selector
            Text("Update Frequency", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            
            val frequencies = listOf(
                "Every 4 Hours" to 4 * 60L,
                "Every 12 Hours" to 12 * 60L,
                "Daily (24 Hours)" to 24 * 60L
            )
            var selectedFreq by remember { mutableStateOf(15L) } // Default 15 min for testing -> 15L
             LaunchedEffect(Unit) {
                selectedFreq = prefs.getLong("update_interval_minutes", 15L)
            }

            frequencies.forEach { (label, minutes) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedFreq == minutes,
                        onClick = { 
                            selectedFreq = minutes
                            prefs.edit().putLong("update_interval_minutes", minutes).apply()
                        },
                         enabled = notificationsEnabled
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp),
                        color = if (notificationsEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    geminiRepository.saveApiKey(apiKey)
                    savedMessageVisible = true
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save Configuration")
            }
            
            if (savedMessageVisible) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                   Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                       Text("\u2705", style = MaterialTheme.typography.titleMedium)
                       Spacer(modifier = Modifier.width(8.dp))
                       Text("Settings Saved!", color = MaterialTheme.colorScheme.onPrimaryContainer)
                   }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "IRCC Tracker v1.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

package com.example.ircctracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ircctracker.data.remote.ApplicationSummary
import com.example.ircctracker.data.remote.DashboardResponse
import com.example.ircctracker.data.repository.DashboardRepository
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    token: String,
    repository: DashboardRepository,
    onAppClick: (String) -> Unit // Return App ID
) {
    var state by remember { mutableStateOf<DashboardUiState>(DashboardUiState.Loading) }

    LaunchedEffect(token) {
        repository.getApplications(token).collectLatest { result ->
            result.onSuccess { response ->
                state = DashboardUiState.Success(response.apps ?: emptyList())
            }
            result.onFailure { error ->
                state = DashboardUiState.Error(error.localizedMessage ?: "Unknown error")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Applications", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            when (val s = state) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DashboardUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                        Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is DashboardUiState.Success -> {
                    if (s.items.isEmpty()) {
                        Text("No applications found.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(s.items) { item ->
                                ApplicationCard(item) { onAppClick(item.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationCard(item: ApplicationSummary, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Application #",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                StatusBadge(status = item.status)
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
            
            // Placeholder for progress - visual inspiration from reference app
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Current Status: ${item.status}", 
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val isClosed = item.status.equals("Closed", ignoreCase = true)
            val progress = if (isClosed) 1.0f else 0.7f
            val completeness = if (isClosed) "100%" else "70%"
            
            // Fake progress bar for visuals (placeholder)
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.LightGray, CircleShape),
                color = if (isClosed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary, // Green if closed
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            Text(
                text = if (isClosed) "Finalized" else "Estimated Completion: $completeness",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "submitted" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "approved" -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    
    Surface(
        color = bgColor,
        shape = CircleShape
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val items: List<ApplicationSummary>) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

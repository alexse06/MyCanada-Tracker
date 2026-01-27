package com.example.ircctracker.ui.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ircctracker.util.ForecastHelper
import com.example.ircctracker.util.ForecastResult

@Composable
fun CrystalBallCard(result: ForecastResult) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(androidx.compose.ui.res.stringResource(com.example.ircctracker.R.string.crystal_ball_title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(result.estimatedDate, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(androidx.compose.ui.res.stringResource(com.example.ircctracker.R.string.est_decision_label), style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    progress = result.progress,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.3f),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("${result.daysRemaining} " + androidx.compose.ui.res.stringResource(com.example.ircctracker.R.string.days_suffix), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(androidx.compose.ui.res.stringResource(com.example.ircctracker.R.string.remaining), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun NewsFeedCard(newsItems: List<String>) {
    if (newsItems.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(androidx.compose.ui.res.stringResource(com.example.ircctracker.R.string.news_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            newsItems.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("•", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

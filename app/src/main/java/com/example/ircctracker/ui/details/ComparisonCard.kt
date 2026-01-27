package com.example.ircctracker.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ComparisonCard(
    dateReceived: String?,
    lob: String?,
    totalMonths: Int
) {
    if (dateReceived == null) return

    val monthsPassed = try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME // e.g. 2025-01-01T...
        val date = LocalDate.parse(dateReceived, formatter)
        ChronoUnit.MONTHS.between(date, LocalDate.now())
    } catch (e: Exception) {
         // Fallback if date parsing fails (try yyyy-MM-dd)
         try {
             val date = LocalDate.parse(dateReceived.take(10))
             ChronoUnit.MONTHS.between(date, LocalDate.now())
         } catch (e2: Exception) {
             0L
         }
    }

    val progress = (monthsPassed.toFloat() / totalMonths.toFloat()).coerceIn(0f, 1f)
    val remaining = (totalMonths - monthsPassed).coerceAtLeast(0)
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Progress Tracker",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${(progress * 100).toInt()}% Complete",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "~$remaining months left",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Based on current processing time for '$lob' ($totalMonths months).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                     text = "Live Data (Google) \u26A1",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

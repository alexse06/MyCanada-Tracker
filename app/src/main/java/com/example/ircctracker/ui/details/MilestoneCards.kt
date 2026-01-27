package com.example.ircctracker.ui.details

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ircctracker.util.ApplicationMilestones
import com.example.ircctracker.util.MilestoneStatus
import androidx.compose.ui.res.stringResource
import com.example.ircctracker.R

@Composable
fun MilestoneCards(milestones: ApplicationMilestones) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MilestoneCard(
            title = stringResource(R.string.medical_exam),
            status = milestones.medical,
            icon = Icons.Default.Favorite,
            delay = 0,
            visible = visible,
            modifier = Modifier.weight(1f)
        )
        MilestoneCard(
            title = stringResource(R.string.biometrics),
            status = milestones.biometrics,
            icon = Icons.Default.Face,
            delay = 100,
            visible = visible,
             modifier = Modifier.weight(1f)
        )
        MilestoneCard(
            title = stringResource(R.string.background_check),
            status = milestones.background,
            icon = Icons.Default.Lock, // Shield icon not standard in default set, Lock is close
            delay = 200,
            visible = visible,
             modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MilestoneCard(
    title: String,
    status: MilestoneStatus,
    icon: ImageVector,
    delay: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 300, delayMillis = delay)
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, delayMillis = delay)
    )

    val (color, statusIcon) = when (status) {
        MilestoneStatus.PASSED -> Color(0xFF4CAF50) to Icons.Default.CheckCircle
        MilestoneStatus.PENDING -> Color(0xFFFF9800) to Icons.Default.Info
        MilestoneStatus.UNKNOWN -> Color.LightGray to Icons.Default.Info // Or Warning
    }
    
    val containerColor = if (status == MilestoneStatus.UNKNOWN) MaterialTheme.colorScheme.surfaceVariant else color.copy(alpha = 0.1f)

    Card(
        modifier = modifier
            .scale(scale)
            .height(100.dp), // Fixed height for uniformity
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if(status == MilestoneStatus.UNKNOWN) Color.Gray else color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface 
            )
            Spacer(modifier = Modifier.height(4.dp))
             Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = if(status == MilestoneStatus.UNKNOWN) Color.Gray else color,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

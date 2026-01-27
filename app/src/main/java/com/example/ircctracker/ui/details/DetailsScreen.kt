package com.example.ircctracker.ui.details

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ircctracker.data.remote.DetailsResponse
import com.example.ircctracker.data.remote.HistoryEvent
import com.example.ircctracker.data.repository.DetailsRepository
import com.example.ircctracker.R
import com.example.ircctracker.util.NetworkConnectivityObserver
import com.example.ircctracker.util.ConnectivityStatus
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DetailsScreen(
    token: String,
    appNum: String,
    uci: String,
    repository: DetailsRepository,
    geminiRepository: com.example.ircctracker.data.repository.GeminiRepository,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val viewModel: com.example.ircctracker.ui.details.DetailsViewModel = viewModel(
        factory = com.example.ircctracker.ui.details.DetailsViewModelFactory(repository, geminiRepository)
    )

    val state = viewModel.uiState.collectAsState()
    val aiInsights = viewModel.aiInsights.collectAsState()
    val estimatedMonths = viewModel.estimatedMonths.collectAsState()
    
    // Connectivity
    val context = LocalContext.current
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val networkStatus by connectivityObserver.observe().collectAsState(initial = ConnectivityStatus.Available)
    val isOffline = networkStatus == ConnectivityStatus.Lost || networkStatus == ConnectivityStatus.Unavailable
    
    // Pull to Refresh State
    val isRefreshing by remember { derivedStateOf { state.value is DetailsUiState.Loading } }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.fetchDetails(token, appNum, uci) }
    )

    LaunchedEffect(token, appNum) {
        if (state.value !is DetailsUiState.Success) {
            viewModel.fetchDetails(token, appNum, uci)
        }
    }
    
    // Sync Widget on Success
    LaunchedEffect(state.value) {
        val currentState = state.value
        if (currentState is DetailsUiState.Success) {
            val app = currentState.data.app
            if (app != null) {
                com.example.ircctracker.data.local.WidgetDataManager.saveState(
                    context,
                    status = app.status ?: "Unknown",
                    lastUpdated = app.lastUpdated ?: "N/A",
                    appNumber = app.appNumber ?: "N/A"
                )
                // Force Update
                com.example.ircctracker.widget.StatusWidget.forceUpdateAll(context)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timeline_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha=0.1f))
                .pullRefresh(pullRefreshState)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Offline Banner
                AnimatedVisibility(
                    visible = isOffline,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                   Box(
                       modifier = Modifier
                           .fillMaxWidth()
                           .background(MaterialTheme.colorScheme.errorContainer)
                           .padding(8.dp),
                       contentAlignment = Alignment.Center
                   ) {
                       Text(
                           text = stringResource(R.string.offline_mode),
                           color = MaterialTheme.colorScheme.onErrorContainer,
                           style = MaterialTheme.typography.labelMedium
                       )
                   }
                }
            
                Box(modifier = Modifier.weight(1f)) {
                    when (val s = state.value) {
                        is DetailsUiState.Loading -> {
                             // Show nothing here, indicator handles it, or show if empty
                             if (s !is DetailsUiState.Success) { 
                                 Box(modifier = Modifier.fillMaxSize()) 
                             }
                        }
                        is DetailsUiState.Error -> {
                             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                             }
                        }
                        is DetailsUiState.Success -> {
                            val app = s.data.app
                            val history = s.data.relations?.firstOrNull()?.history
                                ?.sortedByDescending { it.dateCreated } 
                                ?: emptyList()
                            val activities = s.data.relations?.firstOrNull()?.activities 
                            
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                // Header Card
                                item {
                                    Box {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = true,
                                            enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn()
                                        ) {
                                            ElevatedCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(20.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(stringResource(R.string.details_section), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                                        Text(app?.status?.uppercase() ?: stringResource(R.string.unknown_status), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                                                    }
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Text(app?.appNumber ?: "N/A", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("${stringResource(R.string.received_label)} ${app?.dateRecieved?.take(10) ?: "N/A"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("${stringResource(R.string.last_updated_label)} ${app?.lastUpdated?.take(10) ?: "N/A"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Status Hub
                                item {
                                    androidx.compose.foundation.layout.Column {
                                        Text(stringResource(R.string.status_hub_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                                        val milestones = com.example.ircctracker.util.StatusParser.parse(activities, history)
                                        MilestoneCards(milestones = milestones)
                                        
                                        if (milestones.lastUpdateDate != null) {
                                            val context = LocalContext.current
                                            val dateStr = milestones.lastUpdateDate
                                            val label = try {
                                                if (dateStr != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                   val date = java.time.LocalDate.parse(dateStr.take(10))
                                                   val diff = java.time.temporal.ChronoUnit.DAYS.between(date, java.time.LocalDate.now())
                                                    if (diff == 0L) context.getString(R.string.updated_today) else context.getString(R.string.updated_x_days_ago, diff)
                                                } else {
                                                   context.getString(R.string.last_updated_label) + " " + (dateStr ?: "N/A")
                                                }
                                            } catch (e: Exception) { context.getString(R.string.last_updated_label) + " " + (dateStr ?: "N/A") }
                                            
                                            Text(
                                                text = label, 
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(bottom = 16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                }

                                // Comparative Chart
                                item {
                                   val months = estimatedMonths.value
                                   if (months != null) {
                                       Box {
                                           androidx.compose.animation.AnimatedVisibility(
                                               visible = true,
                                               enter = expandVertically() + fadeIn()
                                           ) {
                                               Column {
                                                   com.example.ircctracker.ui.details.ComparisonCard(
                                                       dateReceived = app?.dateRecieved,
                                                       lob = app?.lob,
                                                       totalMonths = months
                                                   )
                                                   Spacer(modifier = Modifier.height(24.dp))
                                               }
                                           }
                                       }
                                   }
                                }

                                // Crystal Ball Forecast
                                item {
                                   val forecast = viewModel.forecast.collectAsState().value
                                   if (forecast != null) {
                                       com.example.ircctracker.ui.details.CrystalBallCard(result = forecast)
                                       Spacer(modifier = Modifier.height(24.dp))
                                   }
                                }

                                // Status Section
                                if (activities != null) {
                                    item {
                                        Text(stringResource(R.string.current_status_section), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        ElevatedCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                StatusRow(stringResource(R.string.eligibility), activities.eligibility)
                                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                                StatusRow(stringResource(R.string.medical_exam), activities.medical)
                                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                                StatusRow(stringResource(R.string.background_check), activities.background)
                                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                                StatusRow(stringResource(R.string.biometrics), activities.biometrics)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                }
                                
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // History Header
                                item {
                                    Text(stringResource(R.string.history_section), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    // DEBUG: Show status
                                    val status = viewModel.aiStatus.collectAsState().value
                                    val errorInsight = aiInsights.value["ERROR"]
                                    
                                    if (errorInsight != null) {
                                        Text(stringResource(R.string.ai_error_prefix) + errorInsight, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                itemsIndexed(history) { index, event ->
                                    val insight = aiInsights.value[event.key]
                                    
                                    TimelineItem(
                                        event = event, 
                                        isLast = index == history.lastIndex,
                                        aiInsight = insight
                                    )
                                }
                                
                                // News Feed
                                item {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    val news = viewModel.news.collectAsState().value
                                    com.example.ircctracker.ui.details.NewsFeedCard(newsItems = news)
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
fun StatusRow(label: String, status: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        val statusText = status ?: "N/A"
        val statusColor = when (statusText.lowercase()) {
            "completed" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            "inprogress" -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
            "notstarted" -> androidx.compose.ui.graphics.Color.Gray
            else -> MaterialTheme.colorScheme.secondary
        }
        
        Text(
            text = formatStatus(statusText), 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}

fun formatStatus(status: String): String {
    return status.replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}


@Composable
fun TimelineItem(event: HistoryEvent, isLast: Boolean, aiInsight: String? = null) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
         // Content Column (Left)
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp, bottom = 24.dp)) {
            Text(
                text = com.example.ircctracker.ui.util.TranslationHelper.translate(event.key), 
                style = MaterialTheme.typography.titleSmall, 
                fontWeight = FontWeight.Bold
            )
            Text(
                text = event.dateCreated?.take(10) ?: "No Date", 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.secondary
            )
            
            val desc = com.example.ircctracker.ui.util.TranslationHelper.getDescription(event.key)
            if (desc != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // AI Insight
            if (aiInsight != null) {
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
                        Text("🤖", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = aiInsight,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }

        // Timeline Line Column (Right)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(20.dp)) {
             // Dot
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
            )
            
            // Vertical Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    }
}

sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(val data: DetailsResponse) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
}

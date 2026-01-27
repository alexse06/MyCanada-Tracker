package com.example.ircctracker.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ircctracker.data.remote.DetailsResponse
import com.example.ircctracker.data.repository.DetailsRepository
import com.example.ircctracker.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val repository: DetailsRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _aiInsights = MutableStateFlow<Map<String, String>>(emptyMap())
    val aiInsights = _aiInsights.asStateFlow()
    
    private val _estimatedMonths = MutableStateFlow<Int?>(null)
    val estimatedMonths = _estimatedMonths.asStateFlow()

    private val _news = MutableStateFlow<List<String>>(emptyList())
    val news = _news.asStateFlow()

    private val _forecast = MutableStateFlow<com.example.ircctracker.util.ForecastResult?>(null)
    val forecast = _forecast.asStateFlow()
    
    private val processingTimeRepo = com.example.ircctracker.data.repository.ProcessingTimeRepository(geminiRepository)
    
    val hasApiKey: Boolean
        get() = geminiRepository.getApiKey() != null

    fun saveApiKey(key: String) {
        geminiRepository.saveApiKey(key)
        // Retry enhancement if we have data
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            val history = currentState.data.relations?.firstOrNull()?.history ?: emptyList()
            if (history.isNotEmpty()) {
                enhanceDetails(history)
            }
        }
    }

    fun fetchDetails(token: String, appNum: String, uci: String) {
        _uiState.value = DetailsUiState.Loading
        viewModelScope.launch {
            repository.getDetails(token, appNum, uci).collectLatest { result ->
                result.onSuccess { response ->
                    _uiState.value = DetailsUiState.Success(response)
                    


                    // Parallel AI tasks: Insights, Processing Time, News
                    val history = response.relations?.firstOrNull()?.history ?: emptyList()
                    val lob = response.app?.lob ?: ""
                    val dateReceived = response.app?.dateRecieved

                    launch {
                        val insights = geminiRepository.enhanceTimeline(history)
                        _aiInsights.value = insights
                        
                        // Simple status check
                        if (response.app?.status?.equals("Closed", ignoreCase = true) == true) {
                            _aiStatus.value = "AI: Application Closed (Complete)"
                        } else {
                            _aiStatus.value = "AI: Timeline Analyzed"
                        }
                    }
                    
                    launch {
                        val months = geminiRepository.getProcessingTime(lob)
                        _estimatedMonths.value = months
                        
                        // Calculate Forecast if we have months and dateReceived
                        if (months != null && dateReceived != null) {
                            _forecast.value = com.example.ircctracker.util.ForecastHelper.calculateForecast(dateReceived, months)
                        }
                    }
                    
                    launch {
                        val newsItems = geminiRepository.fetchNews()
                        _news.value = newsItems
                    }
                }
                
                result.onFailure { error ->
                    _uiState.value = DetailsUiState.Error(error.localizedMessage ?: "Unknown error")
                }
            }
        }
    }
    
    private val _aiStatus = MutableStateFlow<String>("")
    val aiStatus = _aiStatus.asStateFlow()

    private fun enhanceDetails(history: List<com.example.ircctracker.data.remote.HistoryEvent>) {
        viewModelScope.launch {
            _aiStatus.value = "AI: Analyzing..."
            android.util.Log.d("GeminiDebug", "Starting enhancement for ${history.size} items")
            try {
                val insights = geminiRepository.enhanceTimeline(history)
                if (insights.isNotEmpty()) {
                    _aiInsights.value = insights
                    _aiStatus.value = "AI: Loaded ${insights.size} insights"
                } else {
                    _aiStatus.value = "AI: No insights returned (Empty)"
                }
                android.util.Log.d("GeminiDebug", "Received ${insights.size} insights")
            } catch (e: Exception) {
                _aiStatus.value = "AI Error: ${e.message}"
                android.util.Log.e("GeminiDebug", "Error", e)
            }
        }
    }
}

class DetailsViewModelFactory(
    private val repository: DetailsRepository,
    private val geminiRepository: GeminiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailsViewModel(repository, geminiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

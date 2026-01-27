package com.example.ircctracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.ircctracker.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GeminiRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gemini_prefs", Context.MODE_PRIVATE)
    
    // Create a specific Retrofit instance for Gemini
    // Create a specific Retrofit instance for Gemini with longer timeout
    private val geminiService: GeminiApiService by lazy {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    fun getApiKey(): String? {
        val stored = prefs.getString("api_key", null)
        if (stored.isNullOrEmpty()) {
            return null
        }
        return stored
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString("api_key", key).apply()
    }

    suspend fun enhanceTimeline(events: List<HistoryEvent>): Map<String, String> {
        val apiKey = getApiKey() ?: return emptyMap()
        
        // 1. Check Cache
        val historyCache = context.getSharedPreferences("gemini_history_cache", Context.MODE_PRIVATE)
        val cachedMap = mutableMapOf<String, String>()
        val missingEvents = mutableListOf<HistoryEvent>()
        
        events.forEach { event ->
            val cachedExplanation = historyCache.getString(event.key, null)
            if (cachedExplanation != null) {
                cachedMap[event.key] = cachedExplanation
            } else {
                missingEvents.add(event)
            }
        }
        
        // If all event explanations are cached, return immediately!
        if (missingEvents.isEmpty()) {
            android.util.Log.d("GeminiRepo", "All events found in cache. Skipping API.")
            return cachedMap
        }

        return withContext(Dispatchers.IO) {
            try {
                // Construct the payload with Search Tool
                val prompt = buildPrompt(missingEvents)
                // Remove Google Search tool for simple timeline explanation to avoid tool-use complications
                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                    // No tools needed for definition lookup
                )

                val response = geminiService.generateContent(
                    model = "gemini-3-flash-preview", 
                    apiKey = apiKey,
                    request = request
                )

                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (text != null) {
                   android.util.Log.d("GeminiRepo", "Raw AI Response: $text")
                   val newInsights = parseResponse(text)
                   
                   // 2. Save new insights to cache
                   if (newInsights.isNotEmpty()) {
                        val editor = historyCache.edit()
                        newInsights.forEach { (key, value) ->
                            editor.putString(key, value)
                            cachedMap[key] = value // Add to result
                        }
                        editor.apply()
                   }
                   
                   // Return merged map (Cached + New)
                   cachedMap
                } else {
                     android.util.Log.e("GeminiRepo", "AI Response Text is NULL.")
                     // Return what we have from cache even if API fails
                     cachedMap
                }
            } catch (e: Exception) {
                Log.e("GeminiRepo", "Enhancement failed", e)
                // Return what we have from cache even if API fails
                cachedMap
            }
        }
    }

    suspend fun getProcessingTime(lob: String): Int? {
        val apiKey = getApiKey() ?: return null
        
        // 1. Check Cache (24h validity)
        val timeCache = context.getSharedPreferences("processing_time_cache", Context.MODE_PRIVATE)
        val lastUpdate = timeCache.getLong("${lob}_timestamp", 0L)
        val cachedValue = timeCache.getInt("${lob}_months", -1)
        val twentyFourHoursMs = 24 * 60 * 60 * 1000L
        
        if (cachedValue != -1 && (System.currentTimeMillis() - lastUpdate) < twentyFourHoursMs) {
             android.util.Log.d("GeminiRepo", "Returning cached processing time for $lob: $cachedValue")
             return cachedValue
        }

        return withContext(Dispatchers.IO) {
            try {
                // Specific prompt for processing time targeting official site
                val prompt = "Search site:canada.ca for the current official IRCC processing time for '$lob' applications. Return ONLY the number of months as an integer. If weeks, convert to months. If unknown, return -1."
                
                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    tools = listOf(Tool(googleSearch = GoogleSearch()))
                )

                val response = geminiService.generateContent(
                    model = "gemini-3-flash-preview", 
                    apiKey = apiKey,
                    request = request
                )

                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                // Parse integer from response
                val months = text?.replace(Regex("[^0-9]"), "")?.toIntOrNull()
                
                // 2. Save to Cache if valid
                if (months != null && months > 0) {
                    timeCache.edit()
                        .putInt("${lob}_months", months)
                        .putLong("${lob}_timestamp", System.currentTimeMillis())
                        .apply()
                }
                
                months
            } catch (e: Exception) {
                Log.e("GeminiRepo", "Processing time fetch failed", e)
                // Fallback to cache if request fails
                if (cachedValue != -1) cachedValue else null
            }
        }
    }

    suspend fun fetchNews(): List<String> {
        val apiKey = getApiKey() ?: return emptyList()
        
        return withContext(Dispatchers.IO) {
            try {
                val prompt = "Search for the latest official news from IRCC Canada (last 7 days) regarding Express Entry draws, processing time updates, or major policy changes. Summarize the top 3 most relevant updates into short bullet points. If no major news, mention the latest generic status."
                
                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    tools = listOf(Tool(googleSearch = GoogleSearch()))
                )

                val response = geminiService.generateContent(
                    model = "gemini-3-flash-preview", 
                    apiKey = apiKey,
                    request = request
                )

                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return@withContext emptyList()
                
                // Naive split by newlines or bullets
                text.split("\n")
                    .filter { it.isNotBlank() }
                    .map { it.replace(Regex("^[-*•] "), "").trim() }
                    .take(3)
            } catch (e: Exception) {
                Log.e("GeminiRepo", "News fetch failed", e)
                emptyList()
            }
        }
    }

    private fun buildPrompt(events: List<HistoryEvent>): String {
        val eventListString = events.joinToString("\n") { "${it.key}: ${it.dateCreated ?: "No date"}" }
        
        return """
            You are a helpful immigration assistant.
            Analyze the following list of immigration application history events:
            
            $eventListString
            
            For EACH event, provide a very short, single-sentence explanation (max 10 words) of what it means for the applicant.
            Do not group them. Return a JSON map where the KEY is the EXACT event string from the input (do not modify it, do not change case) and the value is the explanation.
            Example: {"FG_Medical_Passed": "Your medical exam results have been approved."}
            Use strict JSON format.
        """.trimIndent()
    }

    private fun parseResponse(response: String): Map<String, String> {
        return try {
            // Strip markdown code blocks if present
            val jsonString = response.trim()
                .replace(Regex("^```json"), "")
                .replace(Regex("^```"), "")
                .replace(Regex("```$"), "")
                .trim()
            
            val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
            com.google.gson.Gson().fromJson(jsonString, type)
        } catch (e: Exception) {
            Log.e("GeminiRepo", "Failed to parse JSON response: $response", e)
            emptyMap()
        }
    }
}

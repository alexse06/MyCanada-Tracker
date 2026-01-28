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
    
    // AI SERVICE REMOVED: Retrofit/GeminiApiService dependencies deleted.
    // Repository now relies 100% on Static Dictionary and ProcessingTimeData.

    fun getApiKey(): String? {
        // Kept for compatibility but unused
        return prefs.getString("api_key", null)
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString("api_key", key).apply()
    }

    suspend fun enhanceTimeline(events: List<HistoryEvent>): Map<String, String> {
        // 1. Check Cache
        val historyCache = context.getSharedPreferences("gemini_history_cache", Context.MODE_PRIVATE)
        val cachedMap = mutableMapOf<String, String>()
        val missingEvents = mutableListOf<HistoryEvent>()
        
        events.forEach { event ->
            val cachedExplanation = historyCache.getString(event.key, null)
            if (!cachedExplanation.isNullOrBlank() && cachedExplanation != "null") {
                cachedMap[event.key] = cachedExplanation
            } else {
                missingEvents.add(event)
            }
        }
        
        // If all event explanations are cached, return immediately!
        if (missingEvents.isEmpty()) {
            return cachedMap
        }
        
        // 2. Lookup in Static Dictionary
        missingEvents.forEach { event ->
             // Strip prefixes
             val cleanKey = event.key.replace(Regex("^(Medical|Status|Background|Eligibility):\\s*"), "")
             
             // Try Clean & Full Key
             var dictExplanation = com.example.ircctracker.data.local.StatusDictionary.getExplanation(cleanKey) 
                ?: com.example.ircctracker.data.local.StatusDictionary.getExplanation(event.key)
             
             // Try Smart IMM Extraction
             if (dictExplanation == null) {
                 val immMatch = Regex("IMM\\s*(\\d{4,5})", RegexOption.IGNORE_CASE).find(event.key)
                 if (immMatch != null) {
                     val immNumber = immMatch.groupValues[1]
                     dictExplanation = com.example.ircctracker.data.local.StatusDictionary.getExplanation(immNumber)
                 }
             }

             if (dictExplanation != null) {
                 historyCache.edit().putString(event.key, dictExplanation).apply()
                 cachedMap[event.key] = dictExplanation
                 android.util.Log.d("GeminiRepo", "Found in Dict: ${event.key}")
             } else {
                 android.util.Log.e("GeminiRepo", "MISSING in Dictionary: '${event.key}'")
                 // No AI fallback anymore. Just leave it without explanation.
             }
        }
        
        return cachedMap
    }

    suspend fun getProcessingTime(lob: String): Int? {
        // AI REMOVED: Replaced with Static Data Source
        val estimate = com.example.ircctracker.data.local.ProcessingTimeData.getEstimate(lob)
        android.util.Log.d("GeminiRepo", "Static Processing Time for '$lob': $estimate months")
        return estimate
    }

    suspend fun fetchNews(): List<String> {
        // AI REMOVED: Replaced with Official RSS Feed
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.io.canada.ca/io-server/gc/news/en/v2?dept=departmentofcitizenshipandimmigration&sort=publishedDate&orderBy=desc&publishedDate%3E=2021-07-23&pick=50&format=atom&atomtitle=Immigration,%20Refugees%20and%20Citizenship%20Canada"
                
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Android 10; Mobile; rv:125.0) Gecko/125.0 Firefox/125.0")
                    .header("Accept", "application/xml, application/atom+xml")
                    .build()
                
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("GeminiRepo", "RSS Fetch failed: ${response.code}")
                    return@withContext emptyList()
                }

                response.body?.byteStream()?.use { stream ->
                     val parser = RssParser()
                     val news = parser.parse(stream)
                     android.util.Log.d("GeminiRepo", "Fetched ${news.size} news items via RSS.")
                     news
                } ?: emptyList()
                
            } catch (e: Exception) {
                Log.e("GeminiRepo", "News RSS fetch failed", e)
                emptyList()
            }
        }
    }
}

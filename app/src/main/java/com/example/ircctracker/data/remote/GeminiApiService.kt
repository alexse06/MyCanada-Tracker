package com.example.ircctracker.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(
    val contents: List<Content>,
    val tools: List<Tool>? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null
)

data class Tool(
    val google_search_retrieval: GoogleSearchRetrieval? = null,
    val googleSearch: GoogleSearch? = null
)

class GoogleSearchRetrieval
class GoogleSearch

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

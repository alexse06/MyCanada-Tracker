package com.example.ircctracker.data.repository

import android.util.Log
import com.example.ircctracker.data.remote.DashboardResponse
import com.example.ircctracker.data.remote.IrccApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DashboardRepository(private val apiService: IrccApiService) {

    fun getApplications(token: String): Flow<Result<DashboardResponse>> = flow {
        try {
            // Ensure token has "Bearer " prefix if not already present
            val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            Log.i("DashboardRepo", "Fetching applications with token: ${authToken.take(15)}...")
            
            val response = apiService.getProfileSummary(authToken)
            Log.i("DashboardRepo", "Fetch successful. Items: ${response.apps?.size ?: 0}")
            emit(Result.success(response))
        } catch (e: Exception) {
            Log.e("DashboardRepo", "Fetch failed", e)
            emit(Result.failure(e))
        }
    }
}

package com.example.ircctracker.data.repository

import android.util.Log
import com.example.ircctracker.data.remote.DetailsRequest
import com.example.ircctracker.data.remote.DetailsResponse
import com.example.ircctracker.data.remote.IrccApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DetailsRepository(private val apiService: IrccApiService) {

    fun getDetails(token: String, appNum: String, uci: String): Flow<Result<DetailsResponse>> = flow {
        try {
            val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            Log.i("DetailsRepo", "Fetching details for $appNum (UCI: $uci)")
            
            // Clean inputs just in case
            val cleanUci = uci.replace(Regex("[^0-9]"), "")
            
            val request = DetailsRequest(
                applicationNumber = appNum,
                uci = cleanUci
            )
            
            val response = apiService.getApplicationDetails(authToken, request)
            Log.i("DetailsRepo", "Fetch successful")
            emit(Result.success(response))
        } catch (e: Exception) {
            Log.e("DetailsRepo", "Fetch failed", e)
            emit(Result.failure(e))
        }
    }
}

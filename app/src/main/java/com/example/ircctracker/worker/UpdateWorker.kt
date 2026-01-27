package com.example.ircctracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ircctracker.data.remote.IrccApiService
import com.example.ircctracker.data.remote.NetworkClient
import com.example.ircctracker.data.repository.AuthRepository
import com.example.ircctracker.data.repository.DetailsRepository
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("UpdateWorker", "Checking for updates...")
        
        // 1. Setup Dependencies Manually (since no Hilt)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.ircc-tracker-example.com/") // Using config would be better, but hardcoding for worker context safety
            .client(NetworkClient.getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val apiService = retrofit.create(IrccApiService::class.java)
        val authRepo = AuthRepository(apiService, applicationContext)
        val detailsRepo = DetailsRepository(apiService)

        // 2. Refresh Session
        val token = authRepo.refreshToken()
        if (token == null) {
            Log.e("UpdateWorker", "Failed to refresh token")
            return Result.failure()
        }

        // 3. Check for specific app (simplification: checking the last viewed app or first one)
        // In a real app, we'd iterate all tracked apps.
        // For this demo, let's assume we check the UCI which returns a list, or just check the first app if we knew IDs.
        // Better: DashboardRepository check.
        // Let's implement a simple check: Fetch Dashboard, hash result.
        
        try {
            val dashboardRepo = com.example.ircctracker.data.repository.DashboardRepository(apiService)
            val dashboardResult = dashboardRepo.getApplications(token).firstOrNull()?.getOrNull()
            
            if (dashboardResult != null && !dashboardResult.apps.isNullOrEmpty()) {
                val newHash = dashboardResult.apps.hashCode()
                val prefs = applicationContext.getSharedPreferences("worker_prefs", Context.MODE_PRIVATE)
                val lastHash = prefs.getInt("last_hash", 0)
                
                if (lastHash != 0 && lastHash != newHash) {
                    sendNotification("Update Detected", "Your application status has changed!")
                }
                
                prefs.edit().putInt("last_hash", newHash).apply()
            }
        
            return Result.success()
        } catch (e: Exception) {
            Log.e("UpdateWorker", "Error checking updates", e)
            return Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ircc_updates"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Application Updates", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(1, notification)
        } catch (e: SecurityException) {
            Log.e("UpdateWorker", "Permission denied for notifications", e)
        } catch (e: Exception) {
            Log.e("UpdateWorker", "Failed to send notification", e)
        }
    }
}

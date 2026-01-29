package com.example.ircctracker

import android.os.Bundle

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.ircctracker.data.remote.IrccApiService
import com.example.ircctracker.data.repository.AuthRepository
import com.example.ircctracker.ui.login.LoginScreen
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock

import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // Register permission launcher layout-independently
    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            android.util.Log.i("MainActivity", "Notification permission granted")
        } else {
            android.util.Log.w("MainActivity", "Notification permission denied")
        }
    }

    // Biometric Manager
    private lateinit var biometricManager: com.example.ircctracker.util.BiometricPromptManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        biometricManager = com.example.ircctracker.util.BiometricPromptManager(this)
        
        // Manual DI for simplicity in scaffold
        val retrofit = Retrofit.Builder()
            .baseUrl(com.example.ircctracker.data.AppConfig.BASE_URL)
            .client(com.example.ircctracker.data.remote.NetworkClient.getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val apiService = retrofit.create(IrccApiService::class.java)
        val repository = AuthRepository(apiService, applicationContext)
        val dashboardRepository = com.example.ircctracker.data.repository.DashboardRepository(apiService)
        
        // Request Notification Permission (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        setContent {
            MaterialTheme {
                val authState by repository.authState.collectAsState()
                var selectedAppId by remember { mutableStateOf<String?>(null) }
                val detailsRepository = com.example.ircctracker.data.repository.DetailsRepository(apiService)
                val geminiRepository = remember { com.example.ircctracker.data.repository.GeminiRepository(applicationContext) }
                
                // Security Lock State
                // Default to LOCKED if biometric is enabled in settings (default true if hardware available)
                val context = androidx.compose.ui.platform.LocalContext.current
                val prefs = remember { context.getSharedPreferences("security_prefs", android.content.Context.MODE_PRIVATE) }
                val isBiometricEnabled = remember { 
                    prefs.getBoolean("biometric_enabled", false) // Default OFF until user enables it
                }
                var isLocked by remember { mutableStateOf(isBiometricEnabled) }
                
                LaunchedEffect(Unit) {
                    if (isBiometricEnabled && biometricManager.isBiometricAvailable()) {
                        biometricManager.showBiometricPrompt(
                            onSuccess = { isLocked = false },
                            onFailed = { /* Keep locked */ },
                            onError = { _, _ -> /* Keep locked */ }
                        )
                    } else {
                        isLocked = false
                    }
                }
                
                // Schedule Background Work
                LaunchedEffect(Unit) {
                    // Use standard prefs for worker config (non-sensitive)
                    val workerPrefs = context.getSharedPreferences("worker_prefs", android.content.Context.MODE_PRIVATE)
                    val enabled = workerPrefs.getBoolean("notifications_enabled", true)
                    val intervalMinutes = workerPrefs.getLong("update_interval_minutes", 12 * 60L) // Default 12 hours
                    
                    if (enabled) {
                        val workRequest = PeriodicWorkRequestBuilder<com.example.ircctracker.worker.UpdateWorker>(
                            intervalMinutes, TimeUnit.MINUTES
                        ).build()
                        
                        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                            "IrccUpdateWork",
                            ExistingPeriodicWorkPolicy.UPDATE, // Update existing
                            workRequest
                        )
                    } else {
                        WorkManager.getInstance(applicationContext).cancelUniqueWork("IrccUpdateWork")
                    }
                }
                
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (isLocked) {
                        // Locked Screen UI
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                                androidx.compose.material3.Text(
                                    "App Locked",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.material3.Button(onClick = {
                                    biometricManager.showBiometricPrompt(
                                        onSuccess = { isLocked = false },
                                        onFailed = {},
                                        onError = { _, _ -> }
                                    )
                                }) {
                                    androidx.compose.material3.Text("Unlock")
                                }
                            }
                        }
                    } else {
                        // Main App Content
                        when (val state = authState) {
                            is com.example.ircctracker.data.repository.AuthState.LoggedIn -> {
                                if (selectedAppId == "SETTINGS") {
                                    com.example.ircctracker.ui.settings.SettingsScreen(
                                        geminiRepository = geminiRepository,
                                        onBack = { selectedAppId = null }
                                    )
                                } else if (selectedAppId != null) {
                                    com.example.ircctracker.ui.details.DetailsScreen(
                                        token = state.token,
                                        appNum = selectedAppId!!,
                                        uci = state.uci,
                                        repository = detailsRepository,
                                        geminiRepository = geminiRepository,
                                        onBack = { selectedAppId = null },
                                        onSettingsClick = { selectedAppId = "SETTINGS" }
                                    )
                                } else {
                                    com.example.ircctracker.ui.dashboard.DashboardScreen(
                                        token = state.token,
                                        repository = dashboardRepository,
                                        onAppClick = { appId -> 
                                            android.util.Log.i("MainActivity", "App Clicked: $appId")
                                            selectedAppId = appId 
                                        },
                                    )
                                }
                            }
                            else -> {
                                com.example.ircctracker.ui.login.LoginScreen(repository)
                            }
                        }
                    }
                }
            }
        }
    }
}

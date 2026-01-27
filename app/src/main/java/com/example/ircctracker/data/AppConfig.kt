package com.example.ircctracker.data

object AppConfig {
    const val BASE_URL = "https://api.ircc-tracker-suivi.apps.cic.gc.ca/"
    
    // Discovered from JWT token in captured requests
    object Cognito {
        const val REGION = "ca-central-1"
        const val USER_POOL_ID = "ca-central-1_7OCkCncWC"
        const val CLIENT_ID = "3cfutv5ffd1i622g1tn6vton5r"
    }
}

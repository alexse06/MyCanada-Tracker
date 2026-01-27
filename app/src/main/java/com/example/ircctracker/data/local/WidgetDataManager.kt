package com.example.ircctracker.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.ircctracker.R

data class WidgetState(
    val status: String,
    val lastUpdated: String,
    val appNumber: String
)

object WidgetDataManager {
    private const val PREF_NAME = "widget_prefs"
    private const val KEY_STATUS = "key_status"
    private const val KEY_LAST_UPDATED = "key_last_updated"
    private const val KEY_APP_NUMBER = "key_app_number"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveState(context: Context, status: String, lastUpdated: String, appNumber: String) {
        getPrefs(context).edit()
            .putString(KEY_STATUS, status)
            .putString(KEY_LAST_UPDATED, lastUpdated)
            .putString(KEY_APP_NUMBER, appNumber)
            .apply()
    }

    fun getState(context: Context): WidgetState {
        val prefs = getPrefs(context)
        return WidgetState(
            status = prefs.getString(KEY_STATUS, context.getString(R.string.unknown_status)) ?: "Unknown",
            lastUpdated = prefs.getString(KEY_LAST_UPDATED, "N/A") ?: "N/A",
            appNumber = prefs.getString(KEY_APP_NUMBER, "") ?: ""
        )
    }
}

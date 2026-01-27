package com.example.ircctracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.ircctracker.MainActivity
import com.example.ircctracker.R
import com.example.ircctracker.data.local.WidgetDataManager

class StatusWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val state = WidgetDataManager.getState(context)

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_status)
            views.setTextViewText(R.id.widget_status, state.status)
            views.setTextViewText(R.id.widget_last_updated, "Updated: ${state.lastUpdated}")
            views.setTextViewText(R.id.widget_app_num, state.appNumber)

            // Intent to launch app when clicked
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_status, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent)

            // Color coding logic (simple)
            val color = when (state.status.lowercase()) {
                "completed" -> -11751600 // Green #4CAF50
                "inprogress" -> -26368 // Orange #FF9800
                "unknown" -> -16777216 // Black
                else -> -16777216
            }
            views.setTextColor(R.id.widget_status, color)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        // Helper to force update all widgets from the app
        fun forceUpdateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, StatusWidget::class.java))
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
